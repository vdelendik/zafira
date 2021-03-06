/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.zafira.services.services.jmx;

import static com.qaprosoft.zafira.models.db.Setting.Tool.AMAZON;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.SdkBufferedInputStream;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.qaprosoft.zafira.models.db.Setting;
import com.qaprosoft.zafira.models.dto.aws.FileUploadType;
import com.qaprosoft.zafira.services.exceptions.AWSException;
import com.qaprosoft.zafira.services.exceptions.ServiceException;
import com.qaprosoft.zafira.services.services.SettingsService;

@ManagedResource(objectName = "bean:name=amazonService", description = "Amazon init Managed Bean",
		currencyTimeLimit = 15, persistPolicy = "OnUpdate", persistPeriod = 200)
public class AmazonService implements IJMXService
{

	private static final Logger LOGGER = Logger.getLogger(AmazonService.class);

	public static final String COMMENT_KEY = "comment";

	private static final String FILE_PATH_SEPARATOR = "/";

	private AmazonS3 s3Client;

	private BasicAWSCredentials awsCredentials;

	private String s3Bucket;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private CryptoService cryptoService;

	@Autowired
	private ClientConfiguration clientConfiguration;

	@Override
	@PostConstruct
	public void init()
	{
		String accessKey = null;
		String privateKey = null;
		String bucket = null;

		try
		{
			List<Setting> jiraSettings = settingsService.getSettingsByTool(AMAZON);
			for (Setting setting : jiraSettings)
			{
				if (setting.isEncrypted())
				{
					setting.setValue(cryptoService.decrypt(setting.getValue()));
				}
				switch (Setting.SettingType.valueOf(setting.getName()))
				{
				case AMAZON_ACCESS_KEY:
					accessKey = setting.getValue();
					break;
				case AMAZON_SECRET_KEY:
					privateKey = setting.getValue();
					break;
				case AMAZON_BUCKET:
					bucket = setting.getValue();
					break;
				default:
					break;
				}
			}
			init(accessKey, privateKey, bucket);
		} catch (Exception e)
		{
			LOGGER.error("Setting does not exist", e);
		}
	}

	@ManagedOperation(description = "Amazon initialization")
	@ManagedOperationParameters({
			@ManagedOperationParameter(name = "accessKey", description = "Amazon access key"),
			@ManagedOperationParameter(name = "privateKey", description = "Amazon private key"),
			@ManagedOperationParameter(name = "bucket", description = "Amazon bucket")})
	public void init(String accessKey, String privateKey, String bucket)
	{
		try
		{
			if (!StringUtils.isEmpty(accessKey) && !StringUtils.isEmpty(privateKey) && !StringUtils.isEmpty(bucket))
			{
				this.s3Bucket = bucket;
				this.awsCredentials = new BasicAWSCredentials(accessKey, privateKey);
				this.s3Client = new AmazonS3Client(this.awsCredentials, this.clientConfiguration);
			}
		} catch (Exception e)
		{
			LOGGER.error("Unable to initialize Jira integration: " + e.getMessage());
		}
	}

	@Override
	public boolean isConnected()
	{
		try
		{
			return this.s3Client.doesBucketExist(this.s3Bucket);
		} 
		catch (Exception e)
		{
			return false;
		}
	}

	public List<S3ObjectSummary> listFiles(String filePrefix)
	{
		ListObjectsRequest listObjectRequest = new ListObjectsRequest().withBucketName(s3Bucket).withPrefix(filePrefix);
		return getS3Client().listObjects(listObjectRequest).getObjectSummaries();
	}

	public String getComment(String key)
	{
		return getS3Client().getObjectMetadata(s3Bucket, key).getUserMetaDataOf(COMMENT_KEY);
	}

	public String getPublicLink(S3ObjectSummary objectSummary)
	{
		GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(s3Bucket,
				objectSummary.getKey());
		generatePresignedUrlRequest.setMethod(HttpMethod.GET);
		return getS3Client().generatePresignedUrl(generatePresignedUrlRequest).toString();
	}

	public String saveFile(final FileUploadType file, long principalId) throws ServiceException
	{
		SdkBufferedInputStream stream = null;
		GeneratePresignedUrlRequest request;
		try {
			stream = new SdkBufferedInputStream(file.getFile().getInputStream(), (int) (file.getFile().getSize() + 100));
			String type = Mimetypes.getInstance().getMimetype(file.getFile().getOriginalFilename());
			String key = getFileKey(file, principalId);

			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(type);
			metadata.setContentLength(file.getFile().getSize());

			PutObjectRequest putRequest = new PutObjectRequest(this.s3Bucket, key, stream, metadata);
			s3Client.putObject(putRequest);
			s3Client.setObjectAcl(this.s3Bucket, key, CannedAccessControlList.PublicRead);

			request = new GeneratePresignedUrlRequest(this.s3Bucket, key);

		} catch (IOException e) {
			throw new AWSException("Can't save file to Amazone", e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
		return s3Client.generatePresignedUrl(request).toString().split("\\?")[0];
	}

	public void removeFile(final String linkToFile) throws ServiceException
	{
		try
		{
			s3Client.deleteObject(new DeleteObjectRequest(this.s3Bucket, new URL(linkToFile).getPath().substring(1)));
		} catch (MalformedURLException e)
		{
			throw new ServiceException(e);
		}
	}

	private String getFileKey(final FileUploadType file, long principalId)
	{
		return file.getType().name() + FILE_PATH_SEPARATOR + principalId + FILE_PATH_SEPARATOR +
				RandomStringUtils.randomAlphanumeric(20) + "." + FilenameUtils.getExtension(file.getFile().getOriginalFilename());
	}

	@ManagedAttribute(description = "Get amazon client")
	public AmazonS3 getS3Client()
	{
		return s3Client;
	}
}

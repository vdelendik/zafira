package com.qaprosoft.zafira.tests.gui.components.table.row;

import com.qaprosoft.zafira.tests.gui.components.menus.TestArtifactMenu;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class TestRow extends AbstractRow
{

	@FindBy(xpath = "./td[1]/b")
	private WebElement testName;

	@FindBy(xpath = "./td[1]//timer")
	private WebElement elapsedTime;

	@FindBy(xpath = "./td[1]//span[2]")
	private WebElement owner;

	@FindBy(xpath = "./td[1]//span[3]")
	private WebElement device;

	@FindBy(xpath = "./td[2]//a[text() = 'Mark as passed']")
	private WebElement markAsPassed;

	@FindBy(xpath = "./td[2]//a[text() = 'Mark as known issue']")
	private WebElement markAsKnownIssue;

	@FindBy(xpath = "./td[2]//a[text() = 'Edit known issue']")
	private WebElement editKnownIssue;

	@FindBy(xpath = "./td[2]//a[text() = 'Assign task']")
	private WebElement assignTask;

	@FindBy(xpath = "./td[3]//a[contains(@class, 'bug-label-bg')]")
	private WebElement knownIssueLabel;

	@FindBy(xpath = "./td[3]//a[not(contains(@class, 'bug-label-bg'))]")
	private WebElement taskLabel;

	@FindBy(xpath = "./td[4]//button")
	private WebElement testArtifactMenuButton;

	@FindBy(xpath = "//div[preceding-sibling::header]/md-menu-content")
	private TestArtifactMenu testArtifactMenu;

	public TestRow(WebDriver driver, SearchContext context)
	{
		super(driver, context);
	}

	public WebElement getTestName()
	{
		return testName;
	}

	public String getTestNameText()
	{
		return testName.getText();
	}

	public WebElement getElapsedTime()
	{
		return elapsedTime;
	}

	public String getElapsedTimeText()
	{
		return elapsedTime.getText();
	}

	public WebElement getOwner()
	{
		return owner;
	}

	public String getOwnerName()
	{
		return owner.getText();
	}

	public WebElement getDevice()
	{
		return device;
	}

	public String getDeviceName()
	{
		return device.getText();
	}

	public WebElement getMarkAsPassed()
	{
		return markAsPassed;
	}

	public void clickMarkAsPassed()
	{
		markAsPassed.click();
	}

	public WebElement getMarkAsKnownIssue()
	{
		return markAsKnownIssue;
	}

	public void clickMarkAsKnownIssue()
	{
		markAsKnownIssue.click();
	}

	public WebElement getEditKnownIssue()
	{
		return editKnownIssue;
	}

	public void clickEditKnownIssue()
	{
		editKnownIssue.click();
	}

	public WebElement getAssignTask()
	{
		return assignTask;
	}

	public void clickAssignTask()
	{
		assignTask.click();
	}

	public WebElement getKnownIssueLabel()
	{
		return knownIssueLabel;
	}

	public String getKnownIssueTicket()
	{
		return knownIssueLabel.getText();
	}

	public WebElement getTaskLabel()
	{
		return taskLabel;
	}

	public String getTaskTicket()
	{
		return taskLabel.getText();
	}

	public WebElement getTestArtifactMenuButton()
	{
		return testArtifactMenuButton;
	}

	public TestArtifactMenu clickTestArtifactButton()
	{
		testArtifactMenuButton.click();
		return testArtifactMenu;
	}

	public TestArtifactMenu getTestArtifactMenu()
	{
		return testArtifactMenu;
	}
}

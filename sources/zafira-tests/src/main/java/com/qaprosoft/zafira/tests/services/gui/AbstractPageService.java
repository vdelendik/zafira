package com.qaprosoft.zafira.tests.services.gui;

import com.qaprosoft.zafira.tests.util.Config;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public abstract class AbstractPageService
{
	private static final Logger LOGGER = Logger.getLogger(AbstractPageService.class);

	protected int GENERAL_DASHBOARD_ID = Integer.valueOf(Config.get("dashboard.general.id"));
	protected WebDriver driver;

	protected AbstractPageService(WebDriver driver)
	{
		this.driver = driver;
	}

	public void pause(double seconds)
	{
		try
		{
			Thread.sleep(new Double(seconds).intValue());
		} catch (InterruptedException e)
		{
			LOGGER.error(e.getMessage());
		}
	}
}

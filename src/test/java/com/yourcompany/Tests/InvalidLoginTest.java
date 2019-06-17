package com.yourcompany.Tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.openqa.selenium.By;

import java.lang.reflect.Method;
import java.net.MalformedURLException;

public class InvalidLoginTest extends TestBase {


    @Test(dataProvider = "hardCodedBrowsers")
    public void blankCredentials(String browser, String version, String os, Method method) throws MalformedURLException {
        this.createDriver(browser, version, os, method.getName());

        getDriver().get("http://www.saucedemo.com");

        getDriver().findElement(By.id("user-name")).sendKeys("");
        getDriver().findElement(By.id("password")).sendKeys("");
        getDriver().findElement(By.cssSelector(".btn_action")).click();

        Assert.assertTrue(getDriver().findElement(By.cssSelector(".error-button")).isDisplayed());
    }
}

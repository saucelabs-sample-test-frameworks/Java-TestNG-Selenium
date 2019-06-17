package com.yourcompany.Tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.openqa.selenium.By;

import java.lang.reflect.Method;
import java.net.MalformedURLException;


public class RemoveItemFromCartTest extends TestBase {


    @Test(dataProvider = "hardCodedBrowsers")
    public void removeOne(String browser, String version, String os, Method method) throws MalformedURLException {
        this.createDriver(browser, version, os, method.getName());

        getDriver().get("http://www.saucedemo.com/inventory.html");
        getDriver().findElement(By.className("btn_primary")).click();
        getDriver().findElement(By.className("btn_primary")).click();
        getDriver().findElement(By.className("btn_secondary")).click();

        Assert.assertEquals(getDriver().findElement(By.className("shopping_cart_badge")).getText(), "1");

        getDriver().get("http://www.saucedemo.com/cart.html");
        long expected = getDriver().findElements(By.className("inventory_item_name")).size();
        Assert.assertEquals(expected, 1);
    }
}

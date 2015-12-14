package com.yourcompany.Utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverLogLevel;
import org.openqa.selenium.remote.RemoteWebDriver;


import java.rmi.UnexpectedException;
import java.util.logging.Level;

/**
 * Created by mehmetgerceker on 12/9/15.
 */
public class SauceHelpers {

    /**
     * Per browser setup for collecting console logs.
     * @param capabilities the desired capabilities object to add the capabilities to
     * @param logLevel log level for the browser logs to be collected.
     * @throws UnexpectedException Oh well something unexpected happened!
     */
    public static void setLogCapabilities(DesiredCapabilities capabilities, Level logLevel)
            throws UnexpectedException{

        String browser = capabilities.getBrowserName();
        if (browser.contentEquals(DesiredCapabilities.firefox().getBrowserName()) ||
                browser.contentEquals(DesiredCapabilities.chrome().getBrowserName())){

            LoggingPreferences logPrefs = new LoggingPreferences();
            logPrefs.enable(LogType.BROWSER, logLevel);
            capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        } else if(browser.contentEquals(DesiredCapabilities.operaBlink().getBrowserName()) ||
                browser.contentEquals(DesiredCapabilities.opera().getBrowserName())) {

            //Code below is not supported yet!
            //capabilities.setCapability("opera.logging.level", logLevel.getName());

            System.out.printf("Console log for browser: %s is not supported!\n", browser);

        } else if(browser.contentEquals(DesiredCapabilities.safari().getBrowserName())) {

            System.out.printf("Console log for browser: %s is not supported!\n", browser);

        } else if(browser.contentEquals(DesiredCapabilities.internetExplorer().getBrowserName())) {

            System.out.printf("Console log for browser: %s is not supported!\n", browser);

            //Code below not supported yet!
            /*InternetExplorerDriverLogLevel ieLogLevel;

            if (logLevel == Level.ALL){
                ieLogLevel = InternetExplorerDriverLogLevel.TRACE;
            } else if (logLevel == Level.INFO) {
                ieLogLevel = InternetExplorerDriverLogLevel.INFO;
            } else if (logLevel == Level.WARNING) {
                ieLogLevel = InternetExplorerDriverLogLevel.WARN;
            } else if (logLevel == Level.SEVERE) {
                ieLogLevel = InternetExplorerDriverLogLevel.ERROR;
            } else { //all else is debug
                ieLogLevel = InternetExplorerDriverLogLevel.DEBUG;
            }

            if (logLevel != Level.OFF) {
                capabilities.setCapability(InternetExplorerDriver.LOG_LEVEL, ieLogLevel);
            }*/

        } else if(browser.contentEquals(DesiredCapabilities.edge().getBrowserName())) {

            System.out.printf("Console log for browser: %s is not supported!\n", browser);

        } else {

            throw new UnexpectedException("Browser not valid!");

        }
    }

    /**
     * Wraps the log command to Se around to only send it to supported browsers.
     * @param driver driver object to be wrapped
     * @param logType log type to be requested.
     */
    public static void getLogsCommandWrapper(WebDriver driver, String logType) {

        String browser = ((RemoteWebDriver)driver).getCapabilities().getBrowserName();
        if (browser.contentEquals(DesiredCapabilities.firefox().getBrowserName()) ||
                browser.contentEquals(DesiredCapabilities.chrome().getBrowserName())){
            driver.manage().logs().get(logType.toString());
        } else {
            System.out.printf("Console log for browser: %s is not supported!\n", browser);
        }
    }
    /**
     * Will generate the URI that will be used to send commands to the Se instance.
     * If SauceConnect tunnel in use and not directed not to use it will use the SC command relay.
     *
     * @param doNotUseSauceConnectCmdRelay Even if available do not use the relay.
     * @return String formatted uri for Sauce Se commands.
     */
    public static String buildSauceUri(boolean doNotUseSauceConnectCmdRelay) {
        String seleniumURI = "@ondemand.saucelabs.com:80";
        String seleniumPort = System.getenv("SELENIUM_PORT");
        if (!doNotUseSauceConnectCmdRelay && seleniumPort != null) {
            //While running in CI, if Sauce Connect is running the SELENIUM_PORT env var will be set.
            //use SC relay port
            seleniumURI = String.format("@localhost:%s", seleniumPort);

        }
        return seleniumURI;
    }
    /**
     * Will generate the URI that will be used to send commands to the Se instance.
     * If SauceConnect tunnel in use it will use the SC command relay.
     * @return String formatted uri for Sauce Se commands.
     */
    public static String buildSauceUri() {
        return buildSauceUri(false);
    }
}

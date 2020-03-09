package com.yourcompany.Tests;

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.testng.SauceOnDemandAuthenticationProvider;
import com.saucelabs.testng.SauceOnDemandTestListener;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.UnexpectedException;

/**
 * Simple TestNG test which demonstrates being instantiated via a DataProvider in order to supply multiple browser combinations.
 *
 * @authors Neil Manvar, Dylan Lacey
 */
public class TestBase  {

    public String buildTag = System.getenv("BUILD_TAG");

    public String username = System.getenv("SAUCE_USERNAME");

    public String accesskey = System.getenv("SAUCE_ACCESS_KEY");

    /**
     * ThreadLocal variable which contains the  {@link WebDriver} instance which is used to perform browser interactions with.
     */
    private InheritableThreadLocal<WebDriver> webDriver = new InheritableThreadLocal<WebDriver>();

    /**
     * ThreadLocal variable which contains the Sauce Job Id.
     */
    private ThreadLocal<String> sessionId = new ThreadLocal<String>();

    /**
     * ThreadLocal variable which contains the Shutdown Hook instance for this Thread's WebDriver.
     *
     * Registered just before Browser creation, de-registered after 'quit' is called.
     */
    private ThreadLocal<Thread> shutdownHook = new ThreadLocal<>();

    /**
     * Creates the shutdownHook, or returns an existing copy.
     */
    private Thread getShutdownHook() {
        if (shutdownHook.get() == null) {
            shutdownHook.set( new Thread(() -> {
                try {
                    if (webDriver.get() != null) webDriver.get().quit();
                } catch (org.openqa.selenium.NoSuchSessionException ignored) { } // Don't care if session already closed
            }));
        }
        return shutdownHook.get();
    }

    /**
     * Registers the shutdownHook with the runtime.
     *
     * Ignoring exceptions on registration; They mean the VM is already shutting down and it's too late.
     */
    private void registerShutdownHook() {
        try {
            Runtime.getRuntime().addShutdownHook(getShutdownHook());
        } catch (IllegalStateException ignored) {} // Thrown if a hook is added while shutting down; We don't care
    }

    /**
     * De-registers the shutdownHook. This allows the GC to remove the thread and avoids double-quitting.
     *
     * Silently swallows exceptions if the VM is already shutting down; it's too late.
     */
    private void deregisterShutdownHook() {
        if (shutdownHook.get() != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(getShutdownHook());
            } catch (IllegalStateException ignored) { } // VM already shutting down; Irrelevant
        }
    }
    /**
     * DataProvider that explicitly sets the browser combinations to be used.
     *
     * @param testMethod
     * @return Two dimensional array of objects with browser, version, and platform information
     */
    @DataProvider(name = "hardCodedBrowsers", parallel = true)
    public static Object[][] sauceBrowserDataProvider(Method testMethod) {
        return new Object[][]{
                new Object[]{"MicrosoftEdge", "18.17763", "Windows 10"},
                new Object[]{"firefox", "latest", "Windows 10"},
                new Object[]{"internet explorer", "11.0", "Windows 8.1"},
                new Object[]{"safari", "12", "macOS 10.13"},
                new Object[]{"chrome", "latest", "macOS 10.13"},
                new Object[]{"firefox", "latest-1", "Windows 10"},
        };
    }

    /**
     * @return the {@link WebDriver} for the current thread
     */
    public WebDriver getWebDriver() {
        return webDriver.get();
    }

    /**
     *
     * @return the Sauce Job id for the current thread
     */
    public String getSessionId() {
        return sessionId.get();
    }

    /**
     * Constructs a new {@link RemoteWebDriver} instance which is configured to use the capabilities defined by the browser,
     * version and os parameters, and which is configured to run against ondemand.saucelabs.com, using
     * the username and access key populated by the {@link #authentication} instance.
     *
     * @param browser Represents the browser to be used as part of the test run.
     * @param version Represents the version of the browser to be used as part of the test run.
     * @param os Represents the operating system to be used as part of the test run.
     * @param methodName Represents the name of the test case that will be used to identify the test on Sauce.
     * @return
     * @throws MalformedURLException if an error occurs parsing the url
     */
    protected void createDriver(String browser, String version, String os, String methodName)
            throws MalformedURLException, UnexpectedException {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        // set desired capabilities to launch appropriate browser on Sauce
        capabilities.setCapability(CapabilityType.BROWSER_NAME, browser);
        capabilities.setCapability(CapabilityType.VERSION, version);
        capabilities.setCapability(CapabilityType.PLATFORM, os);
        capabilities.setCapability("name", methodName);

        if (buildTag != null) {
            capabilities.setCapability("build", buildTag);
        }

        // Launch remote browser and set it as the current thread
        webDriver.set(new RemoteWebDriver(
                new URL("https://" + username + ":" + accesskey + "@ondemand.saucelabs.com/wd/hub"),
                capabilities));

        registerShutdownHook();

        // set current sessionId
        String id = ((RemoteWebDriver) getWebDriver()).getSessionId().toString();
        sessionId.set(id);
    }

    /**
     * Method that gets invoked after test.
     * Dumps browser log and
     * Closes the browser
     */
    @AfterMethod
    public void tearDown(ITestResult result) throws Exception {
        ((JavascriptExecutor) webDriver.get()).executeScript("sauce:job-result=" + (result.isSuccess() ? "passed" : "failed"));
        webDriver.get().quit();
        deregisterShutdownHook();
    }

    protected void annotate(String text) {
        ((JavascriptExecutor) webDriver.get()).executeScript("sauce:context=" + text);
    }
}

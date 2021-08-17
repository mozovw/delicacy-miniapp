package com.delicacy.miniapp.service.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author code4crafter@gmail.com <br>
 * Date: 13-7-26 <br>
 * Time: 下午1:41 <br>
 */
public class WebDriverPool {

    private final static int DEFAULT_CAPACITY = 5;
    private final static int STAT_RUNNING = 1;
    private final static int STAT_CLODED = 2;
    private final int capacity;
    private AtomicInteger stat = new AtomicInteger(STAT_RUNNING);
    /*
     * new fields for configuring phantomJS
     */
    private WebDriver mDriver = null;
    /**
     * store webDrivers created
     */
    private List<WebDriver> webDriverList = Collections
            .synchronizedList(new ArrayList<WebDriver>());
    /**
     * store webDrivers available
     */
    private BlockingDeque<WebDriver> innerQueue = new LinkedBlockingDeque<WebDriver>();

    public WebDriverPool(int capacity) {
        this.capacity = capacity;
    }

    public WebDriverPool() {
        this(DEFAULT_CAPACITY);
    }


    /**
     * Configure the GhostDriver, and initialize a WebDriver instance. This part
     * of code comes from GhostDriver.
     * https://github.com/detro/ghostdriver/tree/master/test/java/src/test/java/ghostdriver
     *
     * @throws IOException
     * @author bob.li.0718@gmail.com
     */
    public void configure() throws IOException {
        String property = System.getProperties().getProperty("webdriver.chrome.driver");
        if (!ObjectUtils.isEmpty(property)) {
            List<String> args = new ArrayList<>();
            args.add("headless");
            args.add("disable-gpu");
            ChromeOptions options = new ChromeOptions();
            options.addArguments(args);
            this.mDriver = new ChromeDriver(options);
            return;
        }

        property = System.getProperties().getProperty("webdriver.gecko.driver");
        if (!ObjectUtils.isEmpty(property)) {
            this.mDriver = new FirefoxDriver();
        }


    }


    /**
     * @return
     * @throws InterruptedException
     */
    public WebDriver get() throws InterruptedException {
        checkRunning();
        WebDriver poll = innerQueue.poll();
        if (poll != null) {
            return poll;
        }
        if (webDriverList.size() < capacity) {
            synchronized (webDriverList) {
                if (webDriverList.size() < capacity) {
                    // add new WebDriver instance into pool
                    try {
                        configure();
                        innerQueue.add(mDriver);
                        webDriverList.add(mDriver);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return innerQueue.take();
    }

    public void returnToPool(WebDriver webDriver) {
        checkRunning();
        innerQueue.add(webDriver);
    }

    protected void checkRunning() {
        if (!stat.compareAndSet(STAT_RUNNING, STAT_RUNNING)) {
            throw new IllegalStateException("Already closed!");
        }
    }

    public void closeAll() {
        checkRunning();
        for (WebDriver webDriver : webDriverList) {
            webDriver.quit();
        }
    }

}

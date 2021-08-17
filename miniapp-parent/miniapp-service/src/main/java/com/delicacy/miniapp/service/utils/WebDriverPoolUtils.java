package com.delicacy.miniapp.service.utils;

import com.delicacy.miniapp.service.driver.WebDriverPool;
import org.openqa.selenium.WebDriver;

import java.util.function.Consumer;

/**
 * @author yutao
 * @create 2021-08-17 8:03
 **/

public class WebDriverPoolUtils {


    private volatile WebDriverPool webDriverPool;

    public void  processPage(String url, Boolean isFinal, Consumer<String> consumer) {
        WebDriver webDriver = getWebDriver();
        webDriver.get(url);
        consumer.accept(url);
        if (isFinal) {
            closeAll();
        } else {
            returnToPool(webDriver);
        }
    }

    private void closeAll() {
        webDriverPool.closeAll();
    }

    public void todo(String url, Consumer consumer) {
        WebDriver webDriver = getWebDriver();
        webDriver.get(url);
        consumer.accept(null);
        returnToPool(webDriver);
    }

    protected WebDriver getWebDriver() {
        if (webDriverPool == null)
            synchronized (WebDriverPool.class) {
                if (webDriverPool == null) {
                    webDriverPool = new WebDriverPool(Runtime.getRuntime().availableProcessors());
                }
            }
        try {
            return webDriverPool.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    protected void returnToPool(WebDriver webDriver) {
        if (webDriverPool != null) {
            synchronized (WebDriverPool.class) {
                if (webDriverPool != null) {
                    webDriverPool.returnToPool(webDriver);
                }
            }
        }
    }


}

package com.anhtester.helpers;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;

public class WebUI {
    private final WebDriver driver;
    private final int DEFAULT_TIMEOUT = 5;

    public WebUI(WebDriver driver) {
        this.driver = driver;
    }

    // Hàm phân tích lỗi bằng Gemini API
    private void analyzeError(String errorMessage) {
        String prompt = "Phân tích lỗi trong Selenium Java: " + errorMessage + " và đề xuất cách khắc phục.";
        String analysis = GeminiAPIClientHelper.sendErrorForGeminiAI(prompt);
        if (analysis.isEmpty()) {
            System.err.println("Không thể phân tích lỗi từ Gemini API: " + errorMessage);
        } else {
            System.out.println("Phân tích lỗi từ Gemini: " + analysis);
        }
    }

    // Chờ phần tử hiển thị
    private WebElement waitForElementVisible(By locator, int timeout) {
        String errorMessage = "";
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            errorMessage = "Hết thời gian chờ phần tử hiển thị: " + locator + ". Lỗi: " + e.getMessage();
            //System.err.println(errorMessage);
            analyzeError(errorMessage);
            Assert.fail(errorMessage);
            return null;
        }
    }

    // Chờ phần tử có thể nhấn
    private WebElement waitForElementClickable(By locator, int timeout) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            return wait.until(ExpectedConditions.elementToBeClickable(locator));
        } catch (TimeoutException e) {
            String errorMessage = "Hết thời gian chờ phần tử có thể nhấn: " + locator + ". Lỗi: " + e.getMessage();
            //System.err.println(errorMessage);
            analyzeError(errorMessage);
            Assert.fail(errorMessage);
            return null;
        }
    }

    public void clickElement(By locator) {
        try {
            WebElement element = waitForElementClickable(locator, DEFAULT_TIMEOUT);
            element.click();
            System.out.println("Nhấn thành công vào phần tử: " + locator);
        } catch (NoSuchElementException e) {
            String errorMessage = "Không tìm thấy phần tử: " + locator + ". Lỗi: " + e.getMessage();
            //System.err.println(errorMessage);
            analyzeError(errorMessage);
            Assert.fail(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Lỗi khi nhấn vào phần tử: " + locator + ". Lỗi: " + e.getMessage();
            //System.err.println(errorMessage);
            analyzeError(errorMessage);
            Assert.fail(errorMessage);
        }
    }

    public void setText(By locator, String value) {
        try {
            WebElement element = waitForElementVisible(locator, DEFAULT_TIMEOUT);
            element.clear();
            element.sendKeys(value);
            System.out.println("Nhập thành công '" + value + "' vào phần tử: " + locator);
        } catch (NoSuchElementException e) {
            String errorMessage = "Không tìm thấy phần tử: " + locator + ". Lỗi: " + e.getMessage();
            //System.err.println(errorMessage);
            analyzeError(errorMessage);
            Assert.fail(errorMessage);
        }
        catch (Exception e) {
            String errorMessage = "Lỗi khi nhập văn bản vào phần tử: " + locator + ". Lỗi: " + e.getMessage();
            //System.err.println(errorMessage);
            analyzeError(errorMessage);
            Assert.fail(errorMessage);
        }
    }

    public String getElementText(By locator) {
        try {
            WebElement element = waitForElementVisible(locator, DEFAULT_TIMEOUT);
            String text = element.getText();
            System.out.println("Lấy văn bản thành công từ phần tử: " + locator + ": " + text);
            return text;
        } catch (NoSuchElementException e) {
            String errorMessage = "Không tìm thấy phần tử: " + locator + ". Lỗi: " + e.getMessage();
            //System.err.println(errorMessage);
            analyzeError(errorMessage);
            Assert.fail(errorMessage);
            return "";
        } catch (Exception e) {
            String errorMessage = "Lỗi khi lấy văn bản từ phần tử: " + locator + ". Lỗi: " + e.getMessage();
            //System.err.println(errorMessage);
            analyzeError(errorMessage);
            Assert.fail(errorMessage);
            return "";
        }
    }
}
package com.anhtester.ai;

import com.anhtester.helpers.WebUI;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

public class SeleniumWithGeminiTest {
    private WebDriver driver;
    private WebUI webUI;

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        webUI = new WebUI(driver);
    }

    @Test
    public void testLoginAndAnalyzeError() {
        driver.get("https://crm.anhtester.com/admin/authentication");

        webUI.setText(By.id("email"), "admin@example.com");
        webUI.setText(By.id("password123"), "123456");
        webUI.clickElement(By.xpath("//button[normalize-space()='Login']"));

        String dashboardText = webUI.getElementText(By.xpath("//span[normalize-space()='Dashboard']"));
        System.out.println("Dashboard menu: " + dashboardText);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
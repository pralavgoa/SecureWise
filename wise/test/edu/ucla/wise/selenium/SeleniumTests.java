package edu.ucla.wise.selenium;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * TODO: this test is incomplete
 * 
 * @author pdessai
 * 
 */
public class SeleniumTests {

    @Test
    public void seleniumTestCase() {
        WebDriver driver = new HtmlUnitDriver();

        driver.get("http://survey.ctsi.ucla.edu:8080/WISE/admin/");

        WebElement element = driver.findElement(By.name("username"));

        System.out.println(element.getText());
    }

    @Test
    public void seleniumTestCase2() {
        WebDriver driver = new HtmlUnitDriver();

        driver.get("https://ucrex.ctsi.ucla.edu/debug_shrine_webclient/");

        WebElement myDynamicElement = (new WebDriverWait(driver, 10)).until(ExpectedConditions
                .presenceOfElementLocated(By.tagName("body")));
        System.out.println(myDynamicElement.getText());
    }
}

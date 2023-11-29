import edu.sumdu.tss.elephant.Preset;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.*;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class MyStepdefs {
    static Map<String, String> element_by_xpath = new HashMap<String, String>();
    static WebDriver driver;

    @BeforeAll
    public static void init_before_all(){
    System.setProperty("webdriver.chrome.driver","src/test/resources/chromedriver_win32/chromedriver.exe");
    //System.setProperty("webdriver.http.factory", "jdk-http-client");
    driver = new ChromeDriver();
    driver.manage().window().maximize();

        element_by_xpath.put("Sign in", "/html/body/main/div[1]/div/div[2]/form/button");
        element_by_xpath.put("Logout", "/html/body/header/div/a");
        element_by_xpath.put("Dashboard", "//*[@id=\"sidebarMenu\"]/div/ul/li[1]/a");
    }



    @And("I navigate to the {string} page")
    public void iNavigateToThePage(String arg0) {
        driver.get(Preset.BASE_URL+arg0);
    }

    @When("I fill in {string} with {string}")
    public void iFillInWith(String arg0, String arg1) {
        driver.findElement(By.id(arg0)).sendKeys(arg1);
    }

    @And("I click on the {string} button")
    public void iClickOnTheButton(String arg0) {
        driver.findElement(By.xpath(element_by_xpath.get(arg0))).click();
    }

    @Then("I should be redirected on the {string} page")
    public void iShouldBeRedirectedOnThePage(String arg0) throws IOException {
        String cur_url=driver.getCurrentUrl();
        File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File("src\\test\\resources\\screenshots\\On_page_"+arg0+".png"));
        Assertions.assertEquals(cur_url,Preset.BASE_URL+arg0);
    }

    @And("I should see {string} message")
    public void iShouldSeeMessage(String arg0) {
    }

    @And("I should see {string},{string} and {string} links")
    public void iShouldSeeAndLinks(String arg0, String arg1, String arg2) {
    }

    @And("I should see {string} button")
    public void iShouldSeeButton(String arg0) {
    }

    @And("I should see {string} message as {string}")
    public void iShouldSeeMessageAs(String arg0, String arg1) {
    }
}

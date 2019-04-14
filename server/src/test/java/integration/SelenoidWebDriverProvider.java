package integration;

import com.codeborne.selenide.WebDriverProvider;
import common.TestProperties;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class SelenoidWebDriverProvider implements WebDriverProvider {

    @Override
    public WebDriver createDriver(DesiredCapabilities desiredCapabilities) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setBrowserName("chrome");
        capabilities.setVersion("71.0");
        capabilities.setCapability("enableVNC", true);
        capabilities.setCapability("enableVideo", false);
        try {
            return new RemoteWebDriver(
                    URI.create("http://"+ TestProperties.getInstance().getAppHost()+":4444/wd/hub").toURL(),
                    capabilities
            );
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}

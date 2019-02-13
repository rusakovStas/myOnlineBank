package integration;

import com.codeborne.selenide.WebDriverProvider;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Properties;

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
                    URI.create("http://"+TestProperties.getInstance().getAppHost()+":4444/wd/hub").toURL(),
                    capabilities
            );
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.solution.ui.utils;

import com.solution.common.utils.ApplicationProperties;
import com.solution.common.utils.Constants;
import com.codeborne.selenide.WebDriverRunner;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
public class DriverManager {
    private static final ThreadLocal < WebDriver > webDriverThreadLocal = new ThreadLocal <> ( );
    private final Logger log = LoggerFactory.getLogger ( DriverManager.class );

    private WebDriverWait wait;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private DriverWait driverWait;

    @Autowired
    private Environment environment;


    public void createSeleniumDriver ( ) throws Exception {
        //if ( getWebDriver ( ) == null ) {
            if ( Arrays.toString ( this.environment.getActiveProfiles ( ) ).contains ( "jenkins" ) ) {
                log.info ( "Remote URL for Selenium: "+ applicationProperties.getSeleniumGridUrl ( ) );
                setRemoteDriver ( new URL ( applicationProperties.getSeleniumGridUrl ( ) ) );
            } else {
                setLocalWebDriver ( false);
            }
      //  }
        WebDriverRunner.setWebDriver ( getWebDriver ( ) );
        WebDriverRunner.getWebDriver ( ).manage ( ).deleteAllCookies ( );//useful for AJAX pages
        //WebDriverRunner.getWebDriver ( ).manage ( ).window ().maximize ();
    }
    private ChromeOptions buildChromeOptions(boolean mobile, boolean headless) throws Exception {
        log.info("Building Chrome options..");
        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("download.default_directory", System.getProperty("downloadDirectory"));
        //chromePrefs.put("profile.default_content_settings.popups", 1);



       ChromeOptions _options = new ChromeOptions();
        if(mobile) {
            log.info("Building mobile simulator capabilities..");
            throw new RuntimeException("Mobile browser not yet supported");
//            _options = buildSimulatorCapabilities();
        }
        //_options.setExperimentalOption("prefs", chromePrefs);
        //_options.setPageLoadStrategy(PageLoadStrategy.NONE);
        _options.setAcceptInsecureCerts(true);
        _options.addArguments("--acceptSslCerts");
        _options.addArguments("--ignore-certificate-errors");
        _options.addArguments("--start-maximized");
        _options.addArguments("--enable-automation"); // https://stackoverflow.com/a/43840128/1689770
        _options.addArguments("--no-sandbox"); //https://stackoverflow.com/a/50725918/1689770
        _options.addArguments("--disable-features=VizDisplayCompositor");
        _options.addArguments("--disable-infobars"); //https://stackoverflow.com/a/43840128/1689770
        _options.addArguments("--disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
        //_options.addArguments("--disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
        //_options.addArguments("--disable-gpu");
        _options.setAcceptInsecureCerts(true);
        if (headless) {
            _options.addArguments("--headless");
            _options.addArguments("--disable-gpu");
            _options.addArguments("window-size=1296, 696");
        }
        log.info("Returning Chrome options");
        return _options;
    }
    public String buildURL() {
        String url      = "ondemand.saucelabs.com/wd/hub";
       // String auth     = "basic"; // todo none|basic|etc
        String username = "CTFSTest";//"setiteam";
        String password = "21dd0eae-99ff-476e-91d3-2099afd3525f";//"e04357af-5e6e-4a5f-b6c6-a503c9181813";
        return String.format("http://%s:%s@%s", username, password, url);
    }
    public void setLocalWebDriver (boolean headless) throws Exception {
        
		switch (applicationProperties.getBrowser ( )) {
            case ("chrome"):
                WebDriverManager.chromedriver ( ).cachePath ( Constants.DRIVER_DIRECTORY ).avoidOutputTree ( ).setup ( );
                ChromeOptions chromeOptions = new ChromeOptions ( );
                chromeOptions.addArguments ( "--disable-logging" );
                chromeOptions.addArguments ( "--no-sandbox" );
                chromeOptions.addArguments ( "--disable-dev-shm-usage" );
                chromeOptions.setCapability ( "platformName" , Platform.ANY );
                webDriverThreadLocal.set ( new ChromeDriver ( chromeOptions ) );
                break;
            case "chrome-saucelabs":
                log.info("Returning saucelab chromedriver instance..");
//                service = new ChromeDriverService.Builder()
//                        .usingDriverExecutable(new File(System.getProperty("webdriver.chrome.driver")))
//                        .usingAnyFreePort()
//                        .build();
                //chromeOptions = buildChromeOptions(false, headless);
                ChromeOptions remotechromeOptions = new ChromeOptions ( );
                remotechromeOptions.addArguments ( "--disable-logging" );
                remotechromeOptions.addArguments ( "--no-sandbox" );
                remotechromeOptions.addArguments ( "--disable-dev-shm-usage" );
                remotechromeOptions.setCapability ( "platformName" , Platform.ANY );
                remotechromeOptions.setCapability("idleTimeout", "480");
               //_driver = new ChromeDriver(service, cropts);
                webDriverThreadLocal.set(new RemoteWebDriver(new URL(buildURL()), remotechromeOptions));
                break;
            case "chrome-emulator":
                log.info("Returning chrome-emulator instance..");
                WebDriverManager.chromedriver().setup();
                ChromeOptions cropts = new ChromeOptions ( );
                ChromeDriverService service = new ChromeDriverService.Builder()
                        .usingDriverExecutable(new File(Constants.DRIVER_DIRECTORY+"/chromedriver.exe"))
                        .usingAnyFreePort()
                        .build();
                cropts = buildChromeOptions(false, headless);
                Map<String, String> mobileEmulation = new HashMap<>();
               mobileEmulation.put("deviceName", "Nexus 7");
                cropts.setExperimentalOption("mobileEmulation", mobileEmulation);
              webDriverThreadLocal.set(new ChromeDriver(service, cropts));
               // rv[1] = new Boolean(true);
                break;
            case "iphone":
                log.info("Returning saucelabs iphone instance..");
                System.out.println("Returning saucelabs iphone instance..");
                MutableCapabilities cropts1 = new MutableCapabilities();
                cropts1.setCapability("browserName", "Safari");
                cropts1.setCapability("appiumVersion", "1.17.1");
                //cropts.setCapability("browserVersion", "latest");
                cropts1.setCapability("deviceName", "iPhone X Simulator");
                cropts1.setCapability("deviceOrientation", "portrait");
                cropts1.setCapability("platformName", "iOS");
                cropts1.setCapability("platformVersion", "13.2");
                cropts1.setCapability("idleTimeout", "540");
                cropts1.setCapability("maxDuration", "900");
                cropts1.setCapability("commandTimeout", "600");
                webDriverThreadLocal.set(new RemoteWebDriver(new URL(buildURL()), cropts1));
               //rv[1] = new Boolean(true);
                break;
            case "android":
                log.info("Returning saucelabs android instance..");
                System.out.println("Returning saucelabs android instance..");
                MutableCapabilities caps = new ChromeOptions();
//                caps.setCapability("browserName", "Chrome");
//                caps.setCapability("browserVersion", "latest");
//               caps.setCapability("platformName", "Android");
//                caps.setCapability("platformVersion", "10.0");
//                caps.setCapability("username", "setiteam");
//                caps.setCapability("idleTimeout", "540");
//                caps.setCapability("commandTimeout", "600");
//                caps.setCapability("appiumVersion", "1.9.1");
//                caps.setCapability("deviceName", "Google Pixel 3a XL GoogleAPI Emulator");
//                caps.setCapability("deviceOrientation", "Portrait");
//                
                caps.setCapability("browserName", "Chrome");
                caps.setCapability("browserVersion", "latest");
                caps.setCapability("platformName", "Android");
                caps.setCapability("platformVersion", "12.0");
                caps.setCapability("username", "CTFSTest");
                caps.setCapability("idleTimeout", "540");
                caps.setCapability("commandTimeout", "600");
                caps.setCapability("appiumVersion", "1.22.1");
                caps.setCapability("deviceName", "Google Pixel 3a XL GoogleAPI Emulator");
                caps.setCapability("deviceOrientation", "Portrait");
                
                webDriverThreadLocal.set(new RemoteWebDriver(new URL(buildURL()), caps));
              // rv[1] = new Boolean(true);
                break;
            case ("firefox"):
                WebDriverManager.firefoxdriver ( ).cachePath ( Constants.DRIVER_DIRECTORY ).avoidOutputTree ( ).setup ( );
                FirefoxOptions firefoxOptions = new FirefoxOptions ( );
                firefoxOptions.setCapability ( "marionette" , true );
                webDriverThreadLocal.set ( new FirefoxDriver ( firefoxOptions ) );
                break;
            case ("safari"):
                WebDriverManager.safaridriver ( ).cachePath ( Constants.DRIVER_DIRECTORY ).avoidOutputTree ( ).setup ( );
                webDriverThreadLocal.set ( new OperaDriver ( ) );
                break;
            case ("edge"):
                WebDriverManager.edgedriver ( ).cachePath ( Constants.DRIVER_DIRECTORY ).avoidOutputTree ( ).setup ( );
                webDriverThreadLocal.set ( new EdgeDriver ( ) );
                break;
            default:
                throw new NoSuchElementException ( "Failed to create or absence of WebDriver for::: " + applicationProperties.getBrowser ( ) + " ::: Please check DriverManager class." );
        }
        driverWait.getDriverWaitThreadLocal ( ).set ( new WebDriverWait ( webDriverThreadLocal.get ( ) , Constants.timeoutShort , Constants.pollingShort ) );
    }

    private void setRemoteDriver (URL hubUrl) {
        Capabilities capability;
        switch (applicationProperties.getBrowser ( )) {
            case "firefox":
                capability = DesiredCapabilities.firefox ( );
                webDriverThreadLocal.set ( new RemoteWebDriver ( hubUrl , capability ) );
                break;
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions ( );
                chromeOptions.setCapability ( "enableVNC", true );
                chromeOptions.setCapability ( "enableVideo", true );
                chromeOptions.setCapability ( "enableLog", true );
                chromeOptions.setCapability ( "screenResolution", "1920x1080x24" );
                chromeOptions.setCapability ( "platformName", "WINDOWS" );
                webDriverThreadLocal.set ( new RemoteWebDriver ( hubUrl , chromeOptions ) );
                break;
            case "ie":
                capability = DesiredCapabilities.internetExplorer ( );
                webDriverThreadLocal.set ( new RemoteWebDriver ( hubUrl , capability ) );
                break;
            case "safari":
                capability = DesiredCapabilities.safari ( );
                webDriverThreadLocal.set ( new RemoteWebDriver ( hubUrl , capability ) );
                break;
            case ("edge"):
                capability = DesiredCapabilities.edge ( );
                webDriverThreadLocal.set ( new RemoteWebDriver ( hubUrl , capability ) );
                break;
            default:
                throw new NoSuchElementException ( "Failed to create an instance of RemoteWebDriver for: " + applicationProperties.getBrowser ( ) );
        }
        driverWait.getDriverWaitThreadLocal ( ).set ( new WebDriverWait ( webDriverThreadLocal.get ( ) , Constants.timeoutShort , Constants.pollingShort ) );
    }

    public WebDriver getWebDriver ( ) {
        return webDriverThreadLocal.get ( );
    }

    public JavascriptExecutor getJSExecutor ( ) {
        return (JavascriptExecutor) getWebDriver ( );
    }

    public boolean isDriverExisting ( ) {
        File geckoDriver = new File ( Constants.DRIVER_DIRECTORY + "/geckodriver" + getExtension ( ) );
        File chromedriver = new File ( Constants.DRIVER_DIRECTORY + "/chromedriver" + getExtension ( ) );
        return geckoDriver.exists ( ) && chromedriver.exists ( );
    }

    public void downloadDriver ( ) {
        try {
            Process process;
            if ( getOperatingSystem ( ).equals ( "win" ) ) {
                process = Runtime.getRuntime ( ).exec ( "cmd.exe /c downloadDriver.sh" , null , new File ( Constants.COMMON_RESOURCES ) );
            } else {
                process = Runtime.getRuntime ( ).exec ( new String[]{"sh" , "-c" , Constants.COMMON_RESOURCES + "/downloadDriver.sh"} );
            }
            process.waitFor ( );
            BufferedReader reader = new BufferedReader ( new InputStreamReader ( process.getInputStream ( ) ) );
            String line = reader.readLine ( );
            while (line != null) {
                log.debug ( line );
                line = reader.readLine ( );
            }
        } catch (Exception e) {
            e.printStackTrace ( );
        }
    }

    private String getOperatingSystem ( ) {
        String os = System.getProperty ( "os.name" , "generic" ).toLowerCase ( Locale.ROOT );
        log.debug ( "Operative System Detected: " + os );
        if ( os.contains ( "mac" ) || os.contains ( "darwin" ) ) {
            return "mac";
        } else if ( os.contains ( "win" ) ) {
            return "win";
        } else {
            return "linux";
        }
    }

    private String getExtension ( ) {
        String extension = "";
        if ( getOperatingSystem ( ).contains ( "win" ) ) {
            return ".exe";
        }
        return extension;
    }
}

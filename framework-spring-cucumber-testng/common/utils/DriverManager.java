package com.solution.mobile.utils;


import com.solution.common.utils.ApplicationProperties;
import com.solution.common.utils.Constants;
import com.solution.common.utils.SelenoidType;
import com.solution.common.utils.SelenoidValues;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobileCapabilityType;
import io.github.bonigarcia.wdm.WebDriverManager;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

@Component
public class DriverManager {

    private final static  ThreadLocal<AppiumDriver>  appiumDriverThreadLocal = new ThreadLocal <> ( );
    private static final ThreadLocal < WebDriver > webDriverThreadLocal = new ThreadLocal <> ( );
    private final Logger log = LoggerFactory.getLogger ( DriverManager.class );

    @Autowired
    private Environment environment;
    
    @Autowired
    private ApplicationProperties applicationProperties;
    public void createWebWebDriver (String browser, boolean headless) throws Exception {
  		switch (browser) {
              case ("chrome"):
            	  ChromeOptions cropts = new ChromeOptions ( );
              ChromeDriverService service = new ChromeDriverService.Builder()
                      .usingDriverExecutable(new File(Constants.DRIVER_DIRECTORY+"/chromedriver.exe"))
                      .usingAnyFreePort()
                      .build();
                  cropts = buildChromeOptions(false, headless);
                  webDriverThreadLocal.set ( new ChromeDriver (service, cropts ) );
                  break;
  		}}
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
    public void createAppiumDriver ( ) throws MalformedURLException {
    	
      //  if ( getAppiumDriver ( ) == null ) {
            if ( Arrays.toString ( this.environment.getActiveProfiles ( ) ).contains ( "jenkins" ) ) {
                log.info ( "Remote URL for Appium: " + "applicationProperties.getAppiumGridUrl ( )" );
                setRemoteDriverAppium ( "http://127.0.0.1:4444/wd/hub" );
            } else if(( Arrays.toString ( this.environment.getActiveProfiles ( ) ).contains ( "prod" ) )){
            	//may need to improve this conditioning
            	creatSaucelabInstance();
            }
            else {
                log.info ( "Local URL for Appium: " + applicationProperties.getAppiumGridUrl ( ) );
                setLocalDriverAppium ( "http://127.0.0.1:4444/wd/hub" );
            }
      //  }
    }
    public void creatSaucelabInstance() throws MalformedURLException {
        MutableCapabilities caps = new MutableCapabilities();
        caps.setCapability("platformName", "Android");
        caps.setCapability("app", "https://github.com/jesussalatiel/Appium-Cloud/raw/main/notepad.apk");
        caps.setCapability("appium:deviceName", "Google Pixel 3a GoogleAPI Emulator");
        caps.setCapability("appium:platformVersion", "11.0");
        MutableCapabilities sauceOptions = new MutableCapabilities();
        sauceOptions.setCapability("appiumVersion", "1.20.2");
        caps.setCapability("sauce:options", sauceOptions);
        log.info("Step 1 -> App Launched Successfully !!!");
        appiumDriverThreadLocal.set (new AndroidDriver(new URL(buildURL()), caps) );
    }
    public String buildURL() {
        String url      = "ondemand.saucelabs.com/wd/hub";
       // String auth     = "basic"; // todo none|basic|etc
        String username = "CTFSTest";//"setiteam";
        String password = "21dd0eae-99ff-476e-91d3-2099afd3525f";//"e04357af-5e6e-4a5f-b6c6-a503c9181813";
        return String.format("http://%s:%s@%s", username, password, url);
    }
    public void setLocalDriverAppium(String hubUrl) throws MalformedURLException {
        DesiredCapabilities capabilities = new DesiredCapabilities ( );
        capabilities.setCapability ( MobileCapabilityType.DEVICE_NAME , "emulator-5560" );
        capabilities.setCapability ( MobileCapabilityType.APP , "https://github.com/jesussalatiel/Appium-Cloud/raw/main/notepad.apk" );
        capabilities.setCapability ( MobileCapabilityType.AUTOMATION_NAME , AutomationName.ANDROID_UIAUTOMATOR2 );
        capabilities.setCapability ( MobileCapabilityType.PLATFORM_NAME, "Android" );
        /*Load Waits*/
        capabilities.setCapability ( SelenoidType.ANDROID_INSTALL_TIMEOUT, 180000 );
        capabilities.setCapability ( SelenoidType.ACCEPT_INSECURE_CERTS , true );
        capabilities.setCapability ( SelenoidType.APP_WAIT_DURATION , 60000 );
        capabilities.setCapability ( SelenoidType.AVD_LAUNCH_TIMEOUT , 180000 );
        capabilities.setCapability ( SelenoidType.AVD_READY_TIMEOUT , 180000 );
        capabilities.setCapability ( MobileCapabilityType.NEW_COMMAND_TIMEOUT , 180000 );

        /*Reset app or environment*/
        capabilities.setCapability ( MobileCapabilityType.NO_RESET , false );
        capabilities.setCapability ( MobileCapabilityType.FULL_RESET , false );

        // Create Remote Connection to Selenoid
        System.out.println("Creating Appium Driver Instance");
        appiumDriverThreadLocal.set ( new AppiumDriver <> ( new URL ( hubUrl ) , capabilities ) );
    System.out.println("Created Appium Driver Instance");
    }

    /*
     * This method is using to set Selenoid Capabilities
     * https://github.com/aerokube/selenoid/blob/master/docs/special-capabilities.adoc
     *
     * */
    public synchronized static void setRemoteDriverAppium (String hubUrl) throws MalformedURLException {

        DesiredCapabilities capabilities = new DesiredCapabilities ( );

        /*Define Android Skin and Activate VNC*/
        capabilities.setCapability ( SelenoidType.ENABLE_VNC , true );
        capabilities.setCapability ( SelenoidType.SKIN , "WXGA720" );
        capabilities.setCapability ( MobileCapabilityType.BROWSER_NAME , SelenoidValues.android );
        capabilities.setCapability ( MobileCapabilityType.PLATFORM_NAME, "Android" );

        /*Set Run Environment*/
        capabilities.setCapability ( SelenoidType.SCREEN_RESOLUTION , "1280x1024x24" );
        capabilities.setCapability ( SelenoidType.TIME_ZONE , "Europe/Moscow" );
        capabilities.setCapability ( MobileCapabilityType.PLATFORM_VERSION , "7" );
        capabilities.setCapability ( MobileCapabilityType.APP , "https://github.com/jesussalatiel/Appium-Cloud/raw/main/notepad.apk" );
        capabilities.setCapability ( MobileCapabilityType.AUTOMATION_NAME , AutomationName.ANDROID_UIAUTOMATOR2 );

        /*Load Waits*/
        capabilities.setCapability ( SelenoidType.ANDROID_INSTALL_TIMEOUT, 180000 );
        capabilities.setCapability ( SelenoidType.ACCEPT_INSECURE_CERTS , true );
        capabilities.setCapability ( SelenoidType.APP_WAIT_DURATION , 60000 );
        capabilities.setCapability ( SelenoidType.AVD_LAUNCH_TIMEOUT , 180000 );
        capabilities.setCapability ( SelenoidType.AVD_READY_TIMEOUT , 180000 );
        capabilities.setCapability ( MobileCapabilityType.NEW_COMMAND_TIMEOUT , 180000 );

        /*Reset app or environment*/
        capabilities.setCapability ( MobileCapabilityType.NO_RESET , false );
        capabilities.setCapability ( MobileCapabilityType.FULL_RESET , true );

        // Create Remote Connection to Selenoid
        appiumDriverThreadLocal.set ( new AndroidDriver <> ( new URL ( hubUrl ) , capabilities ) );
    }

    public synchronized   WebDriver getWebDriver ( ) {
        return webDriverThreadLocal.get ( );
    }
    public synchronized  AppiumDriver getAppiumDriver ( ) {
        return appiumDriverThreadLocal.get ( );
    }
}

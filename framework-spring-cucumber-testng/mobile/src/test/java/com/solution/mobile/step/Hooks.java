package com.solution.mobile.step;

import com.solution.common.utils.HookUtil;
import com.solution.mobile.config.AbstractTestDefinition;
import com.solution.mobile.utils.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.spring.CucumberContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.MalformedURLException;

@CucumberContextConfiguration
public class Hooks extends AbstractTestDefinition {

    @Autowired
    private HookUtil hookUtil;
    @Autowired
    private DriverManager driverManager;

    @Before
    public void beforeScenario(Scenario scenario) throws MalformedURLException {
        driverManager.createAppiumDriver ();
        hookUtil.startScenario ( scenario );
    }
@AfterStep
public void afterStep(Scenario scenario) {
	if(driverManager.getWebDriver()!=null) {
	    hookUtil.takeScreenshot ( scenario, driverManager.getWebDriver() );
	}else if (driverManager.getWebDriver()!=null && driverManager.getAppiumDriver()!=null ) {
		hookUtil.takeScreenshot ( scenario, driverManager.getWebDriver() );
		hookUtil.takeScreenshot ( scenario, driverManager.getAppiumDriver () );
	}
	else {
		hookUtil.takeScreenshot ( scenario, driverManager.getAppiumDriver () );
	}
	
    
}
    @After
    public void afterScenario(Scenario scenario) {
        
        hookUtil.endOfTest ( scenario, driverManager.getAppiumDriver () );
        if(driverManager.getAppiumDriver () != null){
            driverManager.getAppiumDriver ().quit ();
        }
        if(driverManager.getWebDriver () != null){
            driverManager.getWebDriver ().quit ();
        }
    }
}
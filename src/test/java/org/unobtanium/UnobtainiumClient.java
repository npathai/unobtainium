package org.unobtanium;

import org.junit.Assert;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

@RunWith(Unobtainium.class)
public class UnobtainiumClient {

  @BeforeScenario(name = "1_readPropertyFile")
  public TestStep readPropertyFile() {
    return new ImportPropertyFileConfigurationStep("system.properties");
  }

  @Step(name = "1_passingStep")
  public TestStep passingStep() {
    return new PassStep();
  }

  @Step(name = "2_saveConfigStep")
  public TestStep saveConfigStep() {
    return new PassAndSaveInjectStep();
  }

  @Step(name = "3_fetchConfigStep")
  public TestStep fetchConfigStep() {
    return new PassAndFetchInjectStep();
  }

  @Step(name = "4_failStep")
  public TestStep failStep() {
    return new FailStep();
  }

  private static class PassStep extends TestStep {
    @Override
    public void execute() {
      Assert.assertTrue(true);
    }
  }

  private static class FailStep extends TestStep {
    @Override
    public void execute() {
      Assert.fail("A legitimate failure");
    }
  }

  private static class PassAndSaveInjectStep extends TestStep {

    @Override
    void execute() throws StepFailureException {
      getGlobalContext().set("key", 100);
      getScenarioContext().set("key", 100);
      getStepContext().set("key", 100);
    }
  }

  private static class PassAndFetchInjectStep extends TestStep {

    @Inject(scope = Inject.Scope.SCENARIO, key = "key")
    private Integer scenarioVal;

    @Inject(scope = Inject.Scope.GLOBAL, key = "key")
    private Integer globalVal;

    @Inject(scope = Inject.Scope.SCENARIO, key = "home")
    private String home;

    @Override
    void execute() throws StepFailureException {

    }

    @Override
    void verify() {
      Assert.assertEquals("Scenario context get failed", Integer.valueOf(100), scenarioVal);
      Assert.assertEquals("Global context get failed", Integer.valueOf(100), globalVal);
      Assert.assertEquals("Global context get failed", "/path/to/home", home);
    }
  }

  private static class ImportPropertyFileConfigurationStep extends TestStep {

    private final String name;

    ImportPropertyFileConfigurationStep(String name) {
      this.name = name;
    }

    @Override
    void execute() throws StepFailureException {
      InputStream source = this.getClass().getClassLoader().getResourceAsStream(name);
      if (source == null) {
        throw new StepFailureException("Cannot find property file with name: " + name + " in classpath");
      }
      Properties properties = new Properties();
      try {
        properties.load(source);
        importProperties(properties);
      } catch (IOException e) {
        throw new StepFailureException(e);
      }
    }

    private void importProperties(Properties properties) {
      Enumeration<?> e = properties.propertyNames();
      while (e.hasMoreElements()) {
        String key = (String) e.nextElement();
        String value = properties.getProperty(key);
        getScenarioContext().set(key, value);
      }
    }
  }
}

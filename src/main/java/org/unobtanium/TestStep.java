package org.unobtanium;

public abstract class TestStep {
  private String name;
  private Context stepContext;
  private Context scenarioContext;
  private Context globalContext;

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Context getGlobalContext() {
    return globalContext;
  }

  public void setGlobalContext(Context globalContext) {
    this.globalContext = globalContext;
  }

  public Context getScenarioContext() {
    return scenarioContext;
  }

  public void setScenarioContext(Context scenarioContext) {
    this.scenarioContext = scenarioContext;
  }

  public Context getStepContext() {
    return stepContext;
  }

  public void setStepContext(Context stepContext) {
    this.stepContext = stepContext;
  }

  abstract void execute() throws StepFailureException;

  void preExecute() {

  }

  void postExecute() {

  }

  void verify() {

  }
}

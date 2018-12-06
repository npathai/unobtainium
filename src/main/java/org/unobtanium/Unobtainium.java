package org.unobtanium;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.AssumptionViolatedException;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class Unobtainium extends Runner {

  private Object testInstance;
  private TestClass testClass;
  private Description scenarioDescription;
  private final ConcurrentMap<String, Description> stepNameToDescription = new ConcurrentHashMap<>();
  private List<FrameworkStep> frameworkSteps;
  private List<TestStep> steps;
  private Context scenarioContext;
  private Context globalContext;
  private Map<String, FrameworkStep> stepNameToFrameworkStep;
  private List<FrameworkStep> beforeScenarioSteps;
  private List<TestStep> beforeScenarios;

  public Unobtainium(Class<?> testClass) throws Throwable {
    this.testClass = new TestClass(testClass);
    this.steps = new ArrayList<>();
    this.beforeScenarios = new ArrayList<>();
    this.scenarioContext = new Context();
    createTestInstance();
  }

  public void setGlobalContext(Context globalContext) {
    this.globalContext = globalContext;
  }

  private void createTestInstance() throws Throwable {
    this.testInstance = new ReflectiveCallable() {
      @Override
      protected Object runReflectiveCall() throws Throwable {
        return testClass.getOnlyConstructor().newInstance();
      }
    }.run();

    frameworkSteps = this.testClass.getAnnotatedMethods(Step.class)
        .stream()
        .map(method -> new FrameworkStep(method.getMethod(), method.getAnnotation(Step.class).name()))
        .sorted(Comparator.comparing(method -> method.getAnnotation(Step.class).name()))
        .collect(Collectors.toList());

    stepNameToFrameworkStep = this.frameworkSteps.stream()
        .collect(Collectors.toMap(step -> step.stepName(), step -> step));

    beforeScenarioSteps = this.testClass.getAnnotatedMethods(BeforeScenario.class)
        .stream()
        .map(method -> new FrameworkStep(method.getMethod(), method.getAnnotation(BeforeScenario.class).name()))
        .sorted(Comparator.comparing(method -> method.getAnnotation(BeforeScenario.class).name()))
        .collect(Collectors.toList());

    createStepDescriptions();
  }

  private TestStep createStep(FrameworkStep frameworkStep) throws Throwable {
    TestStep step = (TestStep) new ReflectiveCallable() {

      @Override
      protected Object runReflectiveCall() throws Throwable {
        return frameworkStep.getMethod().invoke(testInstance, null);
      }
    }.run();
    step.setName(frameworkStep.stepName());
    step.setGlobalContext(globalContext);
    step.setScenarioContext(scenarioContext);
    step.setStepContext(new Context());
    return step;
  }

  private void createStepDescriptions() {
    for (FrameworkStep frameworkStep : frameworkSteps) {
      createStepDescription(frameworkStep);
    }
  }

  @Override
  public Description getDescription() {
    if (scenarioDescription != null) {
      return scenarioDescription;
    }

    scenarioDescription = Description.createTestDescription(this.testClass.getJavaClass(), testClass.getName());

    for (FrameworkStep step: frameworkSteps) {
      scenarioDescription.addChild(Description.createTestDescription(testClass.getJavaClass(),
          step.stepName()));
    }
    return scenarioDescription;
  }

  @Override
  public void run(RunNotifier notifier) {
    try {
      for (FrameworkStep beforeScenarioStep : beforeScenarioSteps) {
        TestStep step = createStep(beforeScenarioStep);
        this.beforeScenarios.add(step);
      }

      for (FrameworkStep frameworkStep : frameworkSteps) {
        TestStep step = createStep(frameworkStep);
        this.steps.add(step);
      }
    } catch (Throwable ex) {
      throw new AssertionError(ex);
    }

    notifier.fireTestSuiteStarted(getDescription());
    runBeforeScenarios();
    for (TestStep step: steps) {
      notifier.fireTestStarted(stepNameToDescription.get(step.getName()));
      try {
        injectConfigsInto(step);
        runStep(step);
        step.verify();
      } catch (StepFailureException | AssertionError | AssumptionViolatedException e) {
        notifier.fireTestFailure(new Failure(stepNameToDescription.get(step.getName()), e));
        break; // Stop scenario if any step fails
      } finally {
        notifier.fireTestFinished(stepNameToDescription.get(step.getName()));
      }
    }
    notifier.fireTestSuiteFinished(getDescription());
  }

  private void runStep(TestStep step) throws StepFailureException {
    step.preExecute();
    step.execute();
    step.postExecute();
  }

  private void runBeforeScenarios() {
    for (TestStep beforeScenarioStep : beforeScenarios) {
      try {
        injectConfigsInto(beforeScenarioStep);
        runStep(beforeScenarioStep);
      } catch (StepFailureException e) {
        throw new AssertionError(e);
      }
    }
  }

  private void injectConfigsInto(TestStep step) throws StepFailureException {
    List<FrameworkStepConfigField> configFields = getConfigFields(step);
    for (FrameworkStepConfigField configField : configFields) {
      Object configVal = null;
      switch (configField.config.scope()) {
        case GLOBAL:
          configVal = globalContext.get(configField.config.key());
          break;
        case SCENARIO:
          configVal = scenarioContext.get(configField.config.key());
          break;
        case STEP:
          step.getStepContext().get(configField.config.key());
          break;
        default:
          throw new NullPointerException("Scope should not be null");
      }
      if (configVal == null) {
        throw new StepFailureException("Configuration with key: " + configField.config.key() + " not found in scope: "
            + configField.config.scope());
      }
      try {
        configField.set(step, configVal);
      } catch (Throwable ex) {
        throw new StepFailureException(ex);
      }
    }
  }

  private List<FrameworkStepConfigField> getConfigFields(TestStep step) {
    return FieldUtils.getFieldsListWithAnnotation(step.getClass(), Inject.class)
        .stream()
        .map(field -> new FrameworkStepConfigField(field, field.getAnnotation(Inject.class)))
        .collect(Collectors.toList());

  }

  private void createStepDescription(FrameworkStep step) {
    Description description = stepNameToDescription.get(step.stepName());

    if (description == null) {
      description = Description.createTestDescription(this.testClass.getJavaClass(),
          step.stepName(), null);
      stepNameToDescription.putIfAbsent(step.stepName(), description);
    }
  }

  private class FrameworkStep extends FrameworkMethod {

    private String name;
    /**
     * Returns a new {@code FrameworkMethod} for {@code method}
     *
     * @param method
     */
    public FrameworkStep(Method method, String name) {
      super(method);
      this.name = name;
    }

    public String stepName() {
      return name;
    }
  }

  private class FrameworkStepConfigField {
    private Field field;
    private Inject config;

    FrameworkStepConfigField(Field field, Inject config) {
      this.field = field;
      this.config = config;
    }

    public void set(Object step, Object finalConfigVal) throws Throwable {
      new ReflectiveCallable() {

        @Override
        protected Object runReflectiveCall() throws Throwable {
          field.setAccessible(true);
          field.set(step, finalConfigVal);
          return null;
        }
      }.run();
    }
  }
}

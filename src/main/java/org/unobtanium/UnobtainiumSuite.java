package org.unobtanium;

import org.junit.internal.builders.AnnotatedBuilder;
import org.junit.internal.builders.NullBuilder;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class UnobtainiumSuite extends Suite {

  public UnobtainiumSuite(Class<?> klass) throws InitializationError {
    super(klass, new UnobtainiumRunnerBuilder(new NullBuilder()));
  }

  private static class UnobtainiumRunnerBuilder extends AnnotatedBuilder {
    private Context globalContext = new Context();

    public UnobtainiumRunnerBuilder(RunnerBuilder suiteBuilder) {
      super(suiteBuilder);
    }

    @Override
    public Runner runnerForClass(Class<?> testClass) throws Exception {
      Runner runner = super.runnerForClass(testClass);
      if (runner instanceof Unobtainium) {
        ((Unobtainium)runner).setGlobalContext(globalContext);
      }
      return runner;
    }
  }
}

package org.unobtanium;

import org.junit.internal.builders.AnnotatedBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.TestClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;

public class UnobtainiumSuite extends Suite {


  public UnobtainiumSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
    super(klass, builder);
  }

  @Override
  public Description getDescription() {
    return null;
  }

  @Override
  public void run(RunNotifier notifier) {

  }

//  private static class UnobtainiumRunnerBuilder extends RunnerBuilder {
//
//    @Override
//    public Runner runnerForClass(Class<?> testClass) throws Throwable {
//      return new UnobtainiumSuite(testClass);
//    }
//  }
}

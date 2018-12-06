package org.unobtanium;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;

public class UnobtainiumTest {

  @RunWith(Unobtainium.class)
  public static class SingleStepTest {

    @Step(name = "1_PassingStep")
    public TestStep passingStep() {
      return new PassStep();
    }
  }

  private static class PassStep extends TestStep {
    @Override
    public void execute() {
      Assert.assertTrue(true);
    }
  }

  @Test
  public void singleStepTest() {
    JUnitCore jUnitCore = new JUnitCore();
  }
}

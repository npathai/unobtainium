package org.unobtanium;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {

  Scope scope() default Scope.SCENARIO;
  String key();

  enum Scope {
    GLOBAL,
    SCENARIO,
    STEP
  }
}

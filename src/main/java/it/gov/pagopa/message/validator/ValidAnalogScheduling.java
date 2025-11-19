package it.gov.pagopa.message.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidatorAnalogScheduling.class)
public @interface ValidAnalogScheduling {

  String message() default "Invalid combination of workflowType and analogSchedulingDate";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}

package it.gov.pagopa.message.validator;

import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.enums.WorkflowType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidatorAnalogScheduling implements
    ConstraintValidator<ValidAnalogScheduling, MessageDTO> {

  @Override
  public boolean isValid(MessageDTO messageDTO, ConstraintValidatorContext context) {

    WorkflowType workflowType = messageDTO.getWorkflowType();
    String analogSchedulingDate = messageDTO.getAnalogSchedulingDate();

    if (WorkflowType.ANALOG == workflowType
        && (analogSchedulingDate == null || analogSchedulingDate.isEmpty())) {

      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
              "analogSchedulingDate is required when workflowType is ANALOG")
          .addPropertyNode("analogSchedulingDate")
          .addConstraintViolation();
      return false;
    }

    if (WorkflowType.DIGITAL == workflowType
        && analogSchedulingDate != null
        && !analogSchedulingDate.isEmpty()) {

      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
              "analogSchedulingDate must be null or empty when workflowType is DIGITAL")
          .addPropertyNode("analogSchedulingDate")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
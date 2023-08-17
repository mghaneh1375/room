package bogen.studio.Room.Validator;

import bogen.studio.Room.DTO.BoomDTO;
import org.json.JSONObject;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class BoomValidator implements ConstraintValidator<ValidatedBoom, BoomDTO> {

    @Override
    public void initialize(ValidatedBoom constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(BoomDTO value, ConstraintValidatorContext context) {

        boolean isErrored = false;
        JSONObject errs = new JSONObject();

        if (value.getUserId() == null) {
            errs.put("userId", "لطفا آی دی کاربر را وارد نمایید");
            isErrored = true;
        }

        if (value.getBusinessId() == null) {
            errs.put("businessId", "لطفا آی دی بیزینس را وارد نمایید");
            isErrored = true;
        }

        if (value.getPlaceId() == null) {
            errs.put("businessId", "لطفا آی دی بوم گردی را وارد نمایید");
            isErrored = true;
        }

        if(isErrored) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errs.toString()).addConstraintViolation();
        }

        return !isErrored;
    }

}

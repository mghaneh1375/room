package bogen.studio.Room.Validator;

import bogen.studio.Room.DTO.BoomData;
import org.json.JSONObject;
import org.springframework.util.ObjectUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class BoomValidator implements ConstraintValidator<ValidatedBoom, BoomData> {

    @Override
    public void initialize(ValidatedBoom constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(BoomData value, ConstraintValidatorContext context) {

        boolean isErrored = false;
        JSONObject errs = new JSONObject();

        if (ObjectUtils.isEmpty(value.getTitle())) {
            errs.put("name", "لطفا نام بوم گردی را وارد نمایید");
            isErrored = true;
        }
        if(isErrored) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errs.toString()).addConstraintViolation();
        }

        return !isErrored;
    }

}

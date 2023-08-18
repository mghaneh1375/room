package bogen.studio.Room.Validator;

import bogen.studio.Room.DTO.DatePrice;
import org.json.JSONObject;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DatePriceValidator implements ConstraintValidator<ValidatedDatePrice, DatePrice> {

    @Override
    public void initialize(ValidatedDatePrice constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(DatePrice value, ConstraintValidatorContext context) {

        boolean isErrored = false;
        JSONObject errs = new JSONObject();

        if (value.getDate() == null || !DateValidator.isValid(value.getDate())) {
            errs.put("date", "لطفا تاریخ را وارد نمایید");
            isErrored = true;
        }

        if (value.getPrice() == null || value.getPrice() < 0) {
            errs.put("price", "لطفا قیمت را وارد نمایید");
            isErrored = true;
        }

        if(isErrored) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errs.toString()).addConstraintViolation();
        }

        return !isErrored;
    }

}

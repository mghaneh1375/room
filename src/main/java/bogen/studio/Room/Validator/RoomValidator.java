package bogen.studio.Room.Validator;

import bogen.studio.Room.DTO.RoomData;
import bogen.studio.Room.Enums.Limitation;
import org.json.JSONObject;
import org.springframework.util.ObjectUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RoomValidator implements ConstraintValidator<ValidatedRoom, RoomData> {

    @Override
    public void initialize(ValidatedRoom constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(RoomData value, ConstraintValidatorContext context) {

        boolean isErrored = false;
        JSONObject errs = new JSONObject();

        if (ObjectUtils.isEmpty(value.getTitle())) {
            errs.put("name", "لطفا نام اتاق را وارد نمایید");
            isErrored = true;
        }

        if(value.getLimitations() != null && value.getLimitations().size() > 0) {

            for(String itr : value.getLimitations()) {
                if(!EnumValidatorImp.isValid(itr, Limitation.class)) {
                    errs.put("name", "محدودیت وارد شده نامعتبر است");
                    isErrored = true;
                    break;
                }
            }

        }


        if(isErrored) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errs.toString()).addConstraintViolation();
        }

        return !isErrored;
    }

}

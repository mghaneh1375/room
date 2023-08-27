package bogen.studio.Room.Validator;

import bogen.studio.Room.DTO.RoomDTO;
import bogen.studio.Room.Enums.*;
import org.json.JSONObject;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class RoomValidator implements ConstraintValidator<ValidatedRoom, RoomDTO> {

    @Override
    public void initialize(ValidatedRoom constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    private boolean validateEnumArray(List<String> values, Class c) {

        if(values != null && values.size() > 0) {

            for(String itr : values) {
                if(!EnumValidatorImp.isValid(itr, c))
                    return false;
            }

        }

        return true;
    }

    @Override
    public boolean isValid(RoomDTO value, ConstraintValidatorContext context) {

        boolean isErrored = false;
        JSONObject errs = new JSONObject();

        //mandatory fields

        if (value.getTitle() == null || value.getTitle().length() < 3) {
            errs.put("title", "لطفا نام اتاق را وارد نمایید");
            isErrored = true;
        }

        if(value.getCap() == null || value.getCap() < 0) {
            errs.put("cap", "لطفا ظرفیت اتاق را وارد نمایید");
            isErrored = true;
        }

        if(value.getMaxCap() == null || value.getMaxCap() < 0) {
            errs.put("maxCap", "لطفا حداکثر ظرفیت اتاق را وارد نمایید");
            isErrored = true;
        }

        if(value.getPrice() == null ||  value.getPrice() < 0) {
            errs.put("price", "لطفا هزینه پایه اتاق را وارد نمایید");
            isErrored = true;
        }

        if(value.getCapPrice() == null || value.getCapPrice() < 0) {
            errs.put("capPrice", "هزینه ظرفیت اتاق نامعتبر است");
            isErrored = true;
        }

        if (value.getCount() != null && value.getCount() < 0) {
            errs.put("count", "تعداد اتاق معتبر نمی باشد");
            isErrored = true;
        }

        // optional fields

        if(value.getWeekendPrice() != null && value.getWeekendPrice() < 0) {
            errs.put("weekendPrice", "هزینه آخر هفته اتاق نامعتبر است");
            isErrored = true;
        }

        if(value.getVacationPrice() != null && value.getVacationPrice() < 0) {
            errs.put("vacationPrice", "هزینه تعطیلات اتاق نامعبتر است");
            isErrored = true;
        }

        if(value.getWeekendCapPrice() != null && value.getWeekendCapPrice() < 0) {
            errs.put("weekendCapPrice", "هزینه نفر اضافه در آخر هفته اتاق نامعتبر است");
            isErrored = true;
        }

        if(value.getVacationCapPrice() != null && value.getVacationCapPrice() < 0) {
            errs.put("vacationCapPrice", "هزینه نفر اضافه در تعطیلات اتاق نامعبتر است");
            isErrored = true;
        }

        if(value.getLimitations() == null || !validateEnumArray(value.getLimitations(), Limitation.class)) {
            errs.put("limitations", "محدودیت وارد شده نامعتبر است");
            isErrored = true;
        }

        if(value.getFoodFacilities() == null || !validateEnumArray(value.getFoodFacilities(), FoodFacility.class)) {
            errs.put("foodFacilities", "امکانات غذایی وارد شده نامعتبر است");
            isErrored = true;
        }

        if(value.getSleepFeatures() == null || !validateEnumArray(value.getSleepFeatures(), SleepFeature.class)) {
            errs.put("sleepFeatures", "وضعیت خواب وارد شده نامعتبر است");
            isErrored = true;
        }

        if(value.getWelfares() == null || !validateEnumArray(value.getWelfares(), Welfare.class)) {
            errs.put("welfares", "امکانات رفاهی وارد شده نامعتبر است");
            isErrored = true;
        }

        if(value.getAdditionalFacilities() == null || !validateEnumArray(value.getAdditionalFacilities(), AdditionalFacility.class)) {
            errs.put("additionalFacilities", "امکانات جانبی وارد شده نامعتبر است");
            isErrored = true;
        }

        if(value.getAccessibilityFeatures() == null || !validateEnumArray(value.getAccessibilityFeatures(), AccessibilityFeature.class)) {
            errs.put("accessibilityFeatures", "وضعیت دسترس پذیری وارد شده نامعتبر است");
            isErrored = true;
        }

        if(isErrored) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errs.toString()).addConstraintViolation();
        }

        return !isErrored;
    }

}

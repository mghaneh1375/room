package bogen.studio.Room.Validator;

import bogen.studio.Room.DTO.TripRequestDTO;
import my.common.commonkoochita.Validator.DateValidator;
import org.json.JSONObject;
import org.springframework.util.ObjectUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static my.common.commonkoochita.Utility.Utility.*;

public class TripRequestValidator implements ConstraintValidator<ValidatedTripRequest, TripRequestDTO> {

    @Override
    public void initialize(ValidatedTripRequest constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(TripRequestDTO value, ConstraintValidatorContext context) {

        boolean isErrored = false;
        JSONObject errs = new JSONObject();

        if (ObjectUtils.isEmpty(value.getStartDate()) || !DateValidator.isValid(value.getStartDate())) {
            errs.put("startDate", "لطفا تاریخ شروع اقامت خود را وارد نمایید");
            isErrored = true;
        }

        int d = convertStringToDate(value.getStartDate());
        int today = convertStringToDate(getToday("/"));

        if(d < today) {
            errs.put("startDate", "تاریخ شروع باید از امروز بزرگتر باشد");
            isErrored = true;
        }

        int futureLimit = convertStringToDate(getPast("/", -60));

        if(futureLimit < d) {
            errs.put("startDate", "امکان رزرو تاریخ مورد نظر هنوز باز نشده است");
            isErrored = true;
        }

        if(value.getAdults() == null || value.getAdults() < 1) {
            errs.put("passengers", "لطفا تعداد مسافران را وارد نمایید");
            isErrored = true;
        }

        if(value.getNights() == null) {
            errs.put("nights", "لطفا تعداد شب اقامت را وارد نمایید");
            isErrored = true;
        }

        if(value.getInfants() != null && value.getInfants() < 0) {
            errs.put("infants", "تعداد نوزاد معتبر نمی باشد");
            isErrored = true;
        }

        if(value.getChildren() != null && value.getChildren() < 0) {
            errs.put("children", "تعداد خردسال معتبر نمی باشد");
            isErrored = true;
        }

        if(isErrored) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errs.toString()).addConstraintViolation();
        }

        return !isErrored;
    }

}

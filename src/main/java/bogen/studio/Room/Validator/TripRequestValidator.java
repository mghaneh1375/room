package bogen.studio.Room.Validator;

import bogen.studio.Room.DTO.TripRequestDTO;
import bogen.studio.Room.Utility.Utility;
import org.json.JSONObject;
import org.springframework.util.ObjectUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

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

        int d = Utility.convertStringToDate(value.getStartDate());
        int today = Utility.convertStringToDate(Utility.getToday("/"));

        if(d < today) {
            errs.put("startDate", "تاریخ شروع باید از امروز بزرگتر باشد");
            isErrored = true;
        }

        int futureLimit = Utility.convertStringToDate(Utility.getPast("/", -60));

        if(futureLimit < d) {
            errs.put("startDate", "امکان رزرو تاریخ مورد نظر هنوز باز نشده است");
            isErrored = true;
        }

        if(value.getPassengers() == null || value.getPassengers() < 1) {
            errs.put("passengers", "لطفا تعداد مسافران را وارد نمایید");
            isErrored = true;
        }

        if(value.getNights() == null) {
            errs.put("nights", "لطفا تعداد شب اقامت را وارد نمایید");
            isErrored = true;
        }

        if(value.getInfants() != null && value.getInfants() < 0) {
            errs.put("infants", "تعداد خردسال معتبر نمی باشد");
            isErrored = true;
        }

        if(isErrored) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errs.toString()).addConstraintViolation();
        }

        return !isErrored;
    }

}

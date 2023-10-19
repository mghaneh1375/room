package bogen.studio.Room.Validator;

import bogen.studio.Room.DTO.ReservationRequestDTO;
import my.common.commonkoochita.Validator.DateValidator;
import org.json.JSONObject;
import org.springframework.util.ObjectUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static my.common.commonkoochita.Utility.Utility.*;

public class ReservationRequestValidator implements ConstraintValidator<ValidatedReservationRequest, ReservationRequestDTO> {

    @Override
    public void initialize(ValidatedReservationRequest constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(ReservationRequestDTO value, ConstraintValidatorContext context) {

        boolean isErrored = false;
        JSONObject errs = new JSONObject();

        if (value.getStartDate() == null ||
                ObjectUtils.isEmpty(value.getStartDate()) ||
                !DateValidator.isValid(value.getStartDate())
        ) {
            errs.put("startDate", "لطفا تاریخ شروع اقامت خود را وارد نمایید");
            isErrored = true;
        }

        if(!isErrored) {

            int d = convertStringToDate(value.getStartDate());
            int today = convertStringToDate(getToday("/"));

            if (d < today) {
                errs.put("startDate", "تاریخ شروع باید از امروز بزرگتر باشد");
                isErrored = true;
            }

            int futureLimit = convertStringToDate(getPast("/", -60));

            if (futureLimit < d) {
                errs.put("startDate", "امکان رزرو تاریخ مورد نظر هنوز باز نشده است");
                isErrored = true;
            }

        }

        if(value.getNights() == null || value.getNights() < 0) {
            errs.put("nights", "لطفا تعداد شب اقامت را وارد نمایید");
            isErrored = true;
        }

//        if(value.getAdults() == null || value.getAdults() < 1) {
//            errs.put("adults", "لطفا تعداد مسافران را وارد نمایید");
//            isErrored = true;
//        }
//
//        if(value.getInfants() != null && value.getInfants() < 0) {
//            errs.put("infants", "تعداد خردسال معتبر نمی باشد");
//            isErrored = true;
//        }
//
//        if(value.getChildren() != null && value.getChildren() < 0) {
//            errs.put("children", "تعداد کودک معتبر نمی باشد");
//            isErrored = true;
//        }

        if(value.getPassengersId() == null) {
            errs.put("passengersId", "لطفا آی دی مسافران را وارد نمایید");
            isErrored = true;
        }

        if(isErrored) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errs.toString()).addConstraintViolation();
        }

        return !isErrored;
    }

}

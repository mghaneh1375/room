package bogen.studio.Room.Validator;

import bogen.studio.Room.DTO.ReservationRequestDTO;
import bogen.studio.Room.DTO.RoomDTO;
import bogen.studio.Room.Enums.FoodFacility;
import bogen.studio.Room.Enums.Limitation;
import org.json.JSONObject;
import org.springframework.util.ObjectUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ReservationRequestValidator implements ConstraintValidator<ValidatedReservationRequest, ReservationRequestDTO> {

    @Override
    public void initialize(ValidatedReservationRequest constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(ReservationRequestDTO value, ConstraintValidatorContext context) {

        boolean isErrored = false;
        JSONObject errs = new JSONObject();

        if (ObjectUtils.isEmpty(value.getStartDate()) || !DateValidator.isValid(value.getStartDate())) {
            errs.put("name", "لطفا تاریخ شروع اقامت خود را وارد نمایید");
            isErrored = true;
        }

        if(value.getPassengers() == null || value.getPassengers() < 1) {
            errs.put("price", "لطفا تعداد مسافران را وارد نمایید");
            isErrored = true;
        }

        if(value.getNights() == null) {
            errs.put("capPrice", "لطفا تعداد شب اقامت را وارد نمایید");
            isErrored = true;
        }

        if(isErrored) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errs.toString()).addConstraintViolation();
        }

        return !isErrored;
    }

}

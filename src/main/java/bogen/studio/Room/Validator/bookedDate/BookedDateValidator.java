package bogen.studio.Room.Validator.bookedDate;

import bogen.studio.Room.Exception.InvalidInputException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

public class BookedDateValidator implements ConstraintValidator<ValidBookedDate, LocalDateTime> {
    @Override
    public void initialize(ValidBookedDate constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(LocalDateTime bookedDate, ConstraintValidatorContext constraintValidatorContext) {

        if (bookedDate == null) {
            return true;
        }

        boolean hasError = false;
        StringBuffer sb = new StringBuffer();



        if (bookedDate.get(ChronoField.HOUR_OF_DAY) !=0 ) {
            hasError = true;
            sb.append("ساعت باید صفر باشد");
            sb.append("\n");
        }

        if (bookedDate.get(ChronoField.MINUTE_OF_HOUR) !=0 ) {
            hasError = true;
            sb.append("دقیقه باید صفر باشد");
            sb.append("\n");
        }

        if (bookedDate.get(ChronoField.SECOND_OF_MINUTE) !=0 ) {
            hasError = true;
            sb.append("ثانیه باید صفر باشد");
            sb.append("\n");
        }

        if (bookedDate.get(ChronoField.NANO_OF_SECOND) !=0 ) {
            hasError = true;
            sb.append("میکرو ثانیه باید صفر باشد");
            sb.append("\n");
        }


        if (hasError) {
            throw new InvalidInputException(sb.toString());
        }

        return true;
    }
}

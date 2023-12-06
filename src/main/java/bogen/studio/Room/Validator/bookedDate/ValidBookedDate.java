package bogen.studio.Room.Validator.bookedDate;

import javax.validation.Constraint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = BookedDateValidator.class)
@Target({ElementType.PARAMETER})
@Retention(RUNTIME)
public @interface ValidBookedDate {
    String message() default "Invalid date";

    Class[] groups() default {};

    Class[] payload() default {};

}

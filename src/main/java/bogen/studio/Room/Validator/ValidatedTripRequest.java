package bogen.studio.Room.Validator;

import javax.validation.Constraint;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = TripRequestValidator.class)
@Target({TYPE})
@Retention(RUNTIME)
public @interface ValidatedTripRequest {

    String message() default "";

    Class[] groups() default {};

    Class[] payload() default {};

}

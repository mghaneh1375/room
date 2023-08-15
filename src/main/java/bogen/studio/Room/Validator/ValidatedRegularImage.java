package bogen.studio.Room.Validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RegularImageValidator.class)
@Target( { ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidatedRegularImage {
    String message() default "Invalid Image";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

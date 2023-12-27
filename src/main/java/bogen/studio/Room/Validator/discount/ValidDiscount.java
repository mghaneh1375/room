package bogen.studio.Room.Validator.discount;

import javax.validation.Constraint;
import java.lang.annotation.*;

@Constraint(validatedBy = DiscountValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidDiscount {

    String message() default "Invalid DiscountPostDto";

    Class[] groups() default {};

    Class[] payload() default {};

}

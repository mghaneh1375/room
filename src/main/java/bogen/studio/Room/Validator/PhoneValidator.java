package bogen.studio.Room.Validator;

import bogen.studio.Room.Utility.Utility;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PhoneValidator implements
        ConstraintValidator<PhoneConstraint, String> {

    private static final Pattern phonePattern = Pattern.compile("^(09)\\d{9}$");

    public static boolean isValid(String s) {
        return phonePattern.matcher(Utility.convertPersianDigits(s)).matches();
    }

    @Override
    public void initialize(PhoneConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(String in, ConstraintValidatorContext constraintValidatorContext) {
        return phonePattern.matcher(Utility.convertPersianDigits(in)).matches();
    }
}

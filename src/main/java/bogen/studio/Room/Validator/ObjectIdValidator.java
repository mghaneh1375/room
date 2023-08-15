package bogen.studio.Room.Validator;

import org.bson.types.ObjectId;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class ObjectIdValidator implements
        ConstraintValidator<ObjectIdConstraint, ObjectId> {


    @Override
    public void initialize(ObjectIdConstraint constraintAnnotation) {

    }

    @Override
    public boolean isValid(ObjectId o, ConstraintValidatorContext constraintValidatorContext) {
        return ObjectId.isValid(o.toString());
    }

    public static boolean isValid(String str) {
        return ObjectId.isValid(str);
    }
}

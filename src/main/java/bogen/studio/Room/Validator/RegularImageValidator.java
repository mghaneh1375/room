package bogen.studio.Room.Validator;

import bogen.studio.Room.Exception.InvalidFileTypeException;
import bogen.studio.Room.Utility.FileUtils;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.Objects;

import static bogen.studio.Room.Utility.StaticValues.MAX_FILE_SIZE;


public class RegularImageValidator implements
        ConstraintValidator<ValidatedRegularImage, MultipartFile> {


    @Override
    public void initialize(ValidatedRegularImage constraintAnnotation) {

    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {

        boolean isErrored = false;
        JSONObject errs = new JSONObject();

        if(file == null) {
            isErrored = true;
            errs.put("max_length_err", "لطفا تصویر موردنظر خود را بارگذاری فرمایید.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            isErrored = true;
            errs.put("max_length_err", "حداکثر حجم مجاز، 5 مگ است.");
        }


        try {

            String fileType = (String) FileUtils.getFileType(Objects.requireNonNull(file.getOriginalFilename())).getKey();

            if(!fileType.equals("image")) {
                isErrored = true;
                errs.put("file_type_err", "تنها مجاز به بارگذاری تصویر هستید.");
            }


        } catch (InvalidFileTypeException e) {
            isErrored = true;
            errs.put("unknown_err", "خطا در بارگذاری");
        }

        if(isErrored) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errs.toString()).addConstraintViolation();
        }

        return !isErrored;
    }
}

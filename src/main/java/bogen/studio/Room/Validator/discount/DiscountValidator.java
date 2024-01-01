package bogen.studio.Room.Validator.discount;

import bogen.studio.Room.DTO.DiscountPostDto;
import bogen.studio.Room.Exception.InvalidInputException;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

import static bogen.studio.Room.Enums.DiscountExecution.AMOUNT;
import static bogen.studio.Room.Enums.DiscountExecution.PERCENTAGE;
import static bogen.studio.Room.Enums.DiscountPlace.BOOM_DISCOUNT;
import static bogen.studio.Room.Enums.DiscountPlace.ROOM_DISCOUNT;
import static bogen.studio.Room.Enums.DiscountType.*;
import static bogen.studio.Room.Utility.TimeUtility.convertStringToLdt;

@Slf4j
public class DiscountValidator implements ConstraintValidator<ValidDiscount, DiscountPostDto> {
    /* This class is a beast. Back then, This was the best thing that I could develop.
     * If you have a better solution, please let me know 😊 */
    String datePattern = "yyyy-MM-dd'T'HH:mm:ss";

    @Override
    public void initialize(ValidDiscount constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(DiscountPostDto dto, ConstraintValidatorContext constraintValidatorContext) {

        StringBuffer sb = new StringBuffer();

        boolean discountPlaceHasError = validateDiscountPlace(dto, sb);
        boolean discountPlaceInfoHasError = validateDiscountPlaceInfo(dto, sb);
        boolean discountTypeHasError = validateDiscountType(dto, sb);
        boolean generalDiscountHasError = validateGeneralDiscount(dto, sb);
        boolean lastMinuteDiscountHasError = validateLastMinuteDiscount(dto, sb);
        boolean codeDiscountHasError = validateCodeDiscount(dto, sb);

        if (discountPlaceHasError ||
                discountPlaceInfoHasError ||
                discountTypeHasError ||
                generalDiscountHasError ||
                lastMinuteDiscountHasError ||
                codeDiscountHasError
        ) {
            String errorMsgForLog = sb.toString().replace("\n", "--");
            log.warn("DiscountPostDto validator found an error: " + errorMsgForLog);
            throw new InvalidInputException(sb.toString());
        }

        return true;

    }

    private boolean validateCodeDiscount(DiscountPostDto dto, StringBuffer sb) {

        boolean hasError = false;

        if (dto.getDiscountType().equals(CODE.toString()) &&
                (dto.getGeneralDiscountPostDto() != null || dto.getLastMinuteDiscountPostDto() != null)
        ) {
            sb.append("در حالت کد تخفیف، تخفیف عمومی و لحظه آخری باید تهی باشند");
            sb.append("\n");
            hasError = true;
        } else if (dto.getDiscountType().equals(CODE.toString()) && dto.getCodeDiscountPostDto() == null) {
            sb.append("کد تخفیف تهی است");
            sb.append("\n");
            hasError = true;
        } else if (dto.getDiscountType().equals(CODE.toString()) && dto.getCodeDiscountPostDto() != null) {

            if (doesDiscountExecutionPercentAndAmountHaveError(
                    dto.getCodeDiscountPostDto().getDiscountExecution(),
                    dto.getCodeDiscountPostDto().getAmount(),
                    dto.getCodeDiscountPostDto().getPercent(),
                    null,
                    sb
            )) {
                hasError = true;
            }

            if (doesCodeHasError(dto.getCodeDiscountPostDto().getCode(), sb)) {
                hasError = true;
            }

            if (doesDefinedUsageCountHasError(dto.getCodeDiscountPostDto().getDefinedUsageCount(), sb)) {
                hasError = true;
            }

            if (doesLifeTimeRangeAndTargetDateRangeHaveError(
                    dto.getCodeDiscountPostDto().getLifeTimeStart(),
                    dto.getCodeDiscountPostDto().getLifeTimeEnd(),
                    dto.getCodeDiscountPostDto().getTargetDateStart(),
                    dto.getCodeDiscountPostDto().getTargetDateEnd(),
                    sb
            )) {
                hasError = true;
            }

            if (doesOrderOfDatesForGeneralDiscountHaveError(
                    dto.getCodeDiscountPostDto().getLifeTimeStart(),
                    dto.getCodeDiscountPostDto().getLifeTimeEnd(),
                    dto.getCodeDiscountPostDto().getTargetDateStart(),
                    dto.getCodeDiscountPostDto().getTargetDateEnd(),
                    sb
            )) {
                hasError = true;
            }
        }

        return hasError;
    }

    private boolean doesDefinedUsageCountHasError(int definedUsageCount, StringBuffer sb) {

        if (definedUsageCount <= 0) {
            sb.append("تعداد مصرف کد تخفیف باید بزرگتر از صفر باشد");
            sb.append("\n");
            return true;
        }

        return false;
    }

    private boolean doesCodeHasError(String code, StringBuffer sb) {

        boolean haseError = false;

        if (code == null) {
            sb.append("کد تهی است");
            sb.append("\n");
            haseError = true;
        } else if (code.length() == 0) {
            sb.append("کد خالی است");
            sb.append("\n");
            haseError = true;
        } else if (!code.matches("^[A-Za-z0-9]+$")) {
            sb.append("کد فقط میتواند شامل حروف انگلیسی و اعداد باشد");
            sb.append("\n");
            haseError = true;
        }
        return haseError;
    }

    private boolean validateLastMinuteDiscount(DiscountPostDto dto, StringBuffer sb) {

        boolean hasError = false;

        if (
                dto.getDiscountType().equals(LAST_MINUTE.toString()) &&
                        dto.getLastMinuteDiscountPostDto() == null
        ) {
            hasError = true;
            sb.append("تخفیف لحظه آخری تهی است");
            sb.append("\n");
        } else if (
                dto.getDiscountType().equals(LAST_MINUTE.toString()) &&
                        (dto.getGeneralDiscountPostDto() != null || dto.getCodeDiscountPostDto() != null)
        ) {
            hasError = true;
            sb.append("در حالت تخفیف لحظه آخری، تخفیف های عمومی و کد باید تهی باشند");
            sb.append("\n");
        } else if (dto.getDiscountType().equals(LAST_MINUTE.toString())) {

            if (doesDiscountExecutionPercentAndAmountHaveError(
                    dto.getLastMinuteDiscountPostDto().getDiscountExecution(),
                    dto.getLastMinuteDiscountPostDto().getAmount(),
                    dto.getLastMinuteDiscountPostDto().getPercent(),
                    null,
                    sb
            )) {
                hasError = true;
            }

            if (doesDateHaveError(
                    "تاریخ هدف",
                    dto.getLastMinuteDiscountPostDto().getTargetDate(),
                    sb)) {
                hasError = true;
            }

            if (doesDateHaveError(
                    "تاریخ شروع فعال بودن",
                    dto.getLastMinuteDiscountPostDto().getLifeTimeStart(),
                    sb
            )) {
                hasError = true;
            }

            if (doesOrderOfDatesForLastMinuteHaveError(
                    dto.getLastMinuteDiscountPostDto().getTargetDate(),
                    dto.getLastMinuteDiscountPostDto().getLifeTimeStart(),
                    sb)) {
                hasError = true;
            }

        }

        return hasError;
    }

    private boolean doesDiscountExecutionPercentAndAmountHaveError(
            String inputDiscountExecution,
            Long amount,
            Integer percent,
            Long discountThreshold,
            StringBuffer sb) {

        boolean hasError = false;

        if (inputDiscountExecution == null) {
            hasError = true;
            sb.append("نوع اعمال تخفیف وارد نشده است");
            sb.append("\n");
        } else if (
                !inputDiscountExecution.equals(PERCENTAGE.toString()) &&
                        !inputDiscountExecution.equals(AMOUNT.toString())
        ) {
            sb.append("نوع اعمال تخفیف اشتباه وارد شده است");
            sb.append("\n");
            hasError = true;
        } else if (inputDiscountExecution != null) {

            if (
                    inputDiscountExecution.equals(PERCENTAGE.toString()) &&
                            amount != null
            ) {
                sb.append("در حالت تخفیف درصدی، مقدار تخفیف باید تهی باشد");
                sb.append("\n");
                hasError = true;
            }

            if (
                    inputDiscountExecution.equals(PERCENTAGE.toString()) &&
                            percent == null
            ) {
                sb.append("مقدار درصد تهی است");
                sb.append("\n");
                hasError = true;
            }

            if (
                    inputDiscountExecution.equals(PERCENTAGE.toString()) &&
                            percent != null
            ) {
                if (percent < 0 || percent > 100) {

                    sb.append("مقدار درصد باید بین 0 تا 100 باشد");
                    sb.append("\n");
                    hasError = true;
                }
            }

            if (inputDiscountExecution.equals(AMOUNT.toString()) && percent != null) {
                sb.append("در حالت تخیف مقداری، درصد تخفیف باید تهی باشد");
                sb.append("\n");
                hasError = true;
            }

            if (inputDiscountExecution.equals(AMOUNT.toString()) && amount == null) {
                sb.append("مقدار تخفیف تهی است");
                sb.append("\n");
                hasError = true;
            }

            if (inputDiscountExecution.equals(AMOUNT.toString()) && amount != null) {

                if (amount < 0) {
                    sb.append("مقدار تخفیف منفی است");
                    sb.append("\n");
                    hasError = true;
                }

            }

            if (inputDiscountExecution.equals(AMOUNT.toString()) && discountThreshold != null) {
                sb.append("در حالت تخفیف مقداری، آستانه تخفیف باید تهی باشد");
                sb.append("\n");
                hasError = true;
            }
        }
        return hasError;
    }

    private boolean doesOrderOfDatesForLastMinuteHaveError(
            String targetDateInString,
            String liftTimeStartInString,
            StringBuffer sb) {

        if (targetDateInString == null || liftTimeStartInString == null) {
            sb.append("یکی از زمان های مورد نیاز تهی است");
            sb.append("\n");
            return true;
        }

        boolean hasError = false;

        try {
            LocalDateTime targetDate = convertStringToLdt(targetDateInString, datePattern);
            LocalDateTime lifeTimeStart = convertStringToLdt(liftTimeStartInString, datePattern);

            if (targetDate.isBefore(lifeTimeStart)) {
                sb.append("تاریخ هدف نیمتواند قبل از تاریخ فعال بودن باشد");
                sb.append("\n");
                hasError = true;
            }

        } catch (DateTimeParseException e) {
            sb.append(String.format("فرمت یکی از تاریخ ها اشتباه است. فرمت مجاز %s است", datePattern));
            sb.append("\n");
            hasError = true;
        }

        return hasError;
    }

    private boolean validateGeneralDiscount(DiscountPostDto dto, StringBuffer sb) {
        /* This method is kind of hug. Believe me I know, 😬
         * You can find out each section's responsibility by glancing to the defined error message */

        boolean hasError = false;

        if (
                dto.getDiscountType().equals(GENERAL.toString()) &&
                        dto.getGeneralDiscountPostDto() == null
        ) {
            sb.append("تخفیف عمومی وارد نشده است");
            sb.append("\n");
            hasError = true;
        } else if (
                dto.getDiscountType().equals(GENERAL.toString()) &&
                        (dto.getLastMinuteDiscountPostDto() != null || dto.getCodeDiscountPostDto() != null)
        ) {
            sb.append("در حالت تخفیف عمومی، تخفیف های لحظه آخری و کد باید تهی باشند");
            sb.append("\n");
            hasError = true;
        } else if (dto.getDiscountType().equals(GENERAL.toString()) && dto.getGeneralDiscountPostDto() != null) {

            if (doesDiscountExecutionPercentAndAmountHaveError(
                    dto.getGeneralDiscountPostDto().getDiscountExecution(),
                    dto.getGeneralDiscountPostDto().getDiscountAmount(),
                    dto.getGeneralDiscountPostDto().getDiscountPercent(),
                    dto.getGeneralDiscountPostDto().getDiscountThreshold(),
                    sb
            )) {
                hasError = true;
            }

            if (doesLifeTimeRangeAndTargetDateRangeHaveError(
                    dto.getGeneralDiscountPostDto().getLifeTimeStart(),
                    dto.getGeneralDiscountPostDto().getLifeTimeEnd(),
                    dto.getGeneralDiscountPostDto().getTargetDateStart(),
                    dto.getGeneralDiscountPostDto().getTargetDateEnd(),
                    sb
            )) {
                hasError = true;
            }

            if (doesOrderOfDatesForGeneralDiscountHaveError(
                    dto.getGeneralDiscountPostDto().getLifeTimeStart(),
                    dto.getGeneralDiscountPostDto().getLifeTimeEnd(),
                    dto.getGeneralDiscountPostDto().getTargetDateStart(),
                    dto.getGeneralDiscountPostDto().getTargetDateEnd(),
                    sb
            )) {
                hasError = true;
            }

        }
        return hasError;
    }

    private boolean doesLifeTimeRangeAndTargetDateRangeHaveError(
            String lifeTimeInString,
            String lifeTimeEndInString,
            String targtDateStartInString,
            String targetDateEndInString,
            StringBuffer sb
    ) {

        boolean hasError = false;

        if (doesDateHaveError(
                "شروع زمان فعال بودن",
                lifeTimeInString,
                sb)) {
            hasError = true;
        }

        if (doesDateHaveError(
                "پایان زمان فعال بودن",
                lifeTimeEndInString,
                sb)) {
            hasError = true;
        }

        if (doesDateHaveError(
                "شروع زمان تاریخ هدف",
                targtDateStartInString,
                sb)) {
            hasError = true;
        }

        if (doesDateHaveError(
                "پایان زمان تاریخ هدف",
                targetDateEndInString,
                sb)) {
            hasError = true;
        }

        return hasError;

    }

    private boolean doesOrderOfDatesForGeneralDiscountHaveError(
            String lifeTimeStartInString,
            String lifeTimeEndInString,
            String targetDateStartInString,
            String targetDateEndInString,
            StringBuffer sb) {

        if (lifeTimeStartInString == null ||
                lifeTimeEndInString == null ||
                targetDateStartInString == null ||
                targetDateEndInString == null
        ) {
            sb.append("یکی از زمان های مورد نیاز تهی است");
            sb.append("\n");
            return true;
        }


        boolean hasError = false;

        try {
            LocalDateTime lifeTimeStart = convertStringToLdt(lifeTimeStartInString, datePattern);
            LocalDateTime lifeTimeEnd = convertStringToLdt(lifeTimeEndInString, datePattern);
            LocalDateTime targetDateStart = convertStringToLdt(targetDateStartInString, datePattern);
            LocalDateTime targetDateEnd = convertStringToLdt(targetDateEndInString, datePattern);


            if (lifeTimeEnd.isBefore(lifeTimeStart)) {
                sb.append("تاریخ پایان زمان فعال بودن نمی تواند قبل از تاریخ شروع آن باشد");
                sb.append("\n");
                hasError = true;
            }

            if (targetDateEnd.isBefore(targetDateStart)) {
                sb.append("زمان پایان تاریخ هدف نمی تواند قبل از تاریخ شروع آن باشد");
                sb.append("\n");
                hasError = true;
            }

            if (targetDateStart.isBefore(lifeTimeStart)) {
                sb.append("زمان شروع تاریخ هدف نمی تواند قبل از زمان شروع تاریخ فعال بودن باشد");
                sb.append("\n");
                hasError = true;
            }

            if (targetDateEnd.isBefore(lifeTimeEnd)) {
                sb.append("زمان پایان تاریخ هدف نمی تواند قبل از زمان پایان تاریخ فعال بودن باشد");
                sb.append("\n");
                hasError = true;
            }

        } catch (DateTimeParseException e) {
            sb.append(String.format("فرمت یکی از تاریخ ها اشتباه است. فرمت مجاز %s است", datePattern));
            sb.append("\n");
            hasError = true;
        }

        return hasError;
    }

    private boolean doesDateHaveError(String dateName, String dateInString, StringBuffer sb) {

        boolean hasError = false;

        if (dateInString == null) {
            sb.append(dateName + " نمیتواند تهی باشد");
            sb.append("\n");
            hasError = true;
        } else {

            try {
                LocalDateTime date = convertStringToLdt(dateInString, datePattern);

                if (date.get(ChronoField.HOUR_OF_DAY) != 0) {
                    hasError = true;
                    sb.append("در " + dateName + " ساعت باید صفر باشد");
                    sb.append("\n");
                }

                if (date.get(ChronoField.MINUTE_OF_HOUR) != 0) {
                    hasError = true;
                    sb.append("در " + dateName + " دقیقه باید صفر باشد");
                    sb.append("\n");
                }

                if (date.get(ChronoField.SECOND_OF_MINUTE) != 0) {
                    hasError = true;
                    sb.append("در " + dateName + " ثانیه باید صفر باشد");
                    sb.append("\n");
                }

                if (date.get(ChronoField.NANO_OF_SECOND) != 0) {
                    hasError = true;
                    sb.append("در " + dateName + " میکرو ثانیه باید صفر باشد");
                    sb.append("\n");
                }

            } catch (DateTimeParseException e) {
                sb.append(String.format("فرمت %s اشتباه است. فرمت مجاز %s است", dateName, datePattern));
                sb.append("\n");
                hasError = true;
            }
        }
        return hasError;
    }

    private boolean validateDiscountType(DiscountPostDto dto, StringBuffer sb) {

        boolean hasError = false;

        if (dto.getDiscountType() == null) {
            hasError = true;
            sb.append("نوع تخفیف وارد نشده است");
            sb.append("\n");
        } else if (
                !dto.getDiscountType().equals(GENERAL.toString()) &&
                        !dto.getDiscountType().equals(LAST_MINUTE.toString()) &&
                        !dto.getDiscountType().equals(CODE.toString())

        ) {
            hasError = true;
            sb.append("نوع تخفیف صحیح نیست");
            sb.append("\n");
        }
        return hasError;
    }

    private boolean validateDiscountPlaceInfo(DiscountPostDto dto, StringBuffer sb) {

        boolean hasError = false;

        if (dto.getDiscountPlaceInfoPostDto() == null) {
            hasError = true;
            sb.append("آیدی بوم و نام اتاق وارد نشده است");
            sb.append("\n");
        } else if (
                dto.getDiscountPlace().equals(ROOM_DISCOUNT.toString()) &&
                        (dto.getDiscountPlaceInfoPostDto().getBoomId() == null ||
                                dto.getDiscountPlaceInfoPostDto().getRoomName() == null)
        ) {
            hasError = true;
            sb.append("آیدی بوم یا نام اتاق وارد نشده است");
            sb.append("\n");
        } else if (dto.getDiscountPlace().equals(BOOM_DISCOUNT.toString()) &&
                dto.getDiscountPlaceInfoPostDto().getRoomName() != null) {
            hasError = true;
            sb.append("درحالت تخفیف بوم، نام اتاق باید تهی باشد");
            sb.append("\n");
        } else if (
                dto.getDiscountPlace().equals(BOOM_DISCOUNT.toString()) &&
                        dto.getDiscountPlaceInfoPostDto().getBoomId() == null
        ) {
            hasError = true;
            sb.append("آیدی بوم وارد نشده است");
            sb.append("\n");
        }

        return hasError;
    }

    private boolean validateDiscountPlace(DiscountPostDto dto, StringBuffer sb) {

        boolean hasError = false;

        if (dto.getDiscountPlace() == null) {
            hasError = true;
            sb.append("محل تخفیف وارد نشده است");
            sb.append("\n");
        } else if (
                !dto.getDiscountPlace().equals(ROOM_DISCOUNT.toString()) &&
                        !dto.getDiscountPlace().equals(BOOM_DISCOUNT.toString())
        ) {
            hasError = true;
            sb.append("محل تخفیف اشتباه است");
            sb.append("\n");
        }

        return hasError;

    }
}

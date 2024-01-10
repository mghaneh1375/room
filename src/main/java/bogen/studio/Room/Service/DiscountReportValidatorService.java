package bogen.studio.Room.Service;

import bogen.studio.Room.Enums.DiscountExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static bogen.studio.Room.Enums.DiscountExecution.AMOUNT;
import static bogen.studio.Room.Enums.DiscountExecution.PERCENTAGE;

@Service
@RequiredArgsConstructor
public class DiscountReportValidatorService {

    public boolean hasDateAnyError(Optional<LocalDateTime> dateOptional, StringBuffer sb, String dateName) {

        return dateOptional
                .map(
                        (date) -> {

                            boolean output = false;

                            if (date.getHour() != 0) {
                                sb.append(dateName + ":" + "ساعت باید 0 باشد").append("\n");
                                output = true;
                            }

                            if (date.getMinute() != 0) {
                                sb.append(dateName + ":" + "دقیقه باید 0 باشد").append("\n");
                                output = true;
                            }

                            if (date.getSecond() != 0) {
                                sb.append(dateName + ":" + "ثانیه باید 0 باشد").append("\n");
                                output = true;
                            }

                            if (date.getNano() != 0) {
                                sb.append(dateName + ":" + "نانو ثانیه باید 0 باشد").append("\n");
                                output = true;
                            }


                            return output;
                        }
                )
                .orElse(false);


    }

    public boolean hasDiscountExecutionAmountPercentAnyError(
            Optional<DiscountExecution> discountExecutionOptional,
            Optional<Integer> discountAmountMinOptional,
            Optional<Integer> discountAmountMaxOptional,
            Optional<Integer> discountPercentMinOptional,
            Optional<Integer> discountPercentMaxOptional,
            StringBuffer sb
    ) {

        boolean hasError = false;

        if (discountExecutionOptional.isEmpty()) {
            if (
                    discountAmountMinOptional.isPresent() ||
                            discountAmountMaxOptional.isPresent() ||
                            discountPercentMinOptional.isPresent() ||
                            discountPercentMaxOptional.isPresent()
            ) {
                hasError = true;
                sb.append("در حالتی که نوع تخفیف مشخص نشده است، بازه مبلغ و درصد باید تهی باشند");
                sb.append("\n");
            }
        } else {
            if (
                    discountExecutionOptional.get().equals(AMOUNT) &&
                            (discountPercentMinOptional.isPresent() || discountPercentMaxOptional.isPresent())
            ) {
                hasError = true;
                sb.append("در حالت تخفیف مقداری، بازه درصد باید تهی باشند");
                sb.append("\n");
            }

            if (
                    discountExecutionOptional.get().equals(AMOUNT) &&
                            (discountAmountMinOptional.isPresent() && discountAmountMaxOptional.isEmpty())
            ) {
                hasError = true;
                sb.append("حداکثر مقدار بازه تخفیف وارد نشده است");
                sb.append("\n");
            }

            if (
                    discountExecutionOptional.get().equals(AMOUNT) &&
                            (discountAmountMinOptional.isEmpty() && discountAmountMaxOptional.isPresent())
            ) {
                hasError = true;
                sb.append("حداقل مقدار بازه تخفیف وارد نشده است");
                sb.append("\n");
            }

            if (
                    discountExecutionOptional.get().equals(AMOUNT) &&
                            (discountAmountMinOptional.isPresent() && discountAmountMaxOptional.isPresent())
            ) {

                if (discountAmountMinOptional.get() < 0) {
                    hasError = true;
                    sb.append("حداقل مقدار بازه تخفیف نمیتواند منفی باشد");
                    sb.append("\n");
                }

                if (discountAmountMinOptional.get() > discountAmountMaxOptional.get()) {
                    hasError = true;
                    sb.append("حداقل مقدار بازه مقدار تخفیف بزرگتر از حداکثر بازه است");
                    sb.append("\n");
                }
            }

            if (
                    discountExecutionOptional.get().equals(PERCENTAGE) &&
                            (discountAmountMinOptional.isPresent() || discountAmountMaxOptional.isPresent())
            ) {
                hasError = true;
                sb.append("در حالت تخفیف درصدی، بازه مبلغ باید تهی باشند");
                sb.append("\n");
            }

            if (
                    discountExecutionOptional.get().equals(PERCENTAGE) &&
                            (discountPercentMinOptional.isPresent() && discountPercentMaxOptional.isEmpty())
            ) {
                hasError = true;
                sb.append("حداکثر مقدار بازه درصد وارد نشده است");
                sb.append("\n");
            }

            if (
                    discountExecutionOptional.get().equals(PERCENTAGE) &&
                            (discountPercentMinOptional.isEmpty() && discountPercentMaxOptional.isPresent())
            ) {
                hasError = true;
                sb.append("حداقل مقدار بازه درصد وارد نشده است");
                sb.append("\n");
            }

            if (
                    discountExecutionOptional.get().equals(PERCENTAGE) &&
                            (discountPercentMinOptional.isPresent() && discountPercentMaxOptional.isPresent())
            ) {

                if (discountPercentMinOptional.get() < 0) {
                    hasError = true;
                    sb.append("حداقل مقدار بازه درصد منفی است");
                    sb.append("\n");
                }

                if (discountPercentMaxOptional.get() > 100) {
                    hasError = true;
                    sb.append("حداکثر مقدار بازه درصد بیشتر از 100 است");
                    sb.append("\n");
                }

                if (discountPercentMinOptional.get() > discountPercentMaxOptional.get()) {
                    hasError = true;
                    sb.append("حداکثر مقدار بازه درصد کمتر از حداقل مقدار بازه است");
                    sb.append("\n");
                }
            }

        }

        return hasError;
    }

    public boolean hasPlaceNameAnyError(Optional<String> placeNameOptional, StringBuffer sb, String location) {

        if (placeNameOptional.isPresent()) {
            if (placeNameOptional.get().isBlank()) {
                sb.append(location + ":" + "خالی است");
                sb.append("\n");
                return true;
            }
        }

        return false;
    }

}

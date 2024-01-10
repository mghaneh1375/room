package bogen.studio.Room.Service;

import bogen.studio.Room.Enums.DiscountExecution;
import bogen.studio.Room.Exception.InvalidInputException;
import bogen.studio.Room.Models.*;
import bogen.studio.Room.Repository.CityRepository;
import bogen.studio.Room.Repository.DiscountReportRepository;
import bogen.studio.Room.documents.City;
import bogen.studio.Room.documents.Discount;
import bogen.studio.Room.documents.DiscountReport;
import bogen.studio.Room.documents.Place;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static bogen.studio.Room.Enums.DiscountExecution.AMOUNT;
import static bogen.studio.Room.Enums.DiscountExecution.PERCENTAGE;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountReportService {

    private final DiscountReportRepository discountReportRepository;
    private final RoomService roomService;
    private final BoomService boomService;
    private final PlaceService placeService;
    private final CityRepository cityRepository;
    private final DiscountService discountService;

    public DiscountReport insert(DiscountReport discountReport) {

        return discountReportRepository.insert(discountReport);
    }

    public void buildAndInsertDiscountReportsOfReservationRequest(ReservationRequest request) {
        /* Build and insert discount report for each target date in reservation discount info, if the target date has a
         * valid applied discount */

        try {
            // Fetch room, boom, place ,and city
            Room room = roomService.findById(request.getRoomId());
            Boom boom = boomService.findById(room.getBoomId());
            Place place = placeService.fetchById(boom.getPlaceId());
            City city = cityRepository.fetchById(place.getCityId());

            // Loop over target dates
            for (TargetDateDiscountDetail targetDateDiscountDetail : request.getDiscountInfo().getTargetDateDiscountDetails()) {

                // Check whether the target date has a discount
                if (targetDateDiscountDetail.getDiscountId() != null) {

                    // Fetch the applied discount
                    Discount fetchedDiscount = discountService.fetchDiscountById(targetDateDiscountDetail.getDiscountId());

                    DiscountReport discountReport = new DiscountReport()
                            .setUserId(request.getUserId())
                            .setCreatedAt(LocalDateTime.now())
                            .setTargetDate(targetDateDiscountDetail.getTargetDate())
                            .setRoomId(request.getRoomId())
                            .setRoomName(room.getTitle())
                            .setBoomId(room.getBoomId())
                            .setBoomName(place.getName())
                            .setOwnerId(request.getOwnerId())
                            .setCity(city.getName())
                            .setProvince(city.getState())
                            .setDiscountId(targetDateDiscountDetail.getDiscountId())
                            .setDiscountType(fetchedDiscount.getDiscountType())
                            .setCalculatedDiscount(targetDateDiscountDetail.getCalculatedDiscount());

                    setDiscountExecutionAmountAndPercent(discountReport, fetchedDiscount);

                    discountReportRepository.insert(discountReport);
                    log.info(String.format("Discount report inserted for reservationId: %s, and target date: %s",
                            request.get_id(), targetDateDiscountDetail.getTargetDate()));
                }
            }
        } catch (Exception e) {
            log.error(String.format("Exception in inserting discount report for reservation request: %s, error message: %s",
                    request.get_id(), e.getMessage()));
        }


    }

    private void setDiscountExecutionAmountAndPercent(DiscountReport discountReport, Discount fetchedDiscount) {

        switch (fetchedDiscount.getDiscountType()) {
            case GENERAL:
                GeneralDiscount generalDiscount = fetchedDiscount.getGeneralDiscount();
                discountReport.setDiscountExecution(generalDiscount.getDiscountExecution());
                discountReport.setDiscountAmount(generalDiscount.getAmount());
                discountReport.setDiscountPercent(generalDiscount.getPercent());
                break;
            case LAST_MINUTE:
                LastMinuteDiscount lastMinuteDiscount = fetchedDiscount.getLastMinuteDiscount();
                discountReport.setDiscountExecution(lastMinuteDiscount.getDiscountExecution());
                discountReport.setDiscountAmount(lastMinuteDiscount.getAmount());
                discountReport.setDiscountPercent(lastMinuteDiscount.getPercent());
                break;
            case CODE:
                CodeDiscount codeDiscount = fetchedDiscount.getCodeDiscount();
                discountReport.setDiscountExecution(codeDiscount.getDiscountExecution());
                discountReport.setDiscountAmount(codeDiscount.getAmount());
                discountReport.setDiscountPercent(codeDiscount.getPercent());
                break;
        }

    }

    public void validateSearchFields(
            Optional<String> boomNameOptional,
            Optional<String> roomNameOptional,
            Optional<String> cityNameOptional,
            Optional<String> provinceNameOptional,
            Optional<LocalDateTime> issueDateOptional,
            Optional<LocalDateTime> targetDateOptional,
            Optional<DiscountExecution> discountExecutionOptional,
            Optional<Integer> discountAmountMinOptional,
            Optional<Integer> discountAmountMaxOptional,
            Optional<Integer> discountPercentMinOptional,
            Optional<Integer> discountPercentMaxOptional
    ) {

        StringBuffer sb = new StringBuffer();

        boolean hasIssueDateAnyError = hasDateAnyError(issueDateOptional, sb, "تاریخ صدور");
        boolean hasTargetDateAnyError = hasDateAnyError(targetDateOptional, sb, "تاریخ اقامت");
        boolean hasDiscountExecutionAmountPercentAnyError = hasDiscountExecutionAmountPercentAnyError(
                discountExecutionOptional,
                discountAmountMinOptional,
                discountAmountMaxOptional,
                discountPercentMinOptional,
                discountPercentMaxOptional,
                sb);
        boolean hasBoomNameAnyError = hasPlaceNameAnyError(boomNameOptional, sb, "نام اقامت گاه");
        boolean hasRoomNameAnyError = hasPlaceNameAnyError(roomNameOptional, sb, "نام اتاق");
        boolean hasCityNameAnyError = hasPlaceNameAnyError(cityNameOptional, sb, "نام شهر");
        boolean hasProvinceNameAnyError = hasPlaceNameAnyError(provinceNameOptional, sb, "نام استان");

        if (
                hasIssueDateAnyError || hasTargetDateAnyError || hasDiscountExecutionAmountPercentAnyError ||
                        hasBoomNameAnyError || hasRoomNameAnyError || hasCityNameAnyError || hasProvinceNameAnyError
        ) {
            String logMsg = sb.toString().replace("\n", ",");
            log.error("Error in validating search fields for discount_report search: " + logMsg);
            throw new InvalidInputException(sb.toString());
        }
    }

    private boolean hasDateAnyError(Optional<LocalDateTime> dateOptional, StringBuffer sb, String dateName) {

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

    private boolean hasDiscountExecutionAmountPercentAnyError(
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
                    discountExecutionOptional.get().equals(PERCENTAGE) &&
                            (discountAmountMinOptional.isPresent() || discountAmountMaxOptional.isPresent())
            ) {
                hasError = true;
                sb.append("در حالت تخفیف درصدی، بازه مبلغ باید تهی باشند");
                sb.append("\n");
            }
        }

        return hasError;
    }

    private boolean hasPlaceNameAnyError(Optional<String> placeNameOptional, StringBuffer sb, String location) {

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

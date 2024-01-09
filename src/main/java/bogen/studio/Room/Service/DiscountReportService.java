package bogen.studio.Room.Service;

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
}

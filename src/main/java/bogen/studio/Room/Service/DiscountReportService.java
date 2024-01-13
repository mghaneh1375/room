package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.PaginationResult;
import bogen.studio.Room.Enums.DiscountExecution;
import bogen.studio.Room.Enums.DiscountType;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static bogen.studio.Room.Routes.Utility.getUserId;
import static bogen.studio.Room.Utility.TimeUtility.getExactEndTimeOfInputDate;
import static bogen.studio.Room.Utility.TimeUtility.getExactStartTimeOfInputDate;
import static bogen.studio.Room.Utility.UserUtility.getUserAuthorities;

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
    private final DiscountReportValidatorService validatorService;
    private final PaginationService paginationService;
    private final MongoTemplate mongoTemplate;

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
                            .setUserId(request.getUserId().toString())
                            .setCreatedAt(LocalDateTime.now())
                            .setTargetDate(targetDateDiscountDetail.getTargetDate())
                            .setRoomId(request.getRoomId().toString())
                            .setRoomName(room.getTitle())
                            .setBoomId(room.getBoomId().toString())
                            .setBoomName(place.getName())
                            .setBoomOwnerId(request.getOwnerId().toString())
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
            Optional<LocalDateTime> createdDateOptional,
            Optional<LocalDateTime> targetDateOptional,
            Optional<DiscountExecution> discountExecutionOptional,
            Optional<Integer> discountAmountMinOptional,
            Optional<Integer> discountAmountMaxOptional,
            Optional<Integer> discountPercentMinOptional,
            Optional<Integer> discountPercentMaxOptional
    ) {

        StringBuffer sb = new StringBuffer();

        boolean hasIssueDateAnyError = validatorService.hasDateAnyError(createdDateOptional, sb, "تاریخ صدور");
        boolean hasTargetDateAnyError = validatorService.hasDateAnyError(targetDateOptional, sb, "تاریخ اقامت");
        boolean hasDiscountExecutionAmountPercentAnyError = validatorService.hasDiscountExecutionAmountPercentAnyError(
                discountExecutionOptional,
                discountAmountMinOptional,
                discountAmountMaxOptional,
                discountPercentMinOptional,
                discountPercentMaxOptional,
                sb);
        boolean hasBoomNameAnyError = validatorService.hasPlaceNameAnyError(boomNameOptional, sb, "نام اقامت گاه");
        boolean hasRoomNameAnyError = validatorService.hasPlaceNameAnyError(roomNameOptional, sb, "نام اتاق");
        boolean hasCityNameAnyError = validatorService.hasPlaceNameAnyError(cityNameOptional, sb, "نام شهر");
        boolean hasProvinceNameAnyError = validatorService.hasPlaceNameAnyError(provinceNameOptional, sb, "نام استان");

        if (
                hasIssueDateAnyError || hasTargetDateAnyError || hasDiscountExecutionAmountPercentAnyError ||
                        hasBoomNameAnyError || hasRoomNameAnyError || hasCityNameAnyError || hasProvinceNameAnyError
        ) {
            String logMsg = sb.toString().replace("\n", ",");
            log.error("Error in validating search fields for discount_report search: " + logMsg);
            throw new InvalidInputException(sb.toString());
        }
    }


    public PaginationResult<DiscountReport> paginatedSearch(
            Optional<String> boomNameOptional,
            Optional<String> roomNameOptional,
            Optional<String> cityNameOptional,
            Optional<String> provinceNameOptional,
            Optional<LocalDateTime> createdDateOptional,
            Optional<LocalDateTime> targetDateOptional,
            Optional<DiscountType> discountTypeOptional,
            Optional<DiscountExecution> discountExecutionOptional,
            Optional<Integer> discountAmountMinOptional,
            Optional<Integer> discountAmountMaxOptional,
            Optional<Integer> discountPercentMinOptional,
            Optional<Integer> discountPercentMaxOptional,
            Principal principal,
            int page,
            int size
    ) {
        /* Paginated search of discount-report */

        // Build paginated query
        Query query = buildQueryForDiscountReportSearch(
                boomNameOptional,
                roomNameOptional,
                cityNameOptional,
                provinceNameOptional,
                createdDateOptional,
                targetDateOptional,
                discountTypeOptional,
                discountExecutionOptional,
                discountAmountMinOptional,
                discountAmountMaxOptional,
                discountPercentMinOptional,
                discountPercentMaxOptional,
                principal
        );
        Pageable pageable = paginationService.buildPageable(page, size, "created_at", "ASCENDING");
        Query paginatedQuery = query.with(pageable);

        // Perform search
        List<DiscountReport> discountReports = mongoTemplate.find(
                paginatedQuery,
                DiscountReport.class,
                mongoTemplate.getCollectionName(DiscountReport.class)
        );

        //
        Page<DiscountReport> reportsInPage = PageableExecutionUtils.getPage(
                discountReports,
                pageable,
                () -> mongoTemplate.count(Query.of(paginatedQuery).limit(-1).skip(-1), DiscountReport.class)
        );

        return paginationService.buildPaginationResult(reportsInPage);
    }

    private Query buildQueryForDiscountReportSearch(
            Optional<String> boomNameOptional,
            Optional<String> roomNameOptional,
            Optional<String> cityNameOptional,
            Optional<String> provinceNameOptional,
            Optional<LocalDateTime> createdDateOptional,
            Optional<LocalDateTime> targetDateOptional,
            Optional<DiscountType> discountTypeOptional,
            Optional<DiscountExecution> discountExecutionOptional,
            Optional<Integer> discountAmountMinOptional,
            Optional<Integer> discountAmountMaxOptional,
            Optional<Integer> discountPercentMinOptional,
            Optional<Integer> discountPercentMaxOptional,
            Principal principal
    ) {
        /* Build search query for discount-report according to inputs */

        List<Criteria> criteriaList = new ArrayList<>();

        criteriaList.add(Criteria.where("_id").exists(true));

        boomNameOptional.ifPresent(boomName -> criteriaList.add(Criteria.where("boom_name").is(boomName)));
        roomNameOptional.ifPresent(roomName -> criteriaList.add(Criteria.where("room_name").is(roomName)));
        cityNameOptional.ifPresent(cityName -> criteriaList.add(Criteria.where("city").is(cityName)));
        provinceNameOptional.ifPresent(provinceName -> criteriaList.add(Criteria.where("province").is(provinceName)));
        createdDateOptional.ifPresent(createdDate ->
                criteriaList.add(
                        Criteria.where("created_at").gte(getExactStartTimeOfInputDate(createdDate))
                                .andOperator(Criteria.where("created_at").lte(getExactEndTimeOfInputDate(createdDate)))
                )
        );
        targetDateOptional.ifPresent(targetDate -> criteriaList.add(Criteria.where("target_date").is(targetDate)));
        discountTypeOptional.ifPresent(discountType -> criteriaList.add(Criteria.where("discount_type").is(discountType)));
        discountExecutionOptional.ifPresent(discountExecution -> criteriaList.add(Criteria.where("discount_execution").is(discountExecution)));
        discountAmountMinOptional.ifPresent(amountMin ->
                discountAmountMaxOptional.ifPresent(amountMax ->
                        criteriaList.add(
                                Criteria.where("discount_amount").gte(amountMin)
                                        .andOperator(Criteria.where("discount_amount").lte(amountMax)
                                        )
                        )
                )
        );
        discountPercentMinOptional.ifPresent(percentMin ->
                discountPercentMaxOptional.ifPresent(percentMax ->
                        criteriaList.add(
                                Criteria.where("discount_percent").gte(percentMin)
                                        .andOperator(Criteria.where("discount_percent").lte(percentMax))
                        )

                )

        );

        // Todo: get user authorities, then if it is not admin create a criteria where ownerId equals ApiCallerId
        List<String> authorities = getUserAuthorities(principal);
        if (!authorities.contains("ADMIN")) {
            /* If Api caller is not an admin, then the callerId should be same as the ownerId in the discount-report */
            criteriaList.add(Criteria.where("boom_owner_id").is(getUserId(principal).toString()));
        }

        // Create search criteria
        Criteria searchCriteria = new Criteria().andOperator(criteriaList);

        return new Query().addCriteria(searchCriteria);
    }

}

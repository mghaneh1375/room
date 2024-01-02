package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.DiscountPostDto;
import bogen.studio.Room.Enums.DiscountPlace;
import bogen.studio.Room.Enums.DiscountType;
import bogen.studio.Room.Models.CalculatedDiscountInfo;
import bogen.studio.Room.Models.DiscountPlaceInfo;
import bogen.studio.Room.Repository.DiscountRepository;
import bogen.studio.Room.documents.Discount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import static bogen.studio.Room.Routes.Utility.getUserId;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final InsertDiscountService insertDiscountService;
    private final CalculateDiscountService calculateDiscountService;

    public Discount insert(DiscountPostDto dto, Principal principal) {
        /* Create Discount doc and insert it into DB */

        // Create discountPlace, boomId, DiscountPlaceInfo, and DiscountType from DTO
        DiscountPlace discountPlace = DiscountPlace.valueOf(dto.getDiscountPlace());
        ObjectId boomId = new ObjectId(dto.getDiscountPlaceInfoPostDto().getBoomId());
        DiscountPlaceInfo discountPlaceInfo = new DiscountPlaceInfo(
                boomId,
                dto.getDiscountPlaceInfoPostDto().getRoomName());
        DiscountType discountType = DiscountType.valueOf(dto.getDiscountType());

        // Check the integrity of boomId roomName, and discount code uniqueness;
        insertDiscountService.checkBoomIdExistence(boomId);
        insertDiscountService.checkRoomExistence(discountPlace, boomId, discountPlaceInfo.getRoomName());
        insertDiscountService.checkCodeUniqueness(discountType, boomId, dto);

        // Check the user to be the owner of the boom or the system admin
        insertDiscountService.isUserAllowedToCreateDiscount(boomId, principal);

        // Instantiate discount
        Discount discount = new Discount()
                .setDiscountPlace(discountPlace)
                .setDiscountPlaceInfo(discountPlaceInfo)
                .setDiscountType(discountType)
                .setCreatedBy(getUserId(principal));

        // According to input discountType set general or lastMinute or code discount
        insertDiscountService.setGeneralOrLastMinuteOrCodeDiscount(discountType, discount, dto);

        // Insert to DB
        return discountRepository.insert(discount);
    }

    public CalculatedDiscountInfo getMaximumDiscountForTargetDate(
            ObjectId boomId,
            String roomName,
            LocalDateTime targetDate,
            int nightOrdinalNumber,
            Long totalAmount) {
        /* Find related discounts, then return the one with maximum discount amount */

        // Fetch related discounts from db
        List<Discount> discounts = calculateDiscountService
                .fetchDefinedDiscountsForTargetDate(boomId, roomName, targetDate);

        // Calculate discount-amount for fetched discounts
        List<CalculatedDiscountInfo> calculatedDiscountInfoList =
                calculateDiscountService.
                        calculateDiscountAmountForFetchedDiscounts(
                                discounts,
                                nightOrdinalNumber,
                                totalAmount);

        // Find maximum discount amount
        return calculateDiscountService
                .findMaximumDiscountAmount(calculatedDiscountInfoList);
    }


}

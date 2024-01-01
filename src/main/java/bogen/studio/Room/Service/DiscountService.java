package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.DiscountPostDto;
import bogen.studio.Room.Enums.DiscountExecution;
import bogen.studio.Room.Enums.DiscountPlace;
import bogen.studio.Room.Enums.DiscountType;
import bogen.studio.Room.Models.CalculatedDiscountInfo;
import bogen.studio.Room.Models.DiscountPlaceInfo;
import bogen.studio.Room.Models.GeneralDiscount;
import bogen.studio.Room.Repository.DiscountRepository;
import bogen.studio.Room.documents.Discount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.geo.Circle;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static bogen.studio.Room.Enums.DiscountExecution.AMOUNT;
import static bogen.studio.Room.Enums.DiscountPlace.BOOM_DISCOUNT;
import static bogen.studio.Room.Enums.DiscountPlace.ROOM_DISCOUNT;
import static bogen.studio.Room.Enums.DiscountType.*;
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

    public void calculateDiscountForTargetDate(ObjectId boomId, String roomName, LocalDateTime targetDate, Long totalAmount) {
        /**/

        // Fetch discounts from db
        List<Discount> discounts = calculateDiscountService
                .fetchDefinedDiscountsForTargetDate(boomId, roomName, targetDate);

        // Calculate discount-amount for fetched discounts
        List<CalculatedDiscountInfo> calculatedDiscountInfoList = new ArrayList<>();

        // Todo: delete this section
        System.out.println("Discounts:\n");
        for (Discount discount : discounts) {

            CalculatedDiscountInfo  calculatedDiscountInfo = renameMe(discount, totalAmount);

            System.out.println(discount);
            System.out.println("\n");
            System.out.println("discount: ");
            System.out.println(calculatedDiscountInfo);
            System.out.println("---------------------------");
        }


    }

    private CalculatedDiscountInfo renameMe(Discount discount, Long totalAmount) {

        if (discount.getDiscountType().equals(GENERAL)) {

            return calculateGeneralDiscount(discount, totalAmount);

        }

        return null;
    }

    private CalculatedDiscountInfo calculateGeneralDiscount(Discount discount, Long totalAmount) {
        /* Calculate discount if it is a GENERAL one */

        GeneralDiscount generalDiscount = discount.getGeneralDiscount();
        DiscountExecution discountExecution = generalDiscount.getDiscountExecution();

        if (discountExecution.equals(AMOUNT)) {

            return calculateGeneralAmountWiseDiscount(generalDiscount, discount.get_id(), totalAmount);

        } else if (discountExecution.equals(DiscountExecution.PERCENTAGE)) {

            return calculateGeneralPercentWiseDiscount(generalDiscount, discount.get_id(), totalAmount);

        } else {
            return null;
        }
    }

    private CalculatedDiscountInfo calculateGeneralPercentWiseDiscount(GeneralDiscount generalDiscount, String discountId, Long totalAmount) {
        /* Calculate general discount if execution type is PERCENTAGE */

        Long discountThreshold = generalDiscount.getDiscountThreshold();
        Long minimumRequiredPurchase = generalDiscount.getMinimumRequiredPurchase();

        if (minimumRequiredPurchase != null) {
            if (totalAmount < minimumRequiredPurchase) {
                return null;
            }
        }

        long calculatedDiscount = totalAmount * generalDiscount.getPercent() / 100;
        if (discountThreshold != null) {
            if (calculatedDiscount > discountThreshold) {
                calculatedDiscount = discountThreshold;
            }
        }

        return new CalculatedDiscountInfo()
                .setDiscountId(discountId)
                .setCalculatedDiscount(calculatedDiscount);

    }

    private CalculatedDiscountInfo calculateGeneralAmountWiseDiscount(GeneralDiscount generalDiscount, String discountId, Long totalAmount) {
        /* Calculate discount if the discount is general, and amount-wise */

        Long minimumRequiredPurchase = generalDiscount.getMinimumRequiredPurchase();

        if (minimumRequiredPurchase != null) {
            // If minimumRequiredPurchase is defined in the discount doc.

            if (totalAmount > minimumRequiredPurchase) {
                return new CalculatedDiscountInfo()
                        .setDiscountId(discountId)
                        .setCalculatedDiscount(generalDiscount.getAmount());
            } else {
                return null;
            }

        } else {
            // If minimumRequiredPurchase is NOT defined in the discount doc.

            return new CalculatedDiscountInfo()
                    .setDiscountId(discountId)
                    .setCalculatedDiscount(generalDiscount.getAmount());
        }

    }


}

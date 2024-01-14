package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.CodeDiscountPostDto;
import bogen.studio.Room.DTO.DiscountPostDto;
import bogen.studio.Room.DTO.GeneralDiscountPostDto;
import bogen.studio.Room.DTO.LastMinuteDiscountPostDto;
import bogen.studio.Room.Enums.DiscountExecution;
import bogen.studio.Room.Enums.DiscountPlace;
import bogen.studio.Room.Enums.DiscountType;
import bogen.studio.Room.Exception.BackendErrorException;
import bogen.studio.Room.Exception.InvalidInputException;
import bogen.studio.Room.Exception.NotAccessException;
import bogen.studio.Room.Models.Boom;
import bogen.studio.Room.Models.CodeDiscount;
import bogen.studio.Room.Models.GeneralDiscount;
import bogen.studio.Room.Models.LastMinuteDiscount;
import bogen.studio.Room.Repository.RoomRepository2;
import bogen.studio.Room.Utility.TimeUtility;
import bogen.studio.Room.documents.Discount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import static bogen.studio.Room.Enums.DiscountPlace.ROOM_DISCOUNT;
import static bogen.studio.Room.Routes.Utility.getUserId;
import static bogen.studio.Room.Utility.TimeUtility.convertStringToLdt;
import static bogen.studio.Room.Utility.UserUtility.getUserAuthorities;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsertDiscountService {

    private final BoomService boomService;
    private final RoomRepository2 roomRepository2;
    private final MongoTemplate mongoTemplate;

    public void isUserAllowedToCreateDiscount(ObjectId boomId, Principal principal) {
        /* Two types of users can create discount:
         * 1. Boom owner can create discount for only his/her boom.
         * 2. System admin can create discount for any boom/room. */

        Boom boom = boomService.findById(boomId);

        List<String> userAuthorities = getUserAuthorities(principal);

        if (!userAuthorities.contains("ADMIN")) {
            if (!boom.getUserId().equals(getUserId(principal))) {
                throw new NotAccessException("شما مجاز به ایجاد تخفیف برای این بوم نیستید");
            }
        }
    }

    public void setGeneralOrLastMinuteOrCodeDiscount(DiscountType discountType, Discount discount, DiscountPostDto dto) {
        /* According to input discountType set general or lastMinute or code discount */

        switch (discountType) {

            case GENERAL:
                discount.setGeneralDiscount(createGeneralDiscount(dto.getGeneralDiscountPostDto()));
                break;
            case LAST_MINUTE:
                discount.setLastMinuteDiscount(createLastMinuteDiscount(dto.getLastMinuteDiscountPostDto()));
                break;
            case CODE:
                discount.setCodeDiscount(createCodeDiscount(dto.getCodeDiscountPostDto()));
                break;
            default:
                log.error("Unexpected case in DiscountType: " + discountType);
                throw new BackendErrorException("خطای سرور. لطفا با پشتیبانی تماس بگیرید");
        }

    }

    public void checkCodeUniqueness(DiscountType discountType, ObjectId boomId, DiscountPostDto dto) {
        /* Check uniqueness of the input discount code */

        if (discountType.equals(DiscountType.CODE)) {

            String discountCode = getDiscountCode(discountType, dto);

            Criteria boomIdCriteria = Criteria.where("discount_place_info.boom_id").is(boomId.toString());
            Criteria discountTypeCriteria = Criteria.where("discount_type").is(discountType);
            Criteria discountCodeCriteria = Criteria.where("code_discount.code").is(discountCode);
            Criteria searchCriteria = new Criteria().andOperator(boomIdCriteria, discountTypeCriteria, discountCodeCriteria);

            Query query = new Query().addCriteria(searchCriteria);

            long discountCodeCount = mongoTemplate.count(
                    query,
                    Discount.class,
                    mongoTemplate.getCollectionName(Discount.class)
            );

            if (discountCodeCount > 0) {
                throw new InvalidInputException("کد تخفیف برای این بوم تکراری است");
            }
        }
    }

    private String getDiscountCode(DiscountType discountType, DiscountPostDto dto) {
        /* Get discount code from DiscountPostDto */

        if (discountType.equals(DiscountType.CODE)) {

            CodeDiscountPostDto codeDiscountPostDto = dto.getCodeDiscountPostDto();
            if (codeDiscountPostDto != null) {
                return dto.getCodeDiscountPostDto().getCode();

            } else {
                log.error("Expected to get codeDiscountPostDto but it was null");
                throw new BackendErrorException("خطای سرور. با پشتیبانی تماس بگیرید");
            }

        } else {
            log.error(String.format("Expected to get DiscountType: CODE but got: %s", discountType));
            throw new BackendErrorException("خطای سرور. با پشتیبانی تماس بگیرید");
        }
    }

    public void checkRoomExistence(DiscountPlace discountPlace, ObjectId boomId, String roomName) {
        /* Check room existence according to input discountPlace, boomId, and roomName */

        if (discountPlace.equals(ROOM_DISCOUNT)) {
            List<String> roomNamesInBoom = roomRepository2.fetchDistinctRoomNamesOfBoom(boomId);
            if (!roomNamesInBoom.contains(roomName)) {
                throw new InvalidInputException("اتاقی با این نام در بوم وجود ندارد");
            }
        }

    }

    public void checkBoomIdExistence(ObjectId boomId) {
        /* Throw exception if boomId does not exist */

        if (!boomService.doesBoomIdExist(boomId)) {
            throw new InvalidInputException("بوم در سیستم ثبت نشده است");
        }

    }

    private CodeDiscount createCodeDiscount(CodeDiscountPostDto dto) {
        /* Create CodeDiscount */

        DiscountExecution discountExecution = DiscountExecution.valueOf(dto.getDiscountExecution());

        return new CodeDiscount()
                .setDiscountExecution(discountExecution)
                .setPercent(dto.getPercent())
                .setAmount(dto.getAmount())
                .setCode(dto.getCode())
                .setDefinedUsageCount(dto.getDefinedUsageCount())
                .setCurrentUsageCount(0)
                .setLifeTimeStart(createLdt(dto.getLifeTimeStart()))
                .setLifeTimeEnd(TimeUtility.getExactEndTimeOfInputDate(createLdt(dto.getLifeTimeEnd())))
                .setTargetDateStart(createLdt(dto.getTargetDateStart()))
                .setTargetDateEnd(TimeUtility.getExactEndTimeOfInputDate(createLdt(dto.getTargetDateEnd())));
    }

    private LastMinuteDiscount createLastMinuteDiscount(LastMinuteDiscountPostDto dto) {
        /* Create LastMinuteDiscount */

        DiscountExecution discountExecution = DiscountExecution.valueOf(dto.getDiscountExecution());

        return new LastMinuteDiscount()
                .setDiscountExecution(discountExecution)
                .setPercent(dto.getPercent())
                .setAmount(dto.getAmount())
                .setTargetDate(TimeUtility.getExactEndTimeOfInputDate(createLdt(dto.getTargetDate())))
                .setLifeTimeStart(createLdt(dto.getLifeTimeStart()));
    }

    private GeneralDiscount createGeneralDiscount(GeneralDiscountPostDto dto) {
        /* Create GeneralDiscount */

        DiscountExecution discountExecution = DiscountExecution.valueOf(dto.getDiscountExecution());

        return new GeneralDiscount()
                .setDiscountExecution(discountExecution)
                .setPercent(dto.getDiscountPercent())
                .setAmount(dto.getDiscountAmount())
                .setMinimumRequiredPurchase(dto.getMinimumRequiredPurchase())
                .setDiscountThreshold(dto.getDiscountThreshold())
                .setLifeTimeStart(createLdt(dto.getLifeTimeStart()))
                .setLifeTimeEnd(TimeUtility.getExactEndTimeOfInputDate(createLdt(dto.getLifeTimeEnd())))
                .setTargetDateStart(createLdt(dto.getTargetDateStart()))
                .setTargetDateEnd(TimeUtility.getExactEndTimeOfInputDate(createLdt(dto.getTargetDateEnd())));
    }

    private LocalDateTime createLdt(String dateInString) {
        /* Create LDT from input string according to defined date pattern */

        String datePattern = "yyyy-MM-dd'T'HH:mm:ss";

        try {
            return convertStringToLdt(dateInString, datePattern);
        } catch (DateTimeParseException e) {
            log.error("Error in parsing: " + dateInString);
            throw new BackendErrorException("خطایی در سرور رخ داده است. لطفا با پشتیبانی تماس بگیرید");
        }
    }


}

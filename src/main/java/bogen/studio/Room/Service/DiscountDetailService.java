package bogen.studio.Room.Service;

import bogen.studio.Room.Exception.InvalidIdException;
import bogen.studio.Room.Exception.NotAccessException;
import bogen.studio.Room.Models.DiscountConsumptionInfo;
import bogen.studio.Room.Models.DiscountDetail;
import bogen.studio.Room.Utility.UserUtility;
import bogen.studio.Room.documents.Discount;
import bogen.studio.Room.documents.DiscountReport;
import bogen.studio.Room.documents.KoochitaUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static bogen.studio.Room.Routes.Utility.getUserId;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;

@Service
@RequiredArgsConstructor
public class DiscountDetailService {

    private final DiscountService discountService;
    private final KoochitaUserService koochitaUserService;
    private final MongoTemplate mongoTemplate;

    public DiscountDetail buildDiscountDetail(String discountId, Principal principal) {
        /* Build discountDetail */

        // Fetch discount and check its existence
        Discount fetchedDiscount = discountService.fetchDiscountById(discountId);

        // check whether the Api-caller is the admin or discountCreator
        checkWhetherApiCallerIsAdminOrDiscountCreator(fetchedDiscount.getCreatedBy(), principal);

        // Instantiate discount detail
        DiscountDetail discountDetail = new DiscountDetail();

        // Set discount-info to discountDetail instance
        addDiscountInfoToDiscountDetail(discountDetail, fetchedDiscount);

        // Get user of discount creator, check its existence, then put data in to discountDetail
        KoochitaUser koochitaUser = koochitaUserService.fetchKoochitaUserById(fetchedDiscount.getCreatedBy());
        checkUserExistence(koochitaUser);
        addDiscountCreatorInfoToDiscountDetail(discountDetail, koochitaUser);

        // Get discount consumption info and add to discount detail
        List<DiscountConsumptionInfo> consumptionInfos = getDiscountConsumptionInfo(fetchedDiscount.get_id());
        addDiscountConsumptionInfoToDiscountDetail(discountDetail, consumptionInfos);

        return discountDetail;
    }

    private void checkWhetherApiCallerIsAdminOrDiscountCreator(String discountCreatedBy, Principal principal) {

        List<String> apiCallerAuthorities = UserUtility.getUserAuthorities(principal);

        if (!apiCallerAuthorities.contains("ADMIN")) {
            // If API caller is not admin

            if (!discountCreatedBy.equals(getUserId(principal).toString())) {
                throw new NotAccessException("شما مجاز به دریافت این اطللاعات نیستید");
            }
        }
    }

    private void addDiscountInfoToDiscountDetail(DiscountDetail discountDetail, Discount discount) {

        discountDetail.set_id(discount.get_id());
        discountDetail.setDiscountPlace(discount.getDiscountPlace());
        discountDetail.setDiscountPlaceInfo(discount.getDiscountPlaceInfo());
        discountDetail.setDiscountType(discount.getDiscountType());
        discountDetail.setGeneralDiscount(discount.getGeneralDiscount());
        discountDetail.setLastMinuteDiscount(discount.getLastMinuteDiscount());
        discountDetail.setCodeDiscount(discount.getCodeDiscount());
        discountDetail.setCreatedAt(discount.getCreatedAt());
        discountDetail.setCreatedBy(discount.getCreatedBy());
    }

    private void checkUserExistence(KoochitaUser koochitaUser) {

        if (koochitaUser == null) {
            throw new InvalidIdException("آیدی سازنده تخفیف معتبر نیست");
        }
    }

    private void addDiscountCreatorInfoToDiscountDetail(DiscountDetail discountDetail, KoochitaUser koochitaUser) {

        discountDetail.setCreatedByFirstName(koochitaUser.getFirstName());
        discountDetail.setCreatedByLastName(koochitaUser.getLastName());
    }

    private List<DiscountConsumptionInfo> getDiscountConsumptionInfo(String discountId) {

        AggregationOperation match = Aggregation.match(Criteria.where("discount_id").is(discountId));
        GroupOperation groupOperation = group("discount_id")
                .count().as("usageCount")
                .sum("calculated_discount").as("calculatedDiscountSummation")
                .addToSet("user_id").as("consumersId");

        Aggregation aggregation = Aggregation.newAggregation(match, groupOperation);

        AggregationResults<DiscountConsumptionInfo> groupResult = mongoTemplate.aggregate(
                aggregation,
                DiscountReport.class,
                DiscountConsumptionInfo.class
        );

       return groupResult.getMappedResults();
    }

    private void addDiscountConsumptionInfoToDiscountDetail(DiscountDetail discountDetail, List<DiscountConsumptionInfo> consumptionInfos) {

        if (consumptionInfos.size() > 0) {

            DiscountConsumptionInfo consumptionInfo = consumptionInfos.get(0);

            // Set discount usage count and set total discount summation
            discountDetail.setTotalUsageCount(consumptionInfo.getUsageCount());
            discountDetail.setCalculatedDiscountSummation(consumptionInfo.getCalculatedDiscountSummation());

            // Set discount consumers
            List<KoochitaUser> consumers = new ArrayList<>();
            for (String consumerId : consumptionInfo.getConsumersId()) {
                consumers.add(koochitaUserService.fetchKoochitaUserById(consumerId));
            }
            discountDetail.setConsumers(consumers);
        }

    }
}

package bogen.studio.Room.Repository;

import bogen.studio.Room.documents.DiscountReport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DiscountReportRepository {

    private final MongoTemplate mongoTemplate;

    public DiscountReport insert(DiscountReport discountReport) {

        return mongoTemplate.insert(
                discountReport,
                mongoTemplate.getCollectionName(DiscountReport.class)
        );
    }



}

package bogen.studio.Room.Repository;

import bogen.studio.Room.documents.FinancialReport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinancialReportRepository {

    private final MongoTemplate mongoTemplate;

    public FinancialReport insert(FinancialReport r) {
        /* This method inserts financial report in the database */

        return mongoTemplate.insert(
                r,
                mongoTemplate.getCollectionName(FinancialReport.class));
    }

}

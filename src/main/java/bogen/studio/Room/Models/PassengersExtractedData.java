package bogen.studio.Room.Models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PassengersExtractedData {
    /* This class will contain extracted data from passenger and creators JSON */

    private int adults;
    private int children;
    private int infants;
    List<Document> passengersInfo;

}

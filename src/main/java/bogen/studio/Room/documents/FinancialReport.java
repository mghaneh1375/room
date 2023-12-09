package bogen.studio.Room.documents;

import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Models.PassengerInfo;
import bogen.studio.Room.Models.ReservationCreatorInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(value = "financial-report")
public class FinancialReport {

    @Id
    private String _id;

    private ObjectId reservationId;
    private LocalDateTime purchaseTime;
    private List<LocalDateTime> residenceDates;
    private ReservationCreatorInfo reservationCreatorInfo;
    private List<PassengerInfo> passengersInfo;
    private ReservationStatus status;
    private String description;
    private int numberOfPassengers;
    private int paymentFee;
    private int refundFee;

}

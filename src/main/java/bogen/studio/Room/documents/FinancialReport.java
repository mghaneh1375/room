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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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

    @CreatedDate
    @Field(value = "created-at")
    private LocalDateTime createdAt;

    @Field(value = "reservation-id")
    private String reservationId;

    @Field(value = "room-id")
    private String roomId;

    @Field(value = "purchase-time")
    private LocalDateTime purchaseTime;

    @Field(value = "residence-dates")
    private List<LocalDateTime> residenceDates;

    @Field(value = "residence-start-date")
    private LocalDateTime residenceStartDate;

    @Field(value = "residence-end-date")
    private LocalDateTime residenceEndDate;

    @Field(value = "reservation-creator-info")
    private ReservationCreatorInfo reservationCreatorInfo;

    @Field(value = "passengers-info")
    private List<PassengerInfo> passengersInfo;

    private ReservationStatus status;
    private String description;

    @Field(value = "number-of-passengers")
    private int numberOfPassengers;

    @Field(value = "payment-fee")
    private int paymentFee;

    @Field(value = "refund-fee")
    private int refundFee;

}

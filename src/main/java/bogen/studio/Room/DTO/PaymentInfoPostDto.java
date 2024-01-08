package bogen.studio.Room.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PaymentInfoPostDto {

    private ObjectId reservationRequestId;
    private String paymentCode;

}

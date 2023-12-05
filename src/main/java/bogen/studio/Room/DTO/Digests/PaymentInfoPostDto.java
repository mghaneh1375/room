package bogen.studio.Room.DTO.Digests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfoPostDto {

    ObjectId reservationRequestId;

}

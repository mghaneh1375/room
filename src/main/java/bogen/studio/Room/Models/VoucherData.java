package bogen.studio.Room.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class VoucherData {

    private String boomAddress;
    private String roomName;
    private String JalaliResidenceStartDate;
    private int numberOfStayingNights;
    private String trackingCode;

}

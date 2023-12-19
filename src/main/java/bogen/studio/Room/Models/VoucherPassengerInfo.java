package bogen.studio.Room.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class VoucherPassengerInfo {

    private int index;
    private String nameFa;
    private String lastNameFa;
    private String phone;

}

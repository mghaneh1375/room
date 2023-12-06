package bogen.studio.Room.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ReservationCreatorInfo {

    private String nameFa;
    private String nameEn;
    private String lastNameFa;
    private String lastNameEn;
    private String NID;
    private String passportNo;
    private String citizenNo;
    private String stayStatus;
    private String citizenship;
    private String ageType;
    private String sex;
    private String mail;
    private String phone;

}

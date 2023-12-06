package bogen.studio.Room.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Access;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class GuestCountGetDto {

    private int adultCount;
    private int childrenCount;
    private int infantCount;

}

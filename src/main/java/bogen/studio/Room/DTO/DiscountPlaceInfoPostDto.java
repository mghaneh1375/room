package bogen.studio.Room.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DiscountPlaceInfoPostDto {

    private String boomId;
    private String roomName;

}

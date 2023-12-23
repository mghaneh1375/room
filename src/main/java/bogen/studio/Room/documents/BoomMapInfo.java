package bogen.studio.Room.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Column;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Document(value = "boom-map-info")
public class BoomMapInfo {

    @Id
    private String _id;

    @Indexed(unique = true)
    @Field(name = "boom_id")
    private ObjectId boomId;

    @Field(name = "map_photo_path")
    private String mapPath;

    private Double latitude;
    private Double longitude;

    private String address;

}

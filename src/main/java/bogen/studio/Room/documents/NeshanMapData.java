package bogen.studio.Room.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Document(value = "neshan_map_data")
public class NeshanMapData {

    @Id
    private String _id;

    @Column(name = "base_url")
    private String baseUrl;
    private String key;
    private String type;
    private int zoom;
    private int width;
    private int height;

    @Column(name = "marker_token")
    private String markerToken;

}

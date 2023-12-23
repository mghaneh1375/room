package bogen.studio.Room.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Document(value = "place")
public class Place {
    /* Attention: Place Doc has very fields and is managed by another backend application. Here we have defined the only
     * fields, which we need in Room backend application */

    @Id
    private ObjectId _id;

    private Double c; // As latitude
    private Double d; // As longitude
    private String address;

}

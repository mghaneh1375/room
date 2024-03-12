package bogen.studio.Room.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "state")
public class Province {

    @Id
    private String _id;

    private String name;

}

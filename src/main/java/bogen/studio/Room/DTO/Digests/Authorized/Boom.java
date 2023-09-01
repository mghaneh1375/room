package bogen.studio.Room.DTO.Digests.Authorized;

import org.bson.types.ObjectId;

import java.util.Date;

public class Boom {

    private ObjectId _id;

    private boolean availability;
    private boolean visibility;
    private Date createdAt;

    private ObjectId user_id;

}

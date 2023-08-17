package bogen.studio.Room.Repository;

import bogen.studio.Room.DTO.Digests.Authorized.Boom;
import bogen.studio.Room.Models.Room;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends MongoRepository<Room, ObjectId>, FilterableRepository<Room, Boom> {}
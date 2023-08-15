package bogen.studio.Room.Repository;

import bogen.studio.Room.Models.Boom;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoomRepository extends MongoRepository<Boom, ObjectId>, FilterableRepository<Boom> {}
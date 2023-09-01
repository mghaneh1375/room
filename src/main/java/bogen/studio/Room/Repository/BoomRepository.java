package bogen.studio.Room.Repository;

import bogen.studio.Room.Models.Boom;
import bogen.studio.Room.Models.Room;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoomRepository extends
        MongoRepository<Boom, ObjectId>, FilterableRepository<Boom, bogen.studio.Room.DTO.Digests.Authorized.Boom> {

    @Query(value = "{ 'userId' : ?0 }", fields = "{ '_id': 1, 'availability': 1, 'createdAt': 1, 'place_id': 1 }")
    List<Boom> findByUserIdIncludeEmbeddedFields(ObjectId userId);

    @Query(value = "{ '_id': ?0 }")
    Optional<Boom> findById(ObjectId id);
}
package bogen.studio.Room.Repository;

import bogen.studio.Room.Models.Boom;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoomRepository extends
        MongoRepository<Boom, ObjectId>, FilterableRepository<Boom, bogen.studio.Room.DTO.Digests.Authorized.Boom> {

    @Query(value = "{ 'userId' : ?0 }", fields = "{ '_id': 1, 'availability': 1, 'visibility': 1, 'createdAt': 1 }")
    List<Boom> findByUserIdIncludeEmbeddedFields(Integer userId);

}
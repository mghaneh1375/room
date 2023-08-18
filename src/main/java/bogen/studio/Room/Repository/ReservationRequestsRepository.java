package bogen.studio.Room.Repository;

import bogen.studio.Room.DTO.Digests.Authorized.Boom;
import bogen.studio.Room.Models.ReservationRequests;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRequestsRepository extends MongoRepository<ReservationRequests, ObjectId>, FilterableRepository<ReservationRequests, Boom> {

    //todo: reserved or accept
    @Query(value = "{ 'room_id' : ?0, 'status': 'PENDING', 'nights': {$in:  ?1} }", fields = "{ '_id': 1, 'availability': 1, 'visibility': 1, 'createdAt': 1 }")
    List<ReservationRequests> findActiveReservations(ObjectId roomId, List<String> dates);

}
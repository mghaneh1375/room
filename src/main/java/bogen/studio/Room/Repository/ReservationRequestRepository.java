package bogen.studio.Room.Repository;

import bogen.studio.Room.DTO.Digests.Authorized.Boom;
import bogen.studio.Room.Models.ReservationRequest;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRequestRepository extends MongoRepository<ReservationRequest, ObjectId>, FilterableRepository<ReservationRequest, Boom> {

    //todo: reserved or accept
    @Query(value = "{ 'room_id' : ?0, $or: [ { 'status': 'RESERVED' }, { 'status': 'PENDING' }, { 'status': 'PAID' }, { 'status': 'ACCEPT' } ], 'prices.date': {$in:  ?1} }", count = true)
    Integer findActiveReservations(ObjectId roomId, List<String> dates);

    @Query(value = "{ 'room_id': ?0, 'status': ?1 }", count = true)
    Integer countByRoomIdAndStatus(ObjectId roomId, String status);

    @Query(value = "{ 'room_id': ?0,  $or: [ { 'status': 'PENDING' }, { 'status': 'RESERVED' }, { 'status': 'PAID' }, { 'status': 'FINISH' }, { 'status': 'ACCEPT' }, { 'status': 'REFUND' } ] }", count = true)
    Integer countAllActiveReservationsByRoomId(ObjectId roomId);

    @Query(value = "{ 'owner_id': ?0, 'status': ?1 }", count = true)
    Integer countByOwnerIdAndStatus(ObjectId userId, String status);

    @Query(value = "{ 'reserve_expire_at': { $lt: ?0 }, $or: [ { 'status': 'RESERVED' }, { 'status': 'PENDING' }, { 'status': 'ACCEPT' } ] }")
    List<ReservationRequest> getExpiredReservations(Long curr);

    @Query(value = "{ 'user_id': ?0, $or: [ { 'status': 'PENDING' }, { 'status': 'PAID' }, { 'status': 'ACCEPT' } ] }",
            fields = "{ 'passengers_id': 0, 'owner_id': 0, 'user_id': 0  }")
    List<ReservationRequest> getActiveReservationsByUserId(ObjectId userId);

    @Query(value = "{ 'room_id': ?0, 'owner_id': ?1, $or: [ { 'status': 'PENDING' }, { 'status': 'PAID' }, { 'status': 'ACCEPT' } ] }",
            fields = "{ 'owner_id': 0, 'user_id': 0, 'room_id': 0, 'passengersId': 0  }")
    List<ReservationRequest> getActiveReservationsByRoomIdAndOwnerId(ObjectId roomId, ObjectId ownerId);

    @Query(value = "{ 'owner_id': ?0, $or: [ { 'status': 'PENDING' }, { 'status': 'PAID' }, { 'status': 'ACCEPT' } ] }",
            fields = "{ 'owner_id': 0, 'user_id': 0, 'passengersId': 0  }")
    List<ReservationRequest> getActiveReservationsByOwnerId(ObjectId ownerId);

    @Query(value = "{ 'user_id': ?0, '_id': ?1 }",
            fields = "{ 'passengers_id': 0, 'owner_id': 0, 'user_id': 0  }")
    ReservationRequest getReservationsByUserIdAndId(ObjectId userId, ObjectId id);

    @Query(value = "{ 'user_id': ?0, 'tracking_code': ?1 }",
            fields = "{ 'passengers_id': 0, 'owner_id': 0, 'user_id': 0  }")
    ReservationRequest getReservationsByUserIdAndTrackingCode(ObjectId userId, String trackingCode);

}
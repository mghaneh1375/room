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
    @Query(value = "{ 'room_id' : ?0, $or: [ { 'status': 'RESERVED' }, { 'status': 'PENDING' }, { 'status': 'PAID' }, { 'status': 'ACCEPT' } ], 'prices.date': {$in:  ?1} }", count = true)
    Integer findActiveReservations(ObjectId roomId, List<String> dates);

    @Query(value = "{ 'room_id': ?0, 'status': ?1 }", count = true)
    Integer countByRoomIdAndStatus(ObjectId roomId, String status);

    @Query(value = "{ 'room_id': ?0,  $or: [ { 'status': 'PENDING' }, { 'status': 'RESERVED' }, { 'status': 'PAID' }, { 'status': 'FINISH' }, { 'status': 'ACCEPT' }, { 'status': 'REFUND' } ] }", count = true)
    Integer countAllActiveReservationsByRoomId(ObjectId roomId);

    @Query(value = "{ 'owner_id': ?0, 'status': ?1 }", count = true)
    Integer countByOwnerIdAndStatus(int userId, String status);

    @Query(value = "{ 'reserve_expire_at': { $lt: ?0 }, $or: [ { 'status': 'RESERVED' }, { 'status': 'PENDING' }, { 'status': 'ACCEPT' } ] }")
    List<ReservationRequests> getExpiredReservations(Long curr);

    //todo: user_id filter
    // 'user_id': ?0,
    @Query(value = "{ $or: [ { 'status': 'PENDING' }, { 'status': 'PAID' }, { 'status': 'ACCEPT' } ] }",
            fields = "{ 'passengers_id': 0, 'owner_id': 0, 'user_id': 0  }")
    List<ReservationRequests> getActiveReservationsByUserId(ObjectId userId);

    //todo: user_id filter
    // 'user_id': ?0,
    @Query(value = "{ '_id': ?1 }",
            fields = "{ 'passengers_id': 0, 'owner_id': 0, 'user_id': 0  }")
    ReservationRequests getReservationsByUserIdAndId(ObjectId userId, ObjectId id);

    //todo: user_id filter
    // 'user_id': ?0,
    @Query(value = "{ 'tracking_code': ?1 }",
            fields = "{ 'passengers_id': 0, 'owner_id': 0, 'user_id': 0  }")
    ReservationRequests getReservationsByUserIdAndTrackingCode(ObjectId userId, String trackingCode);

}
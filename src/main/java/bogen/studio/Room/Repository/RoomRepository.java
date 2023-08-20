package bogen.studio.Room.Repository;

import bogen.studio.Room.Models.Room;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends MongoRepository<Room, ObjectId>, FilterableRepository<Room, bogen.studio.Room.DTO.Digests.Authorized.Room> {

    @Query(value = "{ 'date_prices.0': {$exists: true} }")
    List<Room> findHasDatePrices();

    @Query(value = "{ 'user_id': ?0, 'boom_id': ?1 }",
            fields = "{ '_id': 1, 'availability': 1, 'created_at': 1, 'title': 1, 'image': 1 , 'cap': 1, 'price': 1, 'max_cap': 1, 'cap_price': 1, 'online_reservation': 1}"
    )
    List<Room> findByUserIdAndBoomId(int userId, ObjectId boomId);

    @Query(value = "{ 'availability': true, 'boom_id': ?0 }",
            fields = "{ '_id': 1, 'title': 1, 'availability': 1, 'image': 1 , 'cap': 1, 'price': 1, 'max_cap': 1, 'cap_price': 1, 'online_reservation': 1, 'food_facilities': 1, 'limitations': 1, 'welfares': 1, 'sleep_features': 1, 'additional_facilities': 1, 'accessibility_features': 1}"
    )
    List<Room> findByBoomId(ObjectId boomId);

    @Query(value = "{ 'boom_id': ?0 }", count = true)
    Integer countRoomByBoomId(ObjectId boomId);

    @Query(value = "{ 'boom_id': ?0, 'title': ?1 }", count = true)
    Integer countRoomByBoomIdAndTitle(ObjectId boomId, String title);

    @Query(value = "{ '_id': { $in: ?0 } }", fields = "{ 'title': 1, 'image': 1 }")
    List<Room> findDigestByIds(List<ObjectId> ids);

}
package bogen.studio.Room.Repository;

import my.common.commonkoochita.Model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for the {@link User} JPA entity. Any custom methods, not
 * already defined in {@link MongoRepository}, are to be defined here
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

	@Query(value = "{'username' : ?0 }")
	Optional<User> findByUsername(String username);

	@Query(value = "{'email' : ?0 }")
	Optional<User> findByEmail(String email);

}

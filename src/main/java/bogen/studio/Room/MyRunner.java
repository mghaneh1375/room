package bogen.studio.Room;

import bogen.studio.Room.Models.CommonUser;
import bogen.studio.Room.Models.TargetDateDiscountDetail;
import bogen.studio.Room.Repository.UserRepository;
import bogen.studio.Room.Service.DiscountService;
import bogen.studio.Room.Service.RoomService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MyRunner implements CommandLineRunner {

    private final DiscountService discountService;
    private final RoomService roomService;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {

//
//        Optional<CommonUser> commonUserOptional = userRepository.findById(new ObjectId("607f046bdb19380d1ef94428"));
//        System.out.println(commonUserOptional.get());


    }
}

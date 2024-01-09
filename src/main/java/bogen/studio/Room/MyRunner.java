package bogen.studio.Room;

import bogen.studio.Room.Models.TargetDateDiscountDetail;
import bogen.studio.Room.Service.DiscountService;
import bogen.studio.Room.Service.RoomService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MyRunner implements CommandLineRunner {

    private final DiscountService discountService;
    private final RoomService roomService;

    @Override
    public void run(String... args) throws Exception {






    }
}

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


        LocalDateTime targetDate = LocalDateTime.of(2024, 1, 3, 0, 0, 0, 0);
        TargetDateDiscountDetail maxDis = discountService.getMaximumDiscountForTargetDate(new ObjectId("64df8ced6b21b94de607c441"), "اتاق رویال", targetDate, 1, 1000L);
        //discountService.fetchRelatedDiscounts(new ObjectId("64df8ced6b21b94de607c441"),"اتاق رویال", targetDate);
        System.out.println("**Finally: ");
        System.out.println(maxDis);




    }
}

package bogen.studio.Room;

import bogen.studio.Room.Service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MyRunner implements CommandLineRunner {

    private final DiscountService discountService;
    @Override
    public void run(String... args) throws Exception {


        LocalDateTime targetDate = LocalDateTime.of(2024, 1, 1, 0, 0, 0, 0);
        discountService.calculateDiscountForTargetDate(new ObjectId("64df8ced6b21b94de607c441"), "اتاق رویال", targetDate, 1000L);
        //discountService.fetchRelatedDiscounts(new ObjectId("64df8ced6b21b94de607c441"),"اتاق رویال", targetDate);

    }
}

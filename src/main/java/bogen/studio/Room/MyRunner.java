package bogen.studio.Room;

import bogen.studio.Room.Enums.DiscountPlace;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MyRunner implements CommandLineRunner {


    @Override
    public void run(String... args) throws Exception {

        DiscountPlace discountPlace = DiscountPlace.BOOM_DISCOUNT;

        System.out.println("** Hello");
        System.out.println("**: " + discountPlace.gtPersianValue());

    }
}

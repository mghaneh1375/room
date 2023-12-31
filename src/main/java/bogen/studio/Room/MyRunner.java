package bogen.studio.Room;

import bogen.studio.Room.Enums.DiscountPlace;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MyRunner implements CommandLineRunner {


    @Override
    public void run(String... args) throws Exception {

        int a = 10;

        if (a > 0) {
            System.out.println("a > 0");
        } else if (a > 5) {
            System.out.println("a > 5");
        }

    }
}

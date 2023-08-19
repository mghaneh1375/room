package bogen.studio.Room.Utility;

import bogen.studio.Room.DTO.DatePrice;
import bogen.studio.Room.Models.Room;
import bogen.studio.Room.Repository.RoomRepository;

import java.util.*;

import static bogen.studio.Room.Utility.StaticValues.ONE_DAY_MSEC;

public class Jobs implements Runnable {


    RoomRepository roomRepository;

    public Jobs(RoomRepository r) {
        roomRepository = r;
    }

    @Override
    public void run() {
        Timer timer = new Timer();
        timer.schedule(new RemoveRedundantDatePrices(), 0, ONE_DAY_MSEC);
    }

    private class RemoveRedundantDatePrices extends TimerTask {

        @Override
        public void run() {

            List<Room> rooms = roomRepository.findHasDatePrices();
            List<Room> modified = new ArrayList<>();

            int today = Utility.convertStringToDate(Utility.getToday("/"));

            for(Room room : rooms) {

                List<DatePrice> datePrices = room.getDatePrices();
                boolean modify = false;

                Iterator<DatePrice> i = datePrices.iterator();

                while (i.hasNext()) {
                    DatePrice datePrice = i.next();

                    int d = Utility.convertStringToDate(datePrice.getDate());

                    if(d < today) {
                        modify = true;
                        i.remove();
                    }
                }

                if(modify)
                    modified.add(room);
            }

            roomRepository.saveAll(modified);
        }
    }

}

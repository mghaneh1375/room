package bogen.studio.Room.Utility;

import bogen.studio.Room.DTO.DatePrice;
import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Models.ReservationRequest;
import bogen.studio.Room.Models.Room;
import bogen.studio.Room.Repository.ReservationRequestsRepository;
import bogen.studio.Room.Repository.RoomRepository;

import java.util.*;

import static my.common.commonkoochita.Utility.Statics.ONE_DAY_MSEC;
import static my.common.commonkoochita.Utility.Utility.convertStringToDate;
import static my.common.commonkoochita.Utility.Utility.getToday;

public class Jobs implements Runnable {


    RoomRepository roomRepository;
    ReservationRequestsRepository reservationRequestsRepository;

    public Jobs(RoomRepository r, ReservationRequestsRepository rr) {
        roomRepository = r;
        reservationRequestsRepository = rr;
    }

    @Override
    public void run() {
        Timer timer = new Timer();
        timer.schedule(new RemoveRedundantDatePrices(), 0, ONE_DAY_MSEC);
        timer.schedule(new CheckReservationStatus(), 0, 1000 * 60 * 60);
    }

    private class RemoveRedundantDatePrices extends TimerTask {

        @Override
        public void run() {

            List<Room> rooms = roomRepository.findHasDatePrices();
            List<Room> modified = new ArrayList<>();

            int today = convertStringToDate(getToday("/"));

            for(Room room : rooms) {

                List<DatePrice> datePrices = room.getDatePrices();
                boolean modify = false;

                Iterator<DatePrice> i = datePrices.iterator();

                while (i.hasNext()) {
                    DatePrice datePrice = i.next();

                    int d = convertStringToDate(datePrice.getDate());

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

    private class CheckReservationStatus extends TimerTask {

        @Override
        public void run() {

            List<ReservationRequest> reservationRequests = reservationRequestsRepository
                    .getExpiredReservations(System.currentTimeMillis());

            for(ReservationRequest reservationRequest : reservationRequests) {
                if(reservationRequest.getStatus().getName()
                        .equalsIgnoreCase(ReservationStatus.ACCEPT.getName())
                )
                    reservationRequest.setStatus(ReservationStatus.ACCEPT_CANCELED);
                else if(reservationRequest.getStatus().getName()
                        .equalsIgnoreCase(ReservationStatus.PENDING.getName()))
                    reservationRequest.setStatus(ReservationStatus.REJECT);
                else
                    reservationRequest.setStatus(ReservationStatus.CANCELED);
            }

            reservationRequestsRepository.saveAll(reservationRequests);
        }
    }

}

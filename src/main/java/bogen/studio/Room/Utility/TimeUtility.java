package bogen.studio.Room.Utility;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class TimeUtility {

    public static List<LocalDateTime> createLocalDateTimeList(LocalDateTime startDay, int days) {

        // Set time of start date to 00:00:00
        LocalDateTime startDayModified = startDay.with(LocalDateTime.of(startDay.getYear(), startDay.getMonth(), startDay.getDayOfMonth(), 0, 0, 0, 0));

        List<LocalDateTime> output = new ArrayList<>();

        for (int i = 0; i < days; i++) {

            output.add(startDayModified.plusDays(i));
        }

        return output;

    }
    public static Instant calculateTimeOutThreshold(int timeoutInMinutes) {
        /* Calculate instance of time out, since created_at filed of reservation request document has a type of Date.
         * Obviously, for avoiding side effects I did not change the type :)  */

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(timeoutInMinutes);
        ZonedDateTime timeoutThresholdZoned = timeoutThreshold.atZone(ZoneId.systemDefault());

        return timeoutThresholdZoned.toInstant();
    }


}

package bogen.studio.Room.Utility;


import java.time.LocalDateTime;
import java.time.LocalTime;
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

}

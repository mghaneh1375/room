package bogen.studio.Room.Utility;


import my.common.commonkoochita.Utility.JalaliCalendar;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class TimeUtility {

    public static List<LocalDateTime> createDatesForRoomDateReservationDocs(LocalDateTime startDay, int days) {
        /* This method creates a list containing dates, for which RoomDateReservationStatus docs need to be created. */


        // Set time of start date to 00:00:00
        LocalDateTime startDayModified = startDay.with(LocalDateTime.of(startDay.getYear(), startDay.getMonth(), startDay.getDayOfMonth(), 0, 0, 0, 0));

        List<LocalDateTime> output = new ArrayList<>();

        for (int i = 0; i < days; i++) {

            output.add(startDayModified.plusDays(i));
        }

        return output;

    }

    public static Instant calculateCreatedAtExpirationTimoutInInstance(int timeoutInMinutes) {
        /* Calculate instance of time out, since created_at filed of reservation request document has a type of Date.
         * Obviously, for avoiding side effects I did not change the type :)  */

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(timeoutInMinutes);
        ZonedDateTime timeoutThresholdZoned = timeoutThreshold.atZone(ZoneId.systemDefault());

        return timeoutThresholdZoned.toInstant();
    }

    public static LocalDateTime getExactStartTimeOfToday() {
        /* This method returns exact start time of today */

        LocalDateTime now = LocalDateTime.now();

        return LocalDateTime.of(
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                0,
                0,
                0,
                0
        );

    }

    public static LocalDateTime convertStringToLdt(
            String dateInString,
            String datePatternInString
    ) throws DateTimeParseException {
        /* Convert input string to local_date_time according to input date pattern.
         * Attention this method throws DateTimeParseException and should be called in try catch block */

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePatternInString);
        return LocalDateTime.parse(dateInString, formatter);
    }

    public static List<LocalDateTime> convertJalaliDatesListToGregorian(List<String> jalaliDates) {
        /* Input dates format: 1402/09/21 */

        List<LocalDateTime> output = new ArrayList<>();

        for (String date : jalaliDates) {

            String[] jalaliDateValues = date.split("/");
            JalaliCalendar.YearMonthDate gregorianDate = JalaliCalendar.jalaliToGregorian(new JalaliCalendar.YearMonthDate(jalaliDateValues[0], jalaliDateValues[1], jalaliDateValues[2]));
            int year = gregorianDate.getYear();
            int month = gregorianDate.getMonth() + 1;
            int day = gregorianDate.getDate();
            output.add(LocalDateTime.of(year, month, day, 0, 0, 0, 0));
        }

        return output;

    }


}

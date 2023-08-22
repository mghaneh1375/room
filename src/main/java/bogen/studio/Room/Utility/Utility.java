package bogen.studio.Room.Utility;

import org.json.JSONObject;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static bogen.studio.Room.Utility.StaticValues.ONE_DAY_MSEC;

public class Utility {

    public static String convertDateToJalali(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String[] dateTime = simpleDateFormat.format(date).split(" ");
        String[] splited = dateTime[0].split("-");
        return JalaliCalendar.gregorianToJalali(new JalaliCalendar.YearMonthDate(splited[0], splited[1], splited[2])).format("/") + " - " + dateTime[1];
    }

    public static String convertDateToJalali(long time) {
        Date d = new Date(time);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String[] dateTime = simpleDateFormat.format(d).split(" ");
        String[] splited = dateTime[0].split("-");
        return JalaliCalendar.gregorianToJalali(new JalaliCalendar.YearMonthDate(splited[0], splited[1], splited[2])).format("/") + " - " + dateTime[1];
    }

    public static String getPast(String delimeter, String solarDate, int days) {

        Locale loc = new Locale("en_US");
        String[] splited = solarDate.split("\\/");

        JalaliCalendar.YearMonthDate tmp =
                JalaliCalendar.jalaliToGregorian(new JalaliCalendar.YearMonthDate(splited[0], splited[1], splited[2]));

        DateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);

        Date d = null;
        try {
            d = format.parse(tmp.toString());

            SolarCalendar sc = new SolarCalendar(new Date(d.getTime() - ONE_DAY_MSEC * days));
            return String.valueOf(sc.year) + delimeter + String.format(loc, "%02d",
                    sc.month) + delimeter + String.format(loc, "%02d", sc.date);

        } catch (ParseException e) {
            return null;
        }

    }

    public static String getPast(String delimeter, int days) {
        Locale loc = new Locale("en_US");
        SolarCalendar sc = new SolarCalendar(-ONE_DAY_MSEC * days);
        return String.valueOf(sc.year) + delimeter + String.format(loc, "%02d",
                sc.month) + delimeter + String.format(loc, "%02d", sc.date);
    }

    public static String getToday(String delimeter) {
        Locale loc = new Locale("en_US");
        SolarCalendar sc = new SolarCalendar();
        return String.valueOf(sc.year) + delimeter + String.format(loc, "%02d",
                sc.month) + delimeter + String.format(loc, "%02d", sc.date);
    }

    public static int convertStringToDate(String date) {
        return Integer.parseInt(date.substring(0, 4) + date.substring(5, 7) + date.substring(8, 10));
    }

    public static String convertPersianDigits(String number) {

        char[] chars = new char[number.length()];
        for (int i = 0; i < number.length(); i++) {

            char ch = number.charAt(i);

            if (ch >= 0x0660 && ch <= 0x0669)
                ch -= 0x0660 - '0';
            else if (ch >= 0x06f0 && ch <= 0x06F9)
                ch -= 0x06f0 - '0';

            chars[i] = ch;
        }

        return new String(chars);
    }

    public static JSONObject convertPersian(JSONObject jsonObject) {

        for (String key : jsonObject.keySet()) {
            if(key.toLowerCase().contains("password") ||
                    key.equalsIgnoreCase("code") ||
                    key.equals("NID")
            )
                jsonObject.put(key, Utility.convertPersianDigits(jsonObject.get(key).toString()));
            else if (jsonObject.get(key) instanceof Integer)
                jsonObject.put(key, Integer.parseInt(Utility.convertPersianDigits(jsonObject.getInt(key) + "")));
            else if (jsonObject.get(key) instanceof String) {
                String str = Utility.convertPersianDigits(jsonObject.getString(key));
                if(str.charAt(0) == '0' ||
                        key.equalsIgnoreCase("phone") ||
                        key.equalsIgnoreCase("tel") ||
                        key.equals("NID")
                )
                    jsonObject.put(key, str);
                else {
                    try {
                        jsonObject.put(key, Integer.parseInt(str));
                    } catch (Exception x) {
                        jsonObject.put(key, str);
                    }
                }
            }
        }

        return jsonObject;
    }

    public static String generateErr(String msg) {
        return new JSONObject()
                .put("status", "nok")
                .put("msg", msg)
                .toString();
    }

    public static String generateErr(String msg, PairValue... pairValues) {

        JSONObject jsonObject = new JSONObject()
                .put("status", "nok")
                .put("msg", msg);

        for (PairValue p : pairValues)
            jsonObject.put(p.getKey().toString(), p.getValue());

        return jsonObject.toString();
    }

    public static String generateSuccessMsg(String key, Object val, PairValue... pairValues) {

        JSONObject jsonObject = new JSONObject()
                .put("status", "ok")
                .put(key, val);

        for (PairValue p : pairValues)
            jsonObject.put(p.getKey().toString(), p.getValue());

        return jsonObject.toString();

    }

    public static void printException(Exception x) {

        System.out.println(x.getMessage());
        int limit = x.getStackTrace().length > 5 ? 5 : x.getStackTrace().length;
        for (int i = 0; i < limit; i++)
            System.out.println(x.getStackTrace()[i]);

    }

    public static boolean isLargerThanToday(String date) {
        int d = Utility.convertStringToDate(date);
        int today = Utility.convertStringToDate(Utility.getToday("/"));
        return today < d;
    }

    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static SecureRandom rnd = new SecureRandom();

    public static String randomString(int len) {

        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));

        return sb.toString();
    }
}

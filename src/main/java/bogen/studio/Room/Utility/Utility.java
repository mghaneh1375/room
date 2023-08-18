package bogen.studio.Room.Utility;

import org.bson.Document;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static bogen.studio.Room.Utility.StaticValues.ONE_DAY_MSEC;

public class Utility {

    private static final Pattern justNumPattern = Pattern.compile("^\\d+$");
    private static final Pattern passwordStrengthPattern = Pattern.compile("^(?=.*[0-9])(?=.*[A-z])(?=\\S+$).{8,}$");

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

    public static Document searchInDocumentsKeyVal(List<Document> arr, String key, Object val) {

        if (arr == null)
            return null;

        for (Document doc : arr) {
            if (doc.containsKey(key) && doc.get(key).equals(val))
                return doc;
        }

        return null;
    }

    public static Document searchInDocumentsKeyVal(List<Document> arr, String key, Object val,
                                                   String key2, Object val2) {

        if (arr == null)
            return null;

        for (Document doc : arr) {
            if (doc.containsKey(key) && doc.get(key).equals(val) &&
                    doc.containsKey(key2) && (
                    (val2 == null && doc.get(key2) == null) ||
                            (doc.get(key2) != null && doc.get(key2).equals(val2))
            ))
                return doc;
        }

        return null;
    }

    public static int searchInDocumentsKeyValIdx(List<Document> arr, String key, Object val,
                                                 String key2, Object val2) {

        if (arr == null)
            return -1;

        for (int i = 0; i < arr.size(); i++) {
            Document doc = arr.get(i);
            if (doc.containsKey(key) && doc.get(key).equals(val) && (
                    (val2 == null && doc.get(key2) == null) ||
                            (doc.get(key2) != null && doc.get(key2).equals(val2))
            ))
                return i;
        }

        return -1;
    }

    public static int searchInDocumentsKeyValIdx(List<Document> arr, String key, Object val) {

        if (arr == null)
            return -1;

        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i).containsKey(key) && arr.get(i).get(key).equals(val))
                return i;
        }

        return -1;
    }

    public static String convertStringToDate(String date, String delimeter) {
        return date.substring(0, 4) + delimeter + date.substring(4, 6) + delimeter + date.substring(6, 8);
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

    public static boolean validationNationalCode(String code) {

        if (code.length() != 10)
            return false;

        try {
            long nationalCode = Long.parseLong(code);
            byte[] arrayNationalCode = new byte[10];

            //extract digits from number
            for (int i = 0; i < 10; i++) {
                arrayNationalCode[i] = (byte) (nationalCode % 10);
                nationalCode = nationalCode / 10;
            }

            //Checking the control digit
            int sum = 0;
            for (int i = 9; i > 0; i--)
                sum += arrayNationalCode[i] * (i + 1);
            int temp = sum % 11;
            if (temp < 2)
                return arrayNationalCode[0] == temp;
            else
                return arrayNationalCode[0] == 11 - temp;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static void printException(Exception x) {

        System.out.println(x.getMessage());
        int limit = x.getStackTrace().length > 5 ? 5 : x.getStackTrace().length;
        for (int i = 0; i < limit; i++)
            System.out.println(x.getStackTrace()[i]);

    }
}

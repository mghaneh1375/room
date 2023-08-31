package bogen.studio.Room.Utility;

import com.mongodb.BasicDBObject;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StaticValues {

//    public final static String PASSENGER_URL = "http://localhost:8091/api/";
//    public final static String ASSET_URL = "http://localstaticgach.com/";

    public final static String PASSENGER_URL = "https://passenger.bogenstudio.com/api/";
    public final static String ASSET_URL = "https://static.koochita.com/";

//    public final static ObjectId FAKE_USER_ID = new ObjectId("64e0af975bfd9f7e5ec45dcf");
    public final static ObjectId FAKE_USER_ID = new ObjectId("607f046bdb19380d1ef9442c");

    public final static long ONE_DAY_MSEC = 60 * 60 * 24 * 1000;

    public final static long BANK_WAIT_MSEC = 60 * 1000 * 15;
    public final static long ACCEPT_PENDING_WAIT_MSEC = 60 * 1000 * 15;
    public final static long PAY_WAIT_MSEC = 60 * 1000 * 15;

    public static final int MAX_FILE_SIZE = 5 * 1024 * 1024;

    public final static long TOKEN_EXPIRATION_MSEC = ONE_DAY_MSEC * 7;
    public final static int TOKEN_EXPIRATION = 60 * 60 * 24 * 7;
    public final static long SERVER_TOKEN_EXPIRATION_MSEC = 20 * 1000; // 20 s

    public final static List<Integer> weekends = new ArrayList<Integer>(){{add(7);}};

    public static HashMap<String, Boolean> fetchedHolidays = new HashMap<>();

    public final static BasicDBObject USER_DIGEST = new BasicDBObject("_id", 1)
            .append("first_name", 1)
            .append("last_name", 1)
            .append("NID", 1)
            .append("pic", 1);

    public static final BasicDBObject JUST_ID = new BasicDBObject("_id", 1);

    public static final String JSON_OK = new JSONObject().put("status", "ok").toString();
    public static final String JSON_NOT_VALID_TOKEN = new JSONObject().put("status", "nok").put("msg", "token is not valid").toString();
    public static final String JSON_NOT_ACCESS = new JSONObject().put("status", "nok").put("msg", "no access to this method").toString();
    public static final String JSON_NOT_VALID = new JSONObject().put("status", "nok").put("msg", "json not valid").toString();
    public static final String JSON_NOT_VALID_ID = new JSONObject().put("status", "nok").put("msg", "id is not valid").toString();
    public static final String JSON_NOT_VALID_PARAMS = new JSONObject().put("status", "nok").put("msg", "params is not valid").toString();
    public static final String JSON_NOT_UNKNOWN = new JSONObject().put("status", "nok").put("msg", "unknown exception has been occurred").toString();
    public static final String JSON_NOT_VALID_FILE = new JSONObject().put("status", "nok").put("msg", "شما در این قسمت می توانید تنها فایل PDF و یا یک فایل صوتی و یا یک تصویر آپلود نمایید.").toString();
    public static final String JSON_NOT_VALID_6_MB_SIZE = new JSONObject().put("status", "nok").put("msg", "حداکثر حجم مجاز، 6 مگ است.").toString();
    public static final String JSON_UNKNOWN_UPLOAD_FILE = new JSONObject().put("status", "nok").put("msg", "مشکلی در آپلود فایل مورد نظر رخ داده است. لطفا با پشتیبانی تماس بگیرید.").toString();
}

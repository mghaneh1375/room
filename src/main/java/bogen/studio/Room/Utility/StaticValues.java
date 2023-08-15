package bogen.studio.Room.Utility;

import com.mongodb.BasicDBObject;
import org.json.JSONObject;

public class StaticValues {

    public final static int ONE_DAY_MSEC = 60 * 60 * 24 * 1000;

    public static final int MAX_FILE_SIZE = 5 * 1024 * 1024;

    public final static long TOKEN_EXPIRATION_MSEC = 60 * 60 * 24 * 7 * 1000;
    public final static int TOKEN_EXPIRATION = 60 * 60 * 24 * 7;
    public final static long SERVER_TOKEN_EXPIRATION_MSEC = 20 * 1000; // 20 s

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

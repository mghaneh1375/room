package bogen.studio.Room.Utility;

import com.mongodb.BasicDBObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class StaticValues {

    //public final static String PASSENGER_URL = "https://passenger.bogenstudio.com/api/";
    public final static String PASSENGER_URL = "http://localhost:8091/api/";
    public final static String ASSET_URL = "https://static.koochita.com/";

    public final static long BANK_WAIT_MSEC = 60 * 1000 * 15;
    public final static long ACCEPT_PENDING_WAIT_MSEC = 60 * 1000 * 15;
    public final static long PAY_WAIT_MSEC = 60 * 1000 * 15;

    public static final int MAX_FILE_SIZE = 5 * 1024 * 1024;

    public final static List<Integer> weekends = new ArrayList<>() {{
        add(7);
    }};

    public static HashMap<String, Boolean> fetchedHolidays = new HashMap<>();

    public final static BasicDBObject USER_DIGEST = new BasicDBObject("_id", 1)
            .append("first_name", 1)
            .append("last_name", 1)
            .append("NID", 1)
            .append("pic", 1);

}

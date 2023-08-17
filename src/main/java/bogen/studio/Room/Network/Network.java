package bogen.studio.Room.Network;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

public class Network {

    private static final String KOOCHITA_SERVER = "https://koochita-server.bogenstudio.com/api/";

    public static JSONObject sendPostReq(String api, Object data) {

        try {

            HttpResponse<JsonNode> jsonResponse = Unirest.post(KOOCHITA_SERVER + api)
                    .header("content-type", "application/json")
                    .header("accept", "application/json")
                    .body(data)
                    .asJson();

            if(jsonResponse.getStatus() != 200)
                return null;

            return jsonResponse.getBody().getObject();

        } catch (UnirestException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JSONObject sendPutReq(String api) {

        try {

            HttpResponse<JsonNode> jsonResponse = Unirest.put(KOOCHITA_SERVER + api)
                    .header("content-type", "application/json")
                    .header("accept", "application/json")
                    .asJson();

            System.out.println(jsonResponse.getStatus());

            if(jsonResponse.getStatus() != 200)
                return null;

            return jsonResponse.getBody().getObject();

        } catch (UnirestException e) {
            e.printStackTrace();
        }

        return null;
    }

}

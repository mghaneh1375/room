package bogen.studio.Room.Routes.API;

import bogen.studio.Room.Utility.Utility;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/user")
@Validated
public class UserAPIRoutes {

    private HashMap<String, String> tokens = new HashMap<>();

    @PostMapping(value = "setToken")
    @ResponseBody
    public String setToken(HttpServletRequest request,
                           @RequestBody String token) {
        JSONObject jsonObject = new JSONObject(token);
        UUID uuid = UUID.randomUUID();
        tokens.put(uuid.toString(), jsonObject.getString("token"));
        return Utility.generateSuccessMsg("data", uuid.toString());
    }

    @GetMapping(value = "getToken")
    @ResponseBody
    public String getToken(@RequestParam(value = "uuid") String uuid) {

        String token = tokens.getOrDefault(uuid, "");
        if(!token.isEmpty())
            tokens.remove(uuid);

        return token;
    }
}

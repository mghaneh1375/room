package bogen.studio.Room.Routes;

import org.bson.types.ObjectId;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;

public class Utility {

    public static ObjectId getUserId(Principal principal) {
        return ((bogen.studio.Room.Models.User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getId();
    }

}

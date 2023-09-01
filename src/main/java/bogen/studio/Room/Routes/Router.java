package bogen.studio.Room.Routes;


import bogen.studio.Room.Models.User;
import org.bson.types.ObjectId;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;

public class Router {

    public ObjectId getUserId(Principal principal) {
        return ((User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getId();
    }

}

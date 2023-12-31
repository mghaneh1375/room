package bogen.studio.Room.Utility;

import bogen.studio.Room.Models.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserUtility {

    public static List<String> getUserAuthorities(Principal principal) {
        /* Extract and return authorities of the user */

        // Create User instance
        User user = ((bogen.studio.Room.Models.User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal());

        // Get authorities
        Collection<?> authorities = user.getAuthorities();

        // Convert to list of string
        List<String> userAuthorities = new ArrayList<>();
        authorities.forEach(authority -> userAuthorities.add(authority.toString()));

        return userAuthorities;
    }

}

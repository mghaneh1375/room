package bogen.studio.Room.Service;

import bogen.studio.Room.Models.CommonUser;
import bogen.studio.Room.Models.User;
import bogen.studio.Room.Repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetails implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        CommonUser user1 = userRepository.findByUsername(username).orElse(null);
        if(user1 == null)
            return null;

        return new User(
                new ObjectId(user1.getId().toString()),
                user1.getUsername(), user1.getPassword(), user1.isEnabled(),
                user1.isAccountNonExpired(), user1.isCredentialsNonExpired(), user1.isAccountNonLocked(),
                user1.getRoles()
        );
    }
}

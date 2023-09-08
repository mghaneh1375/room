package bogen.studio.Room.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import my.common.commonkoochita.MongoDB.Field;
import my.common.commonkoochita.MongoDB.Id;
import my.common.commonkoochita.MongoDB.Indexed;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection="users")
public class User implements UserDetails {

    @Id
    @Field("_id")
    private ObjectId id;

    @NotNull
    @Size(min = 4, max = 24)
    @Indexed(unique = true)
    private String username;

    @Getter(onMethod = @__(@JsonIgnore))
    @NotNull
    private String password;

    private boolean enabled;

    @Field("account_non_expired")
    private boolean accountNonExpired;

    @Field("credentials_non_expired")
    private boolean credentialsNonExpired;

    @Field("account_non_locked")
    private boolean accountNonLocked;

    /**
     * Contain list of roles by CODE
     */
    private Set<String> roles;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !accountNonExpired;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !credentialsNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountNonLocked;
    }

    /*
     * Get roles and add them as a Set of GrantedAuthority
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }


}
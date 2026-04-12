package sound.pezao.backend.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sound.pezao.backend.entities.Usuario;

import java.util.Collection;

public class UserAuthenticated implements UserDetails {

    private final Usuario user;

    public UserAuthenticated(Usuario user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getCargo().getPermissoes().stream()
                .map(p -> new SimpleGrantedAuthority(p.getNome()))
                .toList();
    }

    public String getPassword(){
        return user.getSenhaHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

package cn.chunana.simblog17api.config;

import cn.chunana.simblog17api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 从数据库加载用户，用于 Spring Security 认证体系（方法安全等）。
 */
@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        cn.chunana.simblog17api.entities.User user =
                userRepository.findByUsername(username)
                              .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new User(
                user.getId().toString(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}


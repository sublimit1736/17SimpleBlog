package cn.chunana.simblog17api.config;

import cn.chunana.simblog17api.utils.JwtUtils;
import cn.chunana.simblog17api.utils.TokenSecurityService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final TokenSecurityService tokenSecurityService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (tokenSecurityService.isTokenRevoked(token)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                Claims claims = jwtUtils.parseAccessToken(token);

                String userId = claims.getSubject();
                String role = claims.get("role", String.class);

                Long.parseLong(userId);
                cn.chunana.simblog17api.entities.User.UserRole.valueOf(role);

                var authority = new SimpleGrantedAuthority("ROLE_" + role);
                var principal = new User(userId, "", List.of(authority));

                var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of(authority));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                log.debug("JWT validation failed: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            return token.isEmpty() ? null : token;
        }
        return null;
    }
}


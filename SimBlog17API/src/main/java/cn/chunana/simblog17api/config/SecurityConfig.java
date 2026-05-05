package cn.chunana.simblog17api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthFilter jwtAuthFilter,
                                                   Environment environment) throws Exception {
        boolean prod = isProd(environment);

        http.csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, exception) ->
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                    .accessDeniedHandler((request, response, exception) ->
                            response.sendError(HttpServletResponse.SC_FORBIDDEN)))
            .headers(headers -> headers
                    .contentTypeOptions(Customizer.withDefaults())
                    .frameOptions(frame -> frame.deny())
                    .contentSecurityPolicy(csp -> csp.policyDirectives(
                            "default-src 'self'; object-src 'none'; frame-ancestors 'none'; base-uri 'self'"))
                    .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        if (prod) {
            http.headers(headers -> headers.httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .preload(true)
                    .maxAgeInSeconds(31536000)));

            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                                     "/swagger-ui.html", "/api/v3/docs/**",
                                     "/api/swagger-ui.html").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/user/auth/login", "/api/user/auth/register", "/api/user/auth/refresh").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/user/auth/profile/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/media/files/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                    .anyRequest().authenticated());
        } else {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .anyRequest().permitAll());
        }

        return http.build();
    }

    private boolean isProd(Environment environment) {
        return Arrays.stream(environment.getActiveProfiles())
                     .anyMatch("prod"::equalsIgnoreCase);
    }
}

package com.example.food_app.security;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // api không cần đăng nhập
                        .requestMatchers(
                                "/auth/**",
                                "/categories/**",
                                "/menus/**",
                                "/wards/delivery",
                                "/payment/**",
                                "/voucher",
                                "/chat"
                        ).permitAll()

                        // api chỉ dành cho khách hàng
                        .requestMatchers("/cart/**",
                                "/orders/**",
                                "/user/**",
                                "/voucher/{voucherId}/save",
                                "/reviews/**"
                                )
                        .hasRole("CUSTOMER")

                        // api chỉ dành cho admin
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // còn lại là các api cần đăng nhập
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}

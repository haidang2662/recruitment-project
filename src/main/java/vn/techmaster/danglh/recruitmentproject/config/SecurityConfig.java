package vn.techmaster.danglh.recruitmentproject.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import vn.techmaster.danglh.recruitmentproject.constant.Role;
import vn.techmaster.danglh.recruitmentproject.security.AuthTokenFilter;
import vn.techmaster.danglh.recruitmentproject.security.AuthenticationEntryPointJwt;
import vn.techmaster.danglh.recruitmentproject.security.CustomUserDetailsService;

import java.util.List;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityConfig {

    CustomUserDetailsService userDetailsService;

    AuthenticationEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll() // Allow access to H2 Console
                        .requestMatchers("/swagger-ui/index.html").permitAll() // Allow access to Swagger UI
                        .requestMatchers("/api/v1/files/**").permitAll() // Allow access to Swagger UI
                        .requestMatchers("/api/v1/locations").permitAll() // Allow access to Swagger UI
                        .requestMatchers("/api/v1/job-categories").permitAll() // Allow access to Swagger UI

                        // authentication start
                        .requestMatchers(
                                "/api/v1/authentications/refresh_token",
                                "/api/v1/authentications/logout"
                        ).authenticated()
                        .requestMatchers(
                                "/api/v1/authentications/login",
                                "/api/v1/authentications/registration"
                        ).permitAll() // Allow access to log-in, register
                        // authentication end

                        // account start
                        .requestMatchers(HttpMethod.GET, "/api/v1/accounts/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/accounts").hasAnyAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.POST, "/api/v1/accounts").hasAnyAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/accounts/{id}/password").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/accounts/{id}").authenticated()
                        .requestMatchers(
                                "/api/v1/accounts/{id}/activations",
                                "/api/v1/accounts/{id}/activation_emails",
                                "/api/v1/accounts/password_forgotten_emails",
                                "/api/v1/accounts/{id}/password_forgotten"
                        ).permitAll()
                        // account end

                        // Job start
                        .requestMatchers(HttpMethod.GET, "/api/v1/jobs/application").hasAnyAuthority(Role.CANDIDATE.toString()) // search job application cho candidate
                        .requestMatchers(HttpMethod.PUT, "/api/v1/jobs/{id}").hasAnyAuthority(Role.COMPANY.toString())
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/jobs/{id}").hasAnyAuthority(Role.COMPANY.toString())
                        .requestMatchers(HttpMethod.POST, "/api/v1/jobs").hasAnyAuthority(Role.COMPANY.toString())
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/jobs/{jobId}/status").hasAnyAuthority(Role.COMPANY.toString())
                        .requestMatchers(HttpMethod.GET, "/api/v1/jobs/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/jobs").permitAll()
                        // Job end

                        // favorite job - START
                        .requestMatchers(HttpMethod.POST, "/api/v1/favourite-jobs").hasAnyAuthority(Role.CANDIDATE.toString())
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/favourite-jobs").hasAnyAuthority(Role.CANDIDATE.toString())
                        // favorite job - END

                        // cv - START
                        .requestMatchers(HttpMethod.POST, "/api/v1/cv").hasAnyAuthority(Role.CANDIDATE.toString())
                        .requestMatchers(HttpMethod.GET, "/api/v1/cv").hasAnyAuthority(Role.CANDIDATE.toString())
                        .requestMatchers(HttpMethod.GET, "/api/v1/cv/download/{cvId}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/cv/{cvId}").hasAnyAuthority(Role.CANDIDATE.toString())
                        .requestMatchers(HttpMethod.PUT, "/api/v1/cv/{cvId}/main").hasAnyAuthority(Role.CANDIDATE.toString())
                        // cv - END

                        // application - START
                        .requestMatchers(HttpMethod.POST, "/api/v1/applications").hasAnyAuthority(Role.CANDIDATE.toString())
                        .requestMatchers(HttpMethod.GET, "/api/v1/applications").hasAnyAuthority(Role.COMPANY.toString())
                        .requestMatchers(HttpMethod.GET, "/api/v1/applications/{applicationId}").hasAnyAuthority(Role.COMPANY.toString())
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/applications/{applicationId}/status").hasAnyAuthority(Role.COMPANY.toString(), Role.CANDIDATE.toString())
                        // application - END

                        // interview - START
                        .requestMatchers(HttpMethod.POST, "/api/v1/interviews").hasAnyAuthority(Role.COMPANY.toString())
                        .requestMatchers(HttpMethod.GET, "/api/v1/interviews").hasAnyAuthority(Role.COMPANY.toString())
                        .requestMatchers(HttpMethod.GET, "/api/v1/interviews/{interviewId}").hasAnyAuthority(Role.COMPANY.toString())
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/interviews/{interviewId}/**").hasAnyAuthority(Role.COMPANY.toString())
                        // interview - END

                        // candidate - START
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidates").hasAnyAuthority(Role.COMPANY.toString())
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidates/{id}").hasAnyAuthority(Role.COMPANY.toString())
                        // candidate - END

                        // company - START
                        .requestMatchers(HttpMethod.GET, "/api/v1/companies").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/companies/{id}").permitAll()
                        // company - END

                        .requestMatchers("/api/**").authenticated() // all other apis need authentication
                        .anyRequest().permitAll() // all thymeleaf, html page don't have to authenticate
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(unauthorizedHandler)
                )
                .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class) // truoc khi xac thuc tai khoan dung khong thi toi xac thuc jwt da dung khong da
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**") // Disable CSRF for H2 Console
                )
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable); // Disable CSRF globally
        return http.build();
    }

}

package accounts.bank.managing.thesis.bachelor.rastvdmy.config;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.User;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.UserVisibility;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

import java.util.Collections;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * This class is responsible for the security configuration of the application.
 * It sets up the security filter chain, the logout success handler, and the user details service.
 * It also configures the global authentication manager builder.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    @Autowired
    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * This method sets up the security filter chain.
     * It configures session management, headers, CSRF protection,
     * request authorization, basic HTTP authentication, form login, and logout.
     *
     * @param http The HttpSecurity instance.
     * @return The SecurityFilterChain instance.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                )
                .headers(headers ->
                        headers.xssProtection(
                                xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                        ).contentSecurityPolicy(
                                cps -> cps.policyDirectives("script-src 'self'")
                        ))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                                .requestMatchers(HttpMethod.POST, "/profile/register").permitAll()
                                .anyRequest().authenticated()
                )
                .httpBasic(withDefaults())
                .formLogin(withDefaults())
                .logout(logout ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessHandler(logoutSuccessHandler())
                                .invalidateHttpSession(true)
                );
        return http.build();
    }

    /**
     * This method sets up the logout success handler.
     * It updates the user's visibility status and redirects the user to the login page after logout.
     *
     * @return The LogoutSuccessHandler instance.
     */
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication != null) {
                User user = userRepository.findByEmail(authentication.getName());
                if (user != null) {
                    user.setVisibility(UserVisibility.STATUS_OFFLINE);
                    userRepository.save(user);
                }
            }
            response.setStatus(HttpStatus.OK.value());
            response.sendRedirect("/login");
        };
    }

    /**
     * This method configures the global authentication manager builder.
     * It sets up the user details service.
     *
     * @param auth The AuthenticationManagerBuilder instance.
     * @throws Exception If an error occurs during configuration.
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(email -> {
            User user = userRepository.findByEmail(email);
            if (user != null) {
                user.setVisibility(UserVisibility.STATUS_ONLINE);
                userRepository.save(user);
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getUserRole().toString());

                return new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        Collections.singleton(authority) // Set a single authority
                );
            } else {
                throw new UsernameNotFoundException("User with email " + email + " not found.");
            }
        });
    }

    /**
     * This method sets up the password encoder.
     * It uses BCryptPasswordEncoder.
     *
     * @return The PasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
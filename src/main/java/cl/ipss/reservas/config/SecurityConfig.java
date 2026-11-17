package cl.ipss.reservas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad: los clientes reservan sin cuenta; la zona /admin solo es para el personal, con usuario y clave.
 * La clave viene de la configuracion (variable ADMIN_PASSWORD en produccion) y se guarda cifrada con BCrypt.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll())
                .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/admin/reservas", true).permitAll())
                .logout(l -> l.logoutSuccessUrl("/"));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService usuarios(PasswordEncoder encoder,
                                       @Value("${app.admin.usuario}") String usuario,
                                       @Value("${app.admin.password}") String password) {
        return new InMemoryUserDetailsManager(
                User.withUsername(usuario).password(encoder.encode(password)).roles("ADMIN").build());
    }
}

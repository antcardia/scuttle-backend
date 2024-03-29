package tfg.backend.scuttle.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import tfg.backend.scuttle.service.UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
// (securedEnabled = true,
// jsr250Enabled = true,
// prePostEnabled = true) // by default
public class SecurityConfiguration {

	@Autowired
    private JwtAuthFilter authFilter;
  
    // User Creation
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserService();
    }

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/login")).permitAll()
				.requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/logout")).permitAll()
				.requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/register")).permitAll()
				.requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/home")).authenticated()
				.requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/select")).authenticated()
				.requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/join-game")).authenticated()
				.requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/new-game")).authenticated()
				.requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/game/**")).authenticated()
				.requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
				.anyRequest().authenticated());
		http.headers(headers -> headers.frameOptions(t -> t.disable()));
		http.addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
		http.authenticationProvider(authenticationProvider());
		http.logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
				.clearAuthentication(true).invalidateHttpSession(true).deleteCookies("JSESSIONID")
				.logoutSuccessHandler((req, res, auth) -> res.setStatus(200))
				.logoutSuccessUrl("/home"));
		return http.build();
	}
}
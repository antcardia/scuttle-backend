package tfg.backend.scuttle.controller;

import tfg.backend.scuttle.entity.Authorities;
import tfg.backend.scuttle.entity.Player;
import tfg.backend.scuttle.entity.User;
import tfg.backend.scuttle.service.AuthoritiesService;
import tfg.backend.scuttle.service.JwtService;
import tfg.backend.scuttle.service.PlayerService;
import tfg.backend.scuttle.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class UserController {

	@Autowired
	private UserService service;

	@Autowired
	private PlayerService playerService;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private AuthoritiesService authoritiesService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private PasswordEncoder encoder;

	@GetMapping("/user")
	@PreAuthorize("hasAuthority('ROLE_USER') || hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<UserDetails> user(HttpServletRequest request) {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = userDetails.getUsername();
		UserDetails user = service.loadUserByUsername(username);
		return ResponseEntity.ok(user);
	}

	@GetMapping("/home")
	public ResponseEntity<String> home(HttpServletRequest request) {
		try {
			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			String username = userDetails.getUsername();
			Authorities authorities = authoritiesService.loadAuthoritiesByUsername(username);
			String localToken = authorities.getToken();
			String token = request.getHeader("Authorization").substring(7);
			if(!localToken.equals(token)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
			}
			jwtService.validateToken(token, service.loadUserByUsername(username));
			return ResponseEntity.ok(token);
		}catch(ExpiredJwtException e) {
			authoritiesService.deleteAuthorities(authoritiesService.loadAuthoritiesByUsername(e.getClaims().getSubject()).getId().intValue());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}catch(UsernameNotFoundException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}
	}

	
	@GetMapping("/user/userProfile")
	@PreAuthorize("hasAuthority('ROLE_USER') || hasAuthority('ROLE_ADMIN')")
	public String userProfile() {
		return "Welcome to User Profile";
	}
	
	@GetMapping("/admin/adminProfile")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public String adminProfile() {
		return "Welcome to Admin Profile";
	}

	@PostMapping("/register")
	public ResponseEntity<String> addNewUser(@RequestBody User user) {
		if(service.existsByUsername(user.getName())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
		}else if(service.existsByEmail(user.getEmail())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
		}
		service.addUser(user);
		Player player = new Player();
		player.setUser(user);
		player.setPoints(0);
		player.setInGame(false);
		playerService.save(player);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody Authorities authorities) {
		try {
			Authorities authdb = authoritiesService.loadAuthoritiesByUsername(authorities.getUsername());
			if(authdb != null) {
				authoritiesService.deleteAuthorities(authdb.getId().intValue());
			}
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authorities.getUsername(), authorities.getPassword()));
			String token = jwtService.generateToken(authorities.getUsername());
			authorities.setToken(token);
			authorities.setPassword(encoder.encode(authorities.getPassword()));
			authoritiesService.addAuthorities(authorities);
			return ResponseEntity.ok(token);
		}catch(BadCredentialsException e){
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid credentials");
		}catch(DisabledException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User disabled");
		}
	}

	@PostMapping("/refresh-token")
	public ResponseEntity<String> refreshToken(){
		try {
			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			String username = userDetails.getUsername();
			Authorities authorities = authoritiesService.loadAuthoritiesByUsername(username);
			String token = jwtService.generateToken(username);
			authorities.setToken(token);
			authoritiesService.addAuthorities(authorities);
			return ResponseEntity.ok(token);
		}catch(Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}
	}
}


package tfg.backend.scuttle.service;

import tfg.backend.scuttle.entity.User;
import tfg.backend.scuttle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository repository;

	@Autowired
	private PasswordEncoder encoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Optional<User> userDetail = repository.findByName(username);

		return userDetail.map(UserInfoDetails::new)
		.orElseThrow(() -> new UsernameNotFoundException("User not found " + username));
}

	public void addUser(User user) {
		user.setPassword(encoder.encode(user.getPassword()));
		user.setRoles("ROLE_USER");
		repository.save(user);
	}

    public boolean existsByUsername(String name) {
        if(repository.existsByUsername(name))
			return true;
		else
			return false;
    }

    public boolean existsByEmail(String email) {
		if(repository.existsByEmail(email))
			return true;
		else
        	return false;
    }


}


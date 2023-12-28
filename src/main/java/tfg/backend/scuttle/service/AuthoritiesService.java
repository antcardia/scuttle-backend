package tfg.backend.scuttle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tfg.backend.scuttle.entity.Authorities;
import tfg.backend.scuttle.repository.AuthoritiesRepository;

@Service
public class AuthoritiesService {
    
    @Autowired
	private AuthoritiesRepository repository;

    public Authorities loadAuthoritiesByUsername(String username) {
        return repository.findByUsername(username).orElse(null);
    }

    public void addAuthorities(Authorities authorities) {
        repository.save(authorities);
    }

    public void deleteAuthorities(Integer id) {
        repository.deleteById(id);
    }
}


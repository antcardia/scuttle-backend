package tfg.backend.scuttle.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tfg.backend.scuttle.entity.Authorities;

@Repository
public interface AuthoritiesRepository extends JpaRepository<Authorities, Integer>{
    
    Optional<Authorities> findByUsername(String username);

}

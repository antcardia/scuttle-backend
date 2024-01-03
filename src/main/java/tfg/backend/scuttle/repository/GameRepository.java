package tfg.backend.scuttle.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tfg.backend.scuttle.entity.Game;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {
    Optional<Game> findById(Long id);
    
}

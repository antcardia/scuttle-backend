package tfg.backend.scuttle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import tfg.backend.scuttle.entity.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer>{

    @Query("SELECT p FROM Player p WHERE p.user.id = :id")
    Player findByUserId(Integer id);
    
}

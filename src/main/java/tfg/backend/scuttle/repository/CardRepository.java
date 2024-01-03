package tfg.backend.scuttle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tfg.backend.scuttle.entity.Card;

@Repository
public interface CardRepository extends JpaRepository<Card, Integer>{
    
}

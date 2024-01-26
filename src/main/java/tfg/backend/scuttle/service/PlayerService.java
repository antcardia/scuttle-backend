package tfg.backend.scuttle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tfg.backend.scuttle.entity.Player;
import tfg.backend.scuttle.repository.PlayerRepository;

@Service
public class PlayerService {
    
    @Autowired
    private PlayerRepository playerRepository;

    public void save(Player player) {
        playerRepository.save(player);
    }

    public Player findByUserId(Integer id) {
        return playerRepository.findByUserId(id);
    }

}

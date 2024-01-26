package tfg.backend.scuttle.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tfg.backend.scuttle.entity.Game;
import tfg.backend.scuttle.entity.GameMode;
import tfg.backend.scuttle.repository.GameRepository;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    public Game findById(Long id) {
        return gameRepository.findById(id).orElse(null);
    }

    public List<Game> findAll() {
        return gameRepository.findAll();
    }

    public List<GameMode> getGameModes() {
        return List.of(GameMode.values());
    }

    public void save(Game game) {
        gameRepository.save(game);
    }

    public void delete(Game game) {
        game.setDeck(new ArrayList<>());
        game.setDiscardDeck(new ArrayList<>());
        game.setHost(null);
        game.setActive(false);
        game.setTime("invalid");
        game.setPlayers(new ArrayList<>());
        save(game);
    }
    
}

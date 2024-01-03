package tfg.backend.scuttle.controller;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import tfg.backend.scuttle.entity.Card;
import tfg.backend.scuttle.entity.Game;
import tfg.backend.scuttle.entity.GameMode;
import tfg.backend.scuttle.entity.Player;
import tfg.backend.scuttle.service.CardService;
import tfg.backend.scuttle.service.GameService;
import tfg.backend.scuttle.service.PlayerService;

@RestController
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private CardService cardService;

    @Autowired
    private PlayerService playerService;

    @GetMapping("/select")
    public ResponseEntity<List<GameMode>> select() {
        List<GameMode> gamemodes = gameService.getGameModes();
        return ResponseEntity.ok(gamemodes);
    }

    @GetMapping("/join-game")
    public ResponseEntity<List<Game>> getJoinableGames() {
        return ResponseEntity.ok(gameService.findAll().stream().filter(game->!game.isActive() && game.getTime().equals(0)).toList());
    }

    @PostMapping("/new-game")
    public ResponseEntity<String> startGame(@RequestBody Game game) {
        gameService.save(game);
        return ResponseEntity.ok("Game created");
    }

    @PostMapping("/join-game/{id}")
    public ResponseEntity<String> joinGame(@PathVariable("id") Long id, @RequestBody Player player) {
        Game game = gameService.findById(id);
        if(game.getPlayers().size() < 5) {
            game.getPlayers().add(player);
            gameService.save(game);
            return ResponseEntity.ok("Game joined");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Game full");
    }

    @PostMapping("/game/{id}/initial")
    public ResponseEntity<String> initial(@PathVariable("id") Long id) {
        Random random = new Random();
        Game game = gameService.findById(id);
        if(game.getMode() != GameMode.SOLO) {
            List<Card> cards = cardService.getCards();
            Collections.shuffle(cards);
            List<Player> players = game.getPlayers();
            for (Player player : players) {
                player.setHand(cards.subList(0, 5));
                cards = cards.subList(5, cards.size());
                playerService.save(player);
            }
            game.setDeck(cards);
            game.setTurn(players.get(random.nextInt(players.size())).getUser().getName());
            gameService.save(game);
            return ResponseEntity.ok("Game created");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Solo mode not implemented");
        //TODO: Solo mode
    }

    @PostMapping("/game/next-turn")
    public ResponseEntity<String> nextTurn(@RequestBody Game game) {
        for(Player player : game.getPlayers()){
            if(player.getUser().getName().equals(game.getTurn())) {
                if(player.getId() == game.getPlayers().size()) {
                    game.setTurn(game.getPlayers().get(0).getUser().getName());
                    gameService.save(game);
                    return ResponseEntity.ok("Turn changed");
                }
                game.setTurn(game.getPlayers().get(player.getId()+1).getUser().getName());
                gameService.save(game);
                return ResponseEntity.ok("Turn changed");
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Player not found");
    }
    
    @PostMapping("/game/{id}/draw-card-turn")
    public ResponseEntity<Card> drawCardTurn(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        List<Card> deck = game.getDeck();
        Card card = deck.get(0);
        deck.remove(card);
        game.setDeck(deck);
        gameService.save(game);
        Player player = getPlayerTurn(game);
        List<Card> hand = player.getHand();
        hand.add(card);
        player.setHand(hand);
        playerService.save(player);
        return ResponseEntity.ok(card);
    }

    @PostMapping("/game/{id}/draw-card-rest")
    public ResponseEntity<String> drawCardRest(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        List<Card> deck = game.getDeck();
        Card card = deck.get(0);
        deck.remove(card);
        game.setDeck(deck);
        gameService.save(game);
        for(Player player : game.getPlayers()){
            if(!player.getUser().getName().equals(game.getTurn())) {
                List<Card> hand = player.getHand();
                hand.add(card);
                player.setHand(hand);
                playerService.save(player);
                return ResponseEntity.ok("Card drawn");
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Player not found");
    }

    @PostMapping("/game/play-treasure-card")
    public ResponseEntity<String> playTreasureCard(@PathVariable("id") Long id, @RequestBody Card card) {
        Game game = gameService.findById(id);
        Player player = getPlayerTurn(game);
        List<Card> hand = player.getHand();
        if(hand.contains(card)) {
            hand.remove(card);
            player.setHand(hand);
            List<Card> playedCards = player.getPlayedCards();
            playedCards.add(card);
            player.setPlayedCards(playedCards);
            player.setPoints(player.getPoints() + Integer.parseInt(card.getValue()));
            playerService.save(player);
            return ResponseEntity.ok("Card played");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Card not found");
    }

    @PostMapping("/game/{id}/play-ability-card")
    public ResponseEntity<String> playAbilityCard(@PathVariable("id") Long id, @RequestBody Card card) {
        Game game = gameService.findById(id);
        Player player = getPlayerTurn(game);
        List<Card> hand = player.getHand();
        if(hand.contains(card)) {
            hand.remove(card);
            player.setHand(hand);
            List<Card> playedCards = player.getPlayedCards();
            playedCards.add(card);
            player.setPlayedCards(playedCards);
            playerService.save(player);
            return ResponseEntity.ok("Card played");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Card not found");
    }

    public Player getPlayerTurn(Game game) {
        for(Player player : game.getPlayers()){
            if(player.getUser().getName().equals(game.getTurn())) {
                return player;
            }
        }
        return null;
    }

    @PostMapping("/game/{id}/end")
    public ResponseEntity<String> endGame(@PathVariable("id") Long id, @RequestBody Game game) {
        gameService.save(game);
        return ResponseEntity.ok("Game ended");
    }
}

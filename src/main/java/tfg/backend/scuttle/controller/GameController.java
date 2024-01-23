package tfg.backend.scuttle.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tfg.backend.scuttle.entity.Card;
import tfg.backend.scuttle.entity.CardType;
import tfg.backend.scuttle.entity.Game;
import tfg.backend.scuttle.entity.GameMode;
import tfg.backend.scuttle.entity.Player;
import tfg.backend.scuttle.service.CardService;
import tfg.backend.scuttle.service.GameService;
import tfg.backend.scuttle.service.PlayerService;
import tfg.backend.scuttle.service.UserService;

@RestController
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private CardService cardService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private UserService userService;

    private Integer numDraws;

    private boolean turnChanged;

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
    public ResponseEntity<Integer> startGame(@RequestBody Map<String, Game> body) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Player player = playerService.findByUserId(userService.findByName(userDetails.getUsername()).getId());
        Game game = body.get("game");
        if(game.getMode() == GameMode.SOLO) {
            game.setNumPlayers(1);
        }
        player.setPoints(0);
        player.setHost(true);
        playerService.save(player);
        game.setHost(userDetails.getUsername());
        game.setPlayers(List.of(player));
        game.setTime(0);
        gameService.save(game);
        player.setGame(game);
        playerService.save(player);
        return ResponseEntity.ok(game.getId());
    }

    @PostMapping("/join-game/{id}")
    public ResponseEntity<String> joinGame(@PathVariable("id") Long id) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Player player = playerService.findByUserId(userService.findByName(userDetails.getUsername()).getId());
        Game game = gameService.findById(id);
        if(game.getPlayers().size() < game.getNumPlayers()) {
            game.getPlayers().add(player);
            gameService.save(game);
            player.setInGame(true);
            player.setGame(game);
            playerService.save(player);
            return ResponseEntity.ok("Game joined");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Game full");
    }

    @GetMapping("/game/{id}/lobby")
    public ResponseEntity<Game> lobby(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        if(game.getPlayers().size() == game.getNumPlayers()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(game);
        }
        return ResponseEntity.ok(game);
    }

    @PostMapping("/game/{id}/initial")
    public ResponseEntity<Player> initial(@PathVariable("id") Long id) {
        Random random = new Random();
        Game game = gameService.findById(id);
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Player p = playerService.findByUserId(userService.findByName(user.getUsername()).getId());
        
        if (game.getMode() != GameMode.SOLO && p.isHost()) {
            List<Card> cards = new ArrayList<>(cardService.getCards());
            Collections.shuffle(cards);
            List<Player> players = game.getPlayers();

            for (Player player : players) {
                int numCardsToAssign = Math.min(5, cards.size());
                List<Card> cardsToAssign = new ArrayList<>(cards.subList(0, numCardsToAssign));
                player.setHand(cardsToAssign);
                cards.removeAll(cardsToAssign);
                playerService.save(player);
            }

            game.setDeck(cards);
            Player pTurn = players.get(random.nextInt(players.size()));
            game.setTurn(pTurn.getUser().getName());
            game.getPlayers().remove(random.nextInt(players.size()));
            game.getPlayers().add(0, pTurn);
            game.setRound(1);
            game.setActive(true);
            gameService.save(game);
            return ResponseEntity.ok(p);
        } else {
            return ResponseEntity.ok(p);
        }
        //TODO: Solo mode
    }

    @GetMapping("/game/hand")
    public ResponseEntity<Player> getHand() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Player player = playerService.findByUserId(userService.findByName(userDetails.getUsername()).getId());
        return ResponseEntity.ok(player);
    }

    @GetMapping("/game/{id}/players")
    public ResponseEntity<List<Player>> getPlayers(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        return ResponseEntity.ok(game.getPlayers());
    }

    @GetMapping("/game/{id}")
    public ResponseEntity<Game> getGame(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        return ResponseEntity.ok(game);
    }

    @PostMapping("/game/next-turn")
    public ResponseEntity<String> nextTurn(@RequestBody Game game) {
        Player player = getPlayerTurn(game);
        if(game.getPlayers().indexOf(player) == game.getPlayers().size()-1) {
            game.setTurn(game.getPlayers().get(0).getUser().getName());
            game.setRound(game.getRound()+1);
            gameService.save(game);
            return ResponseEntity.ok("Turn changed");
        }
        game.setTurn(game.getPlayers().get(game.getPlayers().indexOf(player)+1).getUser().getName());
        this.turnChanged = true;
        gameService.save(game);
        return ResponseEntity.ok("Turn changed");
    }

    public boolean isTurnChanged() {
        return turnChanged;
    }

    public void setTurnChanged(boolean turnChanged) {
        this.turnChanged = turnChanged;
    }

    public Integer getNumDraws() {
        return numDraws;
    }

    public void setNumDraws(Integer numDraws) {
        this.numDraws = numDraws;
    }
    
    @PostMapping("/game/{id}/draw-card-turn")
    public ResponseEntity<Player> drawCardTurn(@PathVariable("id") Long id, @RequestParam(defaultValue = "1") Integer numDraws) {
        Game game = gameService.findById(id);
        List<Card> deck = game.getDeck();
        Player player = getPlayerTurn(game);
        this.numDraws = numDraws;
        for(int i = 0; i < numDraws; i++) {
            Card card = deck.get(0);
            deck.remove(card);
            game.setDeck(deck);
            gameService.save(game);
            List<Card> hand = player.getHand();
            hand.add(card);
            player.setHand(hand);
            playerService.save(player);
        }
        nextTurn(game);
        return ResponseEntity.ok(player);
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

    @PostMapping("/game/{id}/play-treasure-card")
    public ResponseEntity<String> playTreasureCard(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        Player player = getPlayerTurn(game);
        List<Card> hand = new ArrayList<>(player.getHand());
        for(Card card : player.getHand()) {
            if(card.isChosen()) {
                card.setChosen(false);
                cardService.save(card);
                hand.remove(card);
                player.setHand(hand);
                List<Card> playedCards = new ArrayList<>(player.getPlayedCards());
                playedCards.add(card);
                player.setPlayedCards(playedCards);
                player.setPoints(player.getPoints() + Integer.parseInt(card.getValue()));
                playerService.save(player);
            }
        }
        nextTurn(game);
        return ResponseEntity.ok("Card played");
    }

    @PostMapping("/game/{id}/play-ability-card")
    public ResponseEntity<String> playAbilityCard(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        Player player = getPlayerTurn(game);
        List<Card> hand = new ArrayList<>(player.getHand());
        for(Card card : player.getHand()) {
            if(card.isChosen() && !card.getType().equals(CardType.PERMANENT)) {
                card.setChosen(false);
                cardService.save(card);
                hand.remove(card);
                player.setHand(hand);
                game.getDiscardDeck().add(card);
                gameService.save(game);
                playerService.save(player);
                return ResponseEntity.ok(card.getName());
            } else if(card.isChosen() && card.getType().equals(CardType.PERMANENT)) {
                card.setChosen(false);
                card.setPlayedAsPermanent(true);
                cardService.save(card);
                hand.remove(card);
                player.setHand(hand);
                player.getPlayedCards().add(card);
                cardService.save(card);
                playerService.save(player);
                if(card.getName().equals("Lifeboat") || card.getName().equals("ShipsWheel") || card.getName().equals("JollyRoger")) {
                    nextTurn(game);
                }
                return ResponseEntity.ok(card.getName());
            }
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

package tfg.backend.scuttle.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import tfg.backend.scuttle.entity.Card;
import tfg.backend.scuttle.entity.CardType;
import tfg.backend.scuttle.entity.Game;
import tfg.backend.scuttle.entity.Player;
import tfg.backend.scuttle.service.CardService;
import tfg.backend.scuttle.service.GameService;
import tfg.backend.scuttle.service.PlayerService;

@RestController
public class CardController {

    @Autowired
    private CardService cardService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private GameController gameController;

    @Autowired
    private GameService gameService;

    private boolean showPopupLifeboat = false;

    private boolean showPopupFirstCardCannon = false;

    private boolean showPopupSecondCardCannon = false;

    private boolean fromCannon = false;

    private String playerName = "";

    private List<Card> cards = new ArrayList<>();

    private List<Card> cardsCannon = new ArrayList<>();


    @PostMapping("/game/{id}/choose-card")
    public ResponseEntity<String> chooseCard(@PathVariable("id") Long id,
            @RequestBody Map<String, String> requestBody) {
        String cardName = requestBody.get("cardName");
        for (Card card : cardService.getCards()) {
            if (!card.getName().equals(cardName)) {
                card.setChosen(false);
                cardService.save(card);
            } else {
                card.setChosen(true);
                cardService.save(card);
            }
        }
        return ResponseEntity.ok("Card chosen");
    }

    @GetMapping("/game/{id}/anne-bonny")
    public void anneBonnyCard(@PathVariable("id") Long id) {
        gameController.drawCardRest(id);
        gameController.drawCardTurn(id, 3);
    }

    @GetMapping("/game/{id}/henry-morgan")
    public ResponseEntity<List<Card>> henryMorgan(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        List<Card> deck = game.getDeck();
        List<Card> cards = new ArrayList<>(deck.subList(0, 5));
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/game/{id}/chosen-card-henry-morgan")
    public ResponseEntity<String> chosenCard(@PathVariable("id") Long id,
            @RequestBody Map<String, String> requestBody) {
        Random random = new Random();
        Game game = gameService.findById(id);
        String cardName = requestBody.get("card");
        List<Card> deck = game.getDeck();
        List<Card> cards = new ArrayList<>(deck.subList(0, 5));
        game.setDeck(deck.subList(5, deck.size()));
        gameService.save(game);
        for (Card card : cards) {
            if (card.getName().equals(cardName)) {
                Player player = gameController.getPlayerTurn(game);
                player.getHand().add(card);
                playerService.save(player);
                game.getDeck().add(random.nextInt(5), card);
                gameService.save(game);
            }
        }
        gameController.setNumDraws(1);
        gameController.nextTurn(game);
        return ResponseEntity.ok("Cards added");
    }

    @GetMapping("/game/{id}/long-john-silver")
    public ResponseEntity<List<Player>> longJohnSilver(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        Player player = gameController.getPlayerTurn(game);
        List<Player> players = new ArrayList<>(game.getPlayers());
        for (Player p : game.getPlayers()) {
            if (p.getUser().getName().equals(player.getUser().getName())) {
                players.remove(p);
            }
        }
        return ResponseEntity.ok(players);
    }

    @PostMapping("/game/{id}/chosen-player-long-john-silver")
    public ResponseEntity<String> chosenPlayer(@PathVariable("id") Long id,
            @RequestBody Map<String, String> requestBody) {
        Game game = gameService.findById(id);
        String playerName = requestBody.get("player");
        Player p = gameController.getPlayerTurn(game);
        for (Player player : game.getPlayers()) {
            if (player.getUser().getName().equals(playerName)) {
                List<Card> newHand = player.getHand();
                List<Card> oldHand = p.getHand();
                p.setHand(newHand);
                player.setHand(oldHand);
                playerService.save(p);
                playerService.save(player);
            }
        }
        gameController.nextTurn(game);
        return ResponseEntity.ok("Cards swapped");
    }

    @GetMapping("/game/{id}/madame-ching")
    public ResponseEntity<List<Card>> madameChing(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        List<Card> hand = p.getHand();
        Set<Card> cards = new HashSet<>();
        Integer permanentCount = 0;
        Integer treasureCount = 0;
        for (Card card : p.getHand()) {
            if (card.getType().equals(CardType.PERMANENT)) {
                cards.add(card);
                permanentCount++;
            }
            if (card.isTreasure()) {
                cards.add(card);
                treasureCount++;
            }
        }
        if (cards.size() > 1 && permanentCount > 0 && treasureCount > 0) {
            return ResponseEntity.ok(hand);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

    }

    @PostMapping("/game/{id}/chosen-cards-madame-ching")
    public ResponseEntity<String> chosenCards(@PathVariable("id") Long id,
            @RequestBody Map<String, String> requestBody) {
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        String cardName = requestBody.get("card");
        String cardName2 = requestBody.get("card2");
        Card card = cardService.getCards().stream()
                .filter(c -> c.getName().equals(cardName))
                .findFirst()
                .orElse(null);
        Card card2 = cardService.getCards().stream()
                .filter(c -> c.getName().equals(cardName2))
                .findFirst()
                .orElse(null);
        List<Card> cardsToAdd = new ArrayList<>();
        for (Card c : p.getHand()) {
            if (c.getName().equals(cardName)) {
                cardsToAdd.add(card);
                card.setPlayedAsPermanent(true);
                cardService.save(card);
            } else if (c.getName().equals(cardName2)) {
                cardsToAdd.add(card2);
                p.setPoints(p.getPoints() + Integer.parseInt(card2.getValue()));
            }
        }
        p.getPlayedCards().addAll(cardsToAdd);
        p.getHand().removeAll(cardsToAdd);
        playerService.save(p);
        gameController.nextTurn(game);
        return ResponseEntity.ok("Cards played");
    }

    @GetMapping("/game/{id}/stowaway")
    public ResponseEntity<List<Player>> stowaway(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        List<Player> players = new ArrayList<>(game.getPlayers());
        for (Player player : game.getPlayers()) {
            if (player.getUser().getName().equals(p.getUser().getName())) {
                players.remove(player);
            }
        }
        return ResponseEntity.ok(players);
    }

    @PostMapping("/game/{id}/chosen-player-stowaway")
    public ResponseEntity<String> chosenPlayerStowaway(@PathVariable("id") Long id,
            @RequestBody Map<String, String> requestBody) {
        Game game = gameService.findById(id);
        Card card = game.getDiscardDeck().stream()
                .filter(c -> c.getName().equals("Stowaway"))
                .findFirst()
                .orElse(null);
        game.getDiscardDeck().remove(card);
        gameService.save(game);
        String playerName = requestBody.get("player");
        for (Player player : game.getPlayers()) {
            if (player.getUser().getName().equals(playerName)) {
                player.getPlayedCards().add(card);
                playerService.save(player);
                final int[] min = { 10 };

                player.getPlayedCards().stream()
                        .filter(c -> c.isTreasure() && Integer.parseInt(c.getValue()) < min[0])
                        .forEach(c -> min[0] = Integer.parseInt(c.getValue()));

                List<Card> lowerValue = player.getPlayedCards().stream()
                        .filter(c -> c.isTreasure() && Integer.parseInt(c.getValue()) == (min[0]))
                        .toList();

                for (Card c : lowerValue) {
                    if (!c.isProtected()) {
                        player.setPoints(player.getPoints() - Integer.parseInt(c.getValue()));
                        playerService.save(player);
                        c.setValue(c.getValue() + '-');
                        c.setDestroyed(true);
                        cardService.save(c);
                    }
                }

            }
        }
        gameController.nextTurn(game);
        return ResponseEntity.ok("Card added");
    }

    @PostMapping("/game/{id}/stowaway-check")
    public ResponseEntity<String> stowawayCheck(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        Player player = gameController.getPlayerTurn(game);
        Card noValueCard = player.getPlayedCards().stream()
                .filter(c -> c.getValue().endsWith("-"))
                .findFirst()
                .orElse(null);
        Card stowaway = player.getPlayedCards().stream()
                .filter(c -> c.getName().equals("Stowaway"))
                .findFirst()
                .orElse(null);
        if(noValueCard != null && stowaway != null){
            for (Card card : player.getPlayedCards()){
                if (card.isTreasure() && !card.getValue().endsWith("-") && Integer.parseInt(card.getValue()) < Integer.parseInt(noValueCard.getValue().substring(0, noValueCard.getValue().length() - 1))){
                    card.setValue(card.getValue() + '-');
                    cardService.save(card);
                    noValueCard.setValue(noValueCard.getValue().substring(0, noValueCard.getValue().length() - 1));
                    cardService.save(noValueCard);
                }
            }         
        }else if (noValueCard != null && stowaway == null){
            noValueCard.setValue(noValueCard.getValue().substring(0, noValueCard.getValue().length() - 1));
            cardService.save(noValueCard);
        }else if (noValueCard == null && stowaway != null){
            String playerName = game.getPlayers().stream()
                    .filter(p -> p.getPlayedCards().contains(stowaway))
                    .findFirst()
                    .orElse(null)
                    .getUser()
                    .getName();
            for (Player p : game.getPlayers()) {
                if (p.getUser().getName().equals(playerName)) {
                    final int[] min = { 10 };
    
                    p.getPlayedCards().stream()
                            .filter(c -> c.isTreasure() && Integer.parseInt(c.getValue()) < min[0])
                            .forEach(c -> min[0] = Integer.parseInt(c.getValue()));
    
                    List<Card> lowerValue = p.getPlayedCards().stream()
                            .filter(c -> c.isTreasure() && Integer.parseInt(c.getValue()) == (min[0]))
                            .toList();
    
                    for (Card c : lowerValue) {
                        if (!c.isProtected()) {
                            p.setPoints(p.getPoints() - Integer.parseInt(c.getValue()));
                            playerService.save(p);
                            c.setValue(c.getValue() + '-');
                            c.setDestroyed(true);
                            cardService.save(c);
                        }
                    }
    
                }
            }
        }
        return ResponseEntity.ok("Cards checked");
    }

    @PostMapping("/game/{id}/maelstrom")
    public ResponseEntity<List<Card>> maelstrom(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        List<Card> cardsToRemove = new ArrayList<>();
        for (Player player : new ArrayList<>(game.getPlayers())) {
            player.setPoints(0);
            playerService.save(player);
            for (Card card : new ArrayList<>(player.getPlayedCards())) {
                if (!card.isProtected() && card.isTreasure() && !card.isPlayedAsPermanent()) {
                    card.setDestroyed(true);
                    cardService.save(card);
                    cardsToRemove.add(card);
                    game.getDiscardDeck().add(card);
                    gameService.save(game);
                } else if (card.isProtected() && card.isTreasure() && !card.isPlayedAsPermanent()) {
                    player.setPoints(player.getPoints() + Integer.parseInt(card.getValue()));
                    playerService.save(player);
                }
            }
            player.getPlayedCards().removeAll(cardsToRemove);
            playerService.save(player);
        }
        gameController.nextTurn(game);
        return ResponseEntity.ok(cardsToRemove);
    }

    @PostMapping("/game/{id}/mutiny")
    public ResponseEntity<List<Card>> mutiny(@PathVariable("id") Long id, @RequestBody Map<String, Integer> requestBody) {
        Game game = gameService.findById(id);
        Integer number = requestBody.get("number");
        List<Card> cardsToRemove = new ArrayList<>();
        for (Player player : new ArrayList<>(game.getPlayers())) {
            player.setPoints(0);
            playerService.save(player);
            for (Card card : new ArrayList<>(player.getPlayedCards())) {
                if (!card.isProtected() && card.isTreasure() && !card.isPlayedAsPermanent() && Integer.parseInt(card.getValue()) <= number) {
                    card.setDestroyed(true);
                    cardService.save(card);
                    cardsToRemove.add(card);
                    game.getDiscardDeck().add(card);
                    gameService.save(game);
                } else if ((!card.isProtected() && card.isTreasure() && !card.isPlayedAsPermanent() && Integer.parseInt(card.getValue()) > number) || (card.isProtected() && card.isTreasure() && !card.isPlayedAsPermanent())) {
                    player.setPoints(player.getPoints() + Integer.parseInt(card.getValue()));
                    playerService.save(player);
                }
            }
            player.getPlayedCards().removeAll(cardsToRemove);
            playerService.save(player);
        }
        gameController.nextTurn(game);
        return ResponseEntity.ok(cardsToRemove);
    }

    @PostMapping("/game/{id}/shiver-me-timbers")
    public ResponseEntity<List<Card>> shiverMeTimbers(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        List<Card> cardsToRemove = new ArrayList<>();
        for (Player player : new ArrayList<>(game.getPlayers())) {
            for (Card card : new ArrayList<>(player.getPlayedCards())) {
                if (!card.isProtected() && card.isTreasure() && Integer.parseInt(card.getValue()) % 2 == 0) {
                    if(player.getPoints()<=0){
                        player.setPoints(0);
                    }else{
                        player.setPoints(player.getPoints() - Integer.parseInt(card.getValue()));
                    }
                    playerService.save(player);
                    card.setDestroyed(true);
                    cardService.save(card);
                    cardsToRemove.add(card);
                    game.getDiscardDeck().add(card);
                    gameService.save(game);
                }
            }
            player.getPlayedCards().removeAll(cardsToRemove);
            playerService.save(player);
            cardsToRemove.clear();
            for (Card card: new ArrayList<>(player.getHand())) {
                if (!card.isProtected() && card.isTreasure() && Integer.parseInt(card.getValue()) % 2 != 0) {
                    cardsToRemove.add(card);
                    game.getDiscardDeck().add(card);
                    gameService.save(game);
                }
            }
            player.getHand().removeAll(cardsToRemove);
            playerService.save(player);
        }
        gameController.nextTurn(game);
        return ResponseEntity.ok(cardsToRemove);
    }

    @GetMapping("/game/{id}/show-popup-lifeboat")
    public ResponseEntity<Map<String, Object>> getShowPopupLifeboat() {
        if (cards != null){
            Map<String, Object> res = Map.of("showPopupLifeboat", showPopupLifeboat, "playerName", playerName, "cardsLifeboat", cards);
            return ResponseEntity.ok(res);
        }else if (cards == null){
            Map<String, Object> res = Map.of("showPopupLifeboat", false, "playerName", "", "cardsLifeboat", new ArrayList<>());
            return ResponseEntity.ok(res);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PostMapping("/game/{id}/trigger-popup-lifeboat")
    public ResponseEntity<String> triggerPopupLifeboat(@PathVariable("id") Long id, @RequestBody Map<String, List<Card>> requestBody) {
        Game game = gameService.findById(id);
        cards = requestBody.get("cardsLifeboat");
        if(cards == null){
            showPopupLifeboat = false;
            showPopupFirstCardCannon = false;
            showPopupSecondCardCannon = false;
        }else{
            showPopupLifeboat = !showPopupLifeboat;
            showPopupFirstCardCannon = false;
            showPopupSecondCardCannon = false;
        }
        playerName = game.getPlayers().stream()
                .filter(p -> p.getPlayedCards().stream()
                        .anyMatch(c -> c.getName().equals("Lifeboat")))
                .findFirst()
                .orElse(null)
                .getUser()
                .getName();
        return ResponseEntity.ok(playerName);
    }

    @PostMapping("/game/{id}/saved-card-lifeboat")
    public ResponseEntity<String> savedCardLifeboat(@PathVariable("id") Long id,
            @RequestBody Map<String, String> requestBody) {
        Game game = gameService.findById(id);
        String cardName = requestBody.get("card");
        String playerName = requestBody.get("player");
        Card card = cardService.getCards().stream()
                .filter(c -> c.getName().equals(cardName))
                .findFirst()
                .orElse(null);
        Player player = game.getPlayers().stream()
                .filter(p -> p.getUser().getName().equals(playerName))
                .findFirst()
                .orElse(null);
        Card lifeboat = cardService.getCards().stream()
                .filter(c -> c.getName().equals("Lifeboat"))
                .findFirst()
                .orElse(null);
        card.setDestroyed(false);
        lifeboat.setDestroyed(true);
        cardService.save(card);
        cardService.save(lifeboat);
        game.getDiscardDeck().add(lifeboat);
        game.getDiscardDeck().remove(card);
        gameService.save(game);
        if(fromCannon){
            player.getHand().add(card);
            player.getPlayedCards().remove(lifeboat);
            playerService.save(player);
        } else{
            player.getPlayedCards().add(card);
            player.getPlayedCards().remove(lifeboat);
            player.setPoints(player.getPoints() + Integer.parseInt(card.getValue()));
            playerService.save(player);
        }
        fromCannon = false;
        return ResponseEntity.ok("Card recovered");
    }

    @GetMapping("/game/{id}/jolly-roger")
    public ResponseEntity<Boolean> jollyRoger(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        Player player = gameController.getPlayerTurn(game);
        if(player.getPlayedCards().stream().anyMatch(c -> c.getName().equals("JollyRoger") && c.isPlayedAsPermanent()) && gameController.isTurnChanged()){
            gameController.setTurnChanged(false);
            return ResponseEntity.ok(true);
        }else{
            return ResponseEntity.ok(false);
        }
    }

    @PostMapping("/game/{id}/jolly-roger-action")
    public ResponseEntity<String> jollyRogerAction(@PathVariable("id") Long id, @RequestBody Map<String, String> requestBody) {
        Game game = gameService.findById(id);
        String playerName = requestBody.get("player");
        Player player = gameController.getPlayerTurn(game);
        if(player.getUser().getName().equals(playerName)){
            List<Card> deck = game.getDeck();
            Card card = deck.get(0);
            if (!card.isTreasure()) {
                deck.remove(card);
                game.setDeck(deck);
                gameService.save(game);
                List<Card> hand = player.getHand();
                hand.add(card);
                player.setHand(hand);
                playerService.save(player);
                gameController.setNumDraws(1);
            }
        }
        return ResponseEntity.ok("Card removed");
    }

    @GetMapping("/game/{id}/ships-wheel")
    public ResponseEntity<Integer> shipsWheel(@PathVariable("id") Long id) {
        return ResponseEntity.ok(gameController.getNumDraws());
    }

    @PostMapping("/game/{id}/ships-wheel-action")
    public ResponseEntity<String> shipsWheelAction(@PathVariable("id") Long id, @RequestBody Map<String, String> requestBody) {
        Game game = gameService.findById(id);
        Integer numDraws = gameController.getNumDraws();
        List<Card> deck = game.getDeck();
        String playerName = requestBody.get("player");
        Player player = game.getPlayers().stream()
                .filter(p -> p.getUser().getName().equals(playerName))
                .findFirst()
                .orElse(null);
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
        gameController.setNumDraws(0);
        return ResponseEntity.ok("Card drawn");
    }

    @GetMapping("/game/{id}/cutlass")
    public ResponseEntity<List<Card>> cutlass(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        List<Card> cards = new ArrayList<>();
        for (Player player : game.getPlayers()) {
            for (Card card : player.getPlayedCards()) {
                if (card.isPlayedAsPermanent() && !card.isProtected()) {
                    cards.add(card);
                }
            }
        }
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/game/{id}/chosen-card-cutlass")
    public ResponseEntity<String> chosenCardCutlass(@PathVariable("id") Long id,
            @RequestBody Map<String, String> requestBody) {
        Game game = gameService.findById(id);
        String cardName = requestBody.get("card");
        Card card = cardService.getCards().stream()
                .filter(c -> c.getName().equals(cardName))
                .findFirst()
                .orElse(null);
        Player player = game.getPlayers().stream()
                .filter(p -> p.getPlayedCards().contains(card))
                .findFirst()
                .orElse(null);
        card.setDestroyed(true);
        player.getPlayedCards().remove(card);
        game.getDiscardDeck().add(card);
        cardService.save(card);
        playerService.save(player);
        gameService.save(game);
        gameController.drawCardTurn(id, 1);
        return ResponseEntity.ok("Card removed");
    }

    @GetMapping("/game/{id}/monkey")
    public ResponseEntity<List<Card>> monkey(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        List<Card> cards = game.getDiscardDeck();
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/game/{id}/chosen-card-monkey")
    public ResponseEntity<String> chosenCardMonkey(@PathVariable("id") Long id,
            @RequestBody Map<String, String> requestBody) {
        Game game = gameService.findById(id);
        Player player = gameController.getPlayerTurn(game);
        String cardName = requestBody.get("card");
        Card card = cardService.getCards().stream()
                .filter(c -> c.getName().equals(cardName))
                .findFirst()
                .orElse(null);
        card.setDestroyed(false);
        game.getDiscardDeck().remove(card);
        gameService.save(game);
        player.getHand().add(card);
        playerService.save(player);
        gameController.nextTurn(game);
        return ResponseEntity.ok("Card recovered");
    }

    @GetMapping("/game/{id}/cannon")
    public ResponseEntity<List<Player>> Cannon(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        Player player = gameController.getPlayerTurn(game);
        List<Player> players = new ArrayList<>(game.getPlayers());
        players.remove(player);
        return ResponseEntity.ok(players);
    }

    @PostMapping("/game/{id}/chosen-player-cannon")
    public ResponseEntity<List<Card>> chosenPlayerCannon(@PathVariable("id") Long id,
            @RequestBody Map<String, String> requestBody) {
        Game game = gameService.findById(id);
        String playerName = requestBody.get("player");
        Player player = game.getPlayers().stream()
                .filter(p -> p.getUser().getName().equals(playerName))
                .findFirst()
                .orElse(null);
        return ResponseEntity.ok(player.getHand());
    }

    @GetMapping("/game/{id}/show-popup-cannon")
    public ResponseEntity<Map<String, Object>> getShowPopupCannon() {
        if (cardsCannon != null){
            Map<String, Object> res = Map.of("showPopupFirstCardCannon", showPopupFirstCardCannon, "showPopupSecondCardCannon", showPopupSecondCardCannon, "playerName", playerName, "cardsCannon", cardsCannon);
            return ResponseEntity.ok(res);
        }else if (cardsCannon == null){
            Map<String, Object> res = Map.of("showPopupFirstCardCannon", false, "showPopupSecondCardCannon", false, "playerName", "", "cardsCannon", new ArrayList<>());
            return ResponseEntity.ok(res);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PostMapping("/game/{id}/trigger-popup-cannon")
    public ResponseEntity<String> triggerPopupCannon(@PathVariable("id") Long id, @RequestBody Map<String, List<Card>> requestBody) {
        Game game = gameService.findById(id);
        cardsCannon = requestBody.get("cardsCannon");
        if (cardsCannon != null){
            if(showPopupSecondCardCannon == true){
                showPopupSecondCardCannon = false;
                showPopupFirstCardCannon = false;
            }else if(showPopupFirstCardCannon == true){
                showPopupSecondCardCannon = true;
                showPopupFirstCardCannon = false;
            }else{
                showPopupFirstCardCannon = true;
            }
        }else if(cardsCannon == null){
            showPopupFirstCardCannon = false;
            showPopupSecondCardCannon = false;
        }
        playerName = game.getPlayers().stream()
                .filter(p -> p.getHand().get(0).getName().equals(cardsCannon.get(0).getName()))
                .map(p -> p.getUser().getName())
                .findFirst()
                .orElse("");

        return ResponseEntity.ok(playerName);
    }

    @PostMapping("/game/{id}/chosen-cards-cannon")
    public ResponseEntity<List<Card>> chosenCardsCannon(@PathVariable("id") Long id,
            @RequestBody Map<String, String> requestBody) {
        Game game = gameService.findById(id);
        List<Card> cards = new ArrayList<>();
        String cardName = requestBody.get("card");
        String cardName2 = requestBody.get("card2");
        for (Player player : game.getPlayers()) {
            for (Card card : new ArrayList<>(player.getHand())) {
                if (card.getName().equals(cardName) || card.getName().equals(cardName2)) {
                    player.getHand().remove(card);
                    game.getDiscardDeck().add(card);
                    playerService.save(player);
                    gameService.save(game);
                    cards.add(card);
                }
            }
        }
        this.cards = cards;
        showPopupFirstCardCannon = false;
        showPopupSecondCardCannon = false;
        fromCannon = true;
        gameController.nextTurn(game);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/game/{id}/treasure-map")
    public ResponseEntity<String> TreasureMap(@PathVariable("id") Long id) {
        gameController.drawCardTurn(id, 2);
        return ResponseEntity.ok("Cards added");
    }

    @PostMapping("/game/{id}/kraken")
    public ResponseEntity<String> kraken(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        for (Player player : new ArrayList<>(game.getPlayers())) {
            for (Card card : new ArrayList<>(player.getPlayedCards())) {
                if (card.isPlayedAsPermanent() && !card.isProtected()) {
                    card.setDestroyed(true);
                    cardService.save(card);
                    player.getPlayedCards().remove(card);
                    playerService.save(player);
                    game.getDiscardDeck().add(card);
                    gameService.save(game);
                }
            };
        }
        gameController.nextTurn(game);
        return ResponseEntity.ok("Cards removed");
    }

    @PostMapping("/game/{id}/skeleton-key")
    public ResponseEntity<List<Card>> skeletonKey(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        Player player = gameController.getPlayerTurn(game);
        List<Card> deck = game.getDeck();
        Card card = deck.get(0);
        deck.remove(card);
        game.setDeck(deck);
        gameService.save(game);
        List<Card> hand = player.getHand();
        hand.add(card);
        player.setHand(hand);
        playerService.save(player);
        gameController.setNumDraws(1);
        return ResponseEntity.ok(hand);
    }

    @PostMapping("/game/{id}/spyglass")
    public ResponseEntity<String> spyglass(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        Player player = gameController.getPlayerTurn(game);
        for (Player p : game.getPlayers()) {
            if (!p.getUser().getName().equals(player.getUser().getName())) {
                for (Card card : p.getHand()) {
                    card.setRevealed(true);
                    cardService.save(card);
                }
            }
        }
        gameController.nextTurn(game);
        return ResponseEntity.ok("Cards revealed");
    }

    @PostMapping("/game/{id}/spyglass-check")
    public ResponseEntity<String> spyglassCheck(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        Player player = gameController.getPlayerTurn(game);
        List<Card> spyglass = player.getPlayedCards().stream()
                .filter(c -> c.getName().startsWith("Spyglass") && c.isPlayedAsPermanent())
                .toList();
        if(spyglass.isEmpty()){
            for (Player p : game.getPlayers()) {
                if (!p.getUser().getName().equals(player.getUser().getName())) {
                    for (Card card : p.getHand()) {
                        card.setRevealed(false);
                        cardService.save(card);
                    }
                }
            }
        }else if(!spyglass.isEmpty()){
            for (Player p : game.getPlayers()) {
                if (!p.getUser().getName().equals(player.getUser().getName())) {
                    for (Card card : p.getHand()) {
                        card.setRevealed(true);
                        cardService.save(card);
                    }
                    for (Card card : p.getPlayedCards()) {
                        card.setRevealed(false);
                        cardService.save(card);
                    }
                }
            }
        }
        return ResponseEntity.ok("Cards hidden");
    }

    @GetMapping("/game/{id}/pirate-code")
    public ResponseEntity<List<Card>> pirateCode(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        Player player = gameController.getPlayerTurn(game);
        return ResponseEntity.ok(player.getPlayedCards());
    }

    @PostMapping("/game/{id}/chosen-card-pirate-code")
    public ResponseEntity<String> chosenCardPirateCode(@PathVariable("id") Long id,
            @RequestBody Map<String, Card> requestBody) {
        Game game = gameService.findById(id);
        Card card = requestBody.get("card");
        if (card.isTreasure() && !card.isPlayedAsPermanent()) {
            card.setProtected(true);
            card.setUpdatedPirateCode(true);
            cardService.save(card);
        }
        gameController.nextTurn(game);
        return ResponseEntity.ok("Card protected");
    }

    @PostMapping("/game/{id}/pirate-code-check")
    public ResponseEntity<String> pirateCodeCheck(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        for(Player player : new ArrayList<>(game.getPlayers())){
            Card pirateCodeEyepatch = player.getPlayedCards().stream()
                    .filter(c -> c.getName().equals("PirateCodeEyepatch"))
                    .findFirst()
                    .orElse(null);
            Card pirateCodeSkull = player.getPlayedCards().stream()
                    .filter(c -> c.getName().equals("PirateCodeSkull"))
                    .findFirst()
                    .orElse(null);
            Card pirateCodeHook = player.getPlayedCards().stream()
                    .filter(c -> c.getName().equals("PirateCodeHook"))
                    .findFirst()
                    .orElse(null);
            if(pirateCodeEyepatch == null && pirateCodeSkull == null && pirateCodeHook == null){
                for (Card card : new ArrayList<>(player.getPlayedCards())) {
                    if (card.isTreasure() && !card.isPlayedAsPermanent() && card.isUpdatedPirateCode() && !card.isUpdatedFirstMate()) {
                        card.setProtected(false);
                        card.setUpdatedPirateCode(false);
                        cardService.save(card);
                        return ResponseEntity.ok("Card unprotected " + card.getName());
                    }else if(card.isTreasure() && !card.isPlayedAsPermanent() && card.isUpdatedPirateCode() && card.isUpdatedFirstMate()){
                        card.setUpdatedPirateCode(false);
                        cardService.save(card);
                        return ResponseEntity.ok("Card unprotected " + card.getName());
                    }
                }
            }else if((pirateCodeEyepatch != null && pirateCodeEyepatch.isPlayedAsPermanent()) || (pirateCodeSkull != null && pirateCodeSkull.isPlayedAsPermanent()) || (pirateCodeHook != null && pirateCodeHook.isPlayedAsPermanent())){
                for (Card card : new ArrayList<>(player.getPlayedCards())) {
                    if (card.isTreasure() && !card.isPlayedAsPermanent() && !card.isUpdatedPirateCode()) {
                        card.setProtected(true);
                        card.setUpdatedPirateCode(true);
                        cardService.save(card);
                        return ResponseEntity.ok("Card protected " + card.getName());
                    }
                }
            }
        }
        return ResponseEntity.ok("Card unchanged");
    }

    @GetMapping("/game/{id}/lookout")
    public ResponseEntity<List<Card>> lookout(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        List<Card> cards = new ArrayList<>();
        Player p = gameController.getPlayerTurn(game);
        for (Player player : game.getPlayers()) {
            if (!player.equals(p)) {
                for (Card card : player.getPlayedCards()) {
                    if (card.isTreasure() && !card.isPlayedAsPermanent() && !card.isProtected()) {
                        cards.add(card);
                    }
                }
            }
        }
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/game/{id}/chosen-card-lookout")
    public ResponseEntity<String> chosenCardLookout(@PathVariable("id") Long id,
            @RequestBody Map<String, String> requestBody) {
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        String cardName = requestBody.get("card");
        Card card = cardService.getCards().stream()
                .filter(c -> c.getName().equals(cardName))
                .findFirst()
                .orElse(null);
        Player player = game.getPlayers().stream()
                .filter(pl -> pl.getPlayedCards().contains(card))
                .findFirst()
                .orElse(null);
        player.getPlayedCards().remove(card);
        player.setPoints(player.getPoints() - Integer.parseInt(card.getValue()));
        playerService.save(player);
        p.getPlayedCards().add(card);
        p.setPoints(p.getPoints() + Integer.parseInt(card.getValue()));
        playerService.save(p);
        gameController.nextTurn(game);
        return ResponseEntity.ok("Card stolen");
    }

    @PostMapping("/game/{id}/first-mate")
    public ResponseEntity<String> firstMate(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        for (Card card : new ArrayList<>(p.getPlayedCards())) {
            if (card.isTreasure() && !card.isPlayedAsPermanent()) {
                card.setProtected(true);
                card.setUpdatedFirstMate(true);
                cardService.save(card);
            }
        }
        gameController.nextTurn(game);
        return ResponseEntity.ok("Card protected");
    }

    @PostMapping("/game/{id}/first-mate-check")
    public ResponseEntity<String> firstMateCheck(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        for(Player player : new ArrayList<>(game.getPlayers())){
            Card firstMateEyepatch = player.getPlayedCards().stream()
                    .filter(c -> c.getName().equals("FirstMateEyepatch"))
                    .findFirst()
                    .orElse(null);
            Card firstMateSkull = player.getPlayedCards().stream()
                    .filter(c -> c.getName().equals("FirstMateSkull"))
                    .findFirst()
                    .orElse(null);
            Card firstMateHook = player.getPlayedCards().stream()
                    .filter(c -> c.getName().equals("FirstMateHook"))
                    .findFirst()
                    .orElse(null);
            if(firstMateEyepatch == null && firstMateSkull == null && firstMateHook == null){
                for (Card card : new ArrayList<>(player.getPlayedCards())) {
                    if (card.isTreasure() && !card.isPlayedAsPermanent() && card.isUpdatedFirstMate() && !card.isUpdatedPirateCode()) {
                        card.setProtected(false);
                        card.setUpdatedFirstMate(false);
                        cardService.save(card);
                        return ResponseEntity.ok("Card unprotected " + card.getName());
                    }else if(card.isTreasure() && !card.isPlayedAsPermanent() && card.isUpdatedFirstMate() && card.isUpdatedPirateCode()){
                        card.setUpdatedFirstMate(false);
                        cardService.save(card);
                        return ResponseEntity.ok("Card unprotected " + card.getName());
                    }
                }
            }else if(firstMateEyepatch != null || firstMateSkull != null || firstMateHook != null){
                for (Card card : new ArrayList<>(player.getPlayedCards())) {
                    if (card.isTreasure() && !card.isPlayedAsPermanent() && !card.isUpdatedFirstMate()) {
                        card.setProtected(true);
                        card.setUpdatedFirstMate(true);
                        cardService.save(card);
                        return ResponseEntity.ok("Card protected " + card.getName());
                    }
                }
            }
        }
        return ResponseEntity.ok("Card unchanged");
    }

    @PostMapping("/game/{id}/pirate-king")
    public ResponseEntity<String> pirateKing(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        for (Card card : new ArrayList<>(p.getPlayedCards())) {
            if (card.isTreasure() && !card.isPlayedAsPermanent()) {
                card.setUpdatedPirateKing(true);
                cardService.save(card);
                p.setPoints(p.getPoints() + 3);
                playerService.save(p);
            }
        }
        gameController.nextTurn(game);
        return ResponseEntity.ok("Increased card value");
    }

    @PostMapping("/game/{id}/pirate-king-check")
    public ResponseEntity<String> pirateKingCheck(@PathVariable("id") Long id) {
        Game game = gameService.findById(id);
        for(Player player: new ArrayList<>(game.getPlayers())){
            Card pirateKingEyepatch = player.getPlayedCards().stream()
                    .filter(c -> c.getName().equals("PirateKingEyepatch"))
                    .findFirst()
                    .orElse(null);
            Card pirateKingSkull = player.getPlayedCards().stream()
                    .filter(c -> c.getName().equals("PirateKingSkull"))
                    .findFirst()
                    .orElse(null);
            Card pirateKingHook = player.getPlayedCards().stream()
                    .filter(c -> c.getName().equals("PirateKingHook"))
                    .findFirst()
                    .orElse(null);
            if(pirateKingEyepatch == null && pirateKingSkull == null && pirateKingHook == null){
                for (Card card : new ArrayList<>(player.getPlayedCards())) {
                    if (card.isTreasure() && !card.isPlayedAsPermanent() && card.isUpdatedPirateKing()) {
                        card.setUpdatedPirateKing(false);
                        cardService.save(card);
                        player.setPoints(player.getPoints() - 3);
                        playerService.save(player);
                        return ResponseEntity.ok("Decreased card value for card " + card.getName());
                    }
                }
            }else if(pirateKingEyepatch != null || pirateKingSkull != null || pirateKingHook != null){
                for (Card card : new ArrayList<>(player.getPlayedCards())) {
                    if (card.isTreasure() && !card.isPlayedAsPermanent() && !card.isUpdatedPirateKing()) {
                        card.setUpdatedPirateKing(true);
                        cardService.save(card);
                        player.setPoints(player.getPoints() + 3);
                        playerService.save(player);
                        return ResponseEntity.ok("Increased value for card " + card.getName());
                    }
                }
            }
        }
        return ResponseEntity.ok("Card value unchanged");
    }
}

package tfg.backend.scuttle.controller;

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
    
    @GetMapping("/game/{id}/anne-bonny")
    public void anneBonnyCard(@PathVariable("id") Long id){
        Game game = gameService.findById(id);
        Player player = gameController.getPlayerTurn(game);
        Card card = player.getHand().stream()
                .filter(c -> c.getName().equals("Anne Bonny"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, card);
        gameController.drawCardTurn(id);
        gameController.drawCardTurn(id);
        gameController.drawCardTurn(id);
        gameController.drawCardRest(id);

    }

    @GetMapping("/game/{id}/henry-morgan")
    public ResponseEntity<List<Card>> henryMorgan(@PathVariable("id") Long id){
        Game game = gameService.findById(id);
        Player player = gameController.getPlayerTurn(game);
        Card card = player.getHand().stream()
                .filter(c -> c.getName().equals("Henry Morgan"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, card);
        List<Card> deck = game.getDeck();
        List<Card> cards = deck.subList(0, 5);
        deck = deck.subList(5, deck.size());
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/game/{id}/chosen-card")
    public ResponseEntity<String> chosenCard(@PathVariable("id") Long id, @RequestBody List<Card> cards){
        Random random = new Random();
        Game game = gameService.findById(id);
        for(Card card:cards){
            if(card.isChosen()){
                Player player = gameController.getPlayerTurn(game);
                player.getHand().add(card);
                playerService.save(player);
            }
            game.getDeck().add(random.nextInt(5), card);
            gameService.save(game);
        }
        return ResponseEntity.ok("Cards added");
    }

    @PostMapping("/game/{id}/long-john-silver")
    public ResponseEntity<String> longJohnSilver(@PathVariable("id") Long id, @RequestBody Player player){
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        Card card = p.getHand().stream()
                .filter(c -> c.getName().equals("Long John Silver"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, card);
        List<Card> newHand = player.getHand();
        List<Card> oldHand = p.getHand();
        p.setHand(newHand);
        player.setHand(oldHand);
        playerService.save(p);
        playerService.save(player);
        return ResponseEntity.ok("Cards swapped");
    }

    @PostMapping("/game/{id}/madame-ching")
    public ResponseEntity<String> madameChing(@PathVariable("id") Long id, @RequestBody List<Card> cards){
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        Card c = p.getHand().stream()
        .filter(ca -> ca.getName().equals("Madame Ching"))
        .findFirst()
        .orElse(null);
        gameController.playAbilityCard(id, c);
        for(Card card:cards){
            if(card.getType().equals(CardType.PERMANENT)){
                gameController.playAbilityCard(id, card);
                cardService.save(card);
            }
            gameController.playTreasureCard(id, card);
            cardService.save(card);
        }
        return ResponseEntity.ok("Cards played");
    }

    @PostMapping("/game/{id}/stowaway")
    public ResponseEntity<String> stowaway(@PathVariable("id") Long id, @RequestBody Player player){
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        Card card = p.getHand().stream()
        .filter(c -> c.getName().equals("Stowaway"))
        .findFirst()
        .orElse(null);
        gameController.playAbilityCard(id, card);
        player.getPlayedCards().add(card);
        playerService.save(player);

        final int[] min = {10};

        player.getPlayedCards().stream()
                .filter(c -> !"Joker".equals(c.getValue()) && Integer.parseInt(c.getValue()) < min[0])
                .forEach(c -> min[0] = Integer.parseInt(c.getValue()));

        List<Card> lowerValue = player.getPlayedCards().stream()
                .filter(c -> c.getValue().equals(String.valueOf(min[0])))
                .toList();

        for(Card c : lowerValue){
            if(!c.isProtected()) {
                c.setValue(c.getValue() + '-');
                cardService.save(c);
            }
        }

        return ResponseEntity.ok("Card added");
    }

    @PostMapping("/game/{id}/recover-value")
    public ResponseEntity<String> recoverValue(@PathVariable("id") Long id, @RequestBody List<Card> cards){
        Game game = gameService.findById(id);
        gameController.getPlayerTurn(game);
        for(Card card:cards){
            card.setValue(card.getValue().substring(0, card.getValue().length() - 1));
            cardService.save(card);
        }
        return ResponseEntity.ok("Card recovered");
    }

    @PostMapping("/game/{id}/maelstrom")
    public ResponseEntity<String> maelstrom(@PathVariable("id") Long id){
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        Card c = p.getHand().stream()
                .filter(ca -> ca.getName().equals("Maelstrom"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, c);
        for(Player player:game.getPlayers()){
            List<Card> cards = player.getPlayedCards();
            for (Card card:player.getPlayedCards()){
                if(!card.isProtected() && !card.getValue().equals(card.getValue() + '-') && !card.getValue().equals("Joker")){
                    cards.remove(card);
                    game.getDiscardDeck().add(card);
                    cardService.save(card);
                    gameService.save(game);
                }
            }
        }
        return ResponseEntity.ok("Cards removed");
    }

    @PostMapping("/game/{id}/mutiny")
    public ResponseEntity<String> mutiny(@PathVariable("id") Long id, @RequestBody Integer number){
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        Card c = p.getHand().stream()
                .filter(ca -> ca.getName().equals("Mutiny"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, c);
        for(Player player:game.getPlayers()){
            List<Card> cards = player.getPlayedCards();
            for (Card card:player.getPlayedCards()){
                if(!card.isProtected() && !card.getValue().equals(card.getValue() + "-") && !card.getValue().equals("Joker") && Integer.parseInt(card.getValue()) <= number){
                    cards.remove(card);
                    game.getDiscardDeck().add(card);
                    cardService.save(card);
                    gameService.save(game);
                }
            }
        }
        return ResponseEntity.ok("Cards removed");
    }

    @PostMapping("/game/{id}/shiver-me-timbers")
    public ResponseEntity<String> shiverMeTimbers(@PathVariable("id") Long id){
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        Card c = p.getHand().stream()
                .filter(ca -> ca.getName().equals("Shiver Me Timbers"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, c);
        for(Player player:game.getPlayers()){
            List<Card> cards = player.getPlayedCards();
            for (Card card:player.getPlayedCards()){
                if(!card.isProtected() && !card.getValue().equals(card.getValue() + "-") && !card.getValue().equals("Joker") && Integer.parseInt(card.getValue()) % 2 == 0){
                    cards.remove(card);
                    card.setDestroyed(true);
                    game.getDiscardDeck().add(card);
                    cardService.save(card);
                    gameService.save(game);
                }else if(!card.isProtected() && !card.getValue().equals("No value") && !card.getValue().equals("Joker") && Integer.parseInt(card.getValue()) % 2 != 0){
                    cards.remove(card);
                    game.getDiscardDeck().add(card);
                    cardService.save(card);
                    gameService.save(game);
                }
            }
        }
        return ResponseEntity.ok("Cards removed");
    }

    @PostMapping("/game/{id}/lifeboat")
    public ResponseEntity<String> lifeboat(@PathVariable("id") Long id){
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        Card card = p.getHand().stream()
                .filter(c -> c.getName().equals("Lifeboat"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, card);
        for(Player player:game.getPlayers()){
            if(!card.isProtected() && player.getPlayedCards().contains(card)){
                player.getPlayedCards().remove(card);
                card.setDestroyed(true);
                game.getDiscardDeck().add(card);
                playerService.save(player);
                cardService.save(card);
                gameService.save(game);
            }
        }
        return ResponseEntity.ok("Card removed");
    }

    @PostMapping("/game/{id}/jolly-roger")
    public ResponseEntity<String> jollyRoger(@PathVariable("id") Long id){
        Game game = gameService.findById(id);
        Player player = gameController.getPlayerTurn(game);
        Card c = player.getHand().stream()
                .filter(ca -> ca.getName().equals("Jolly Roger"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, c);
        List<Card> deck = game.getDeck();
        Card card = deck.get(0);
        if(!card.isTreasure()){
            deck.remove(card);
            game.setDeck(deck);
            gameService.save(game);
            List<Card> hand = player.getHand();
            hand.add(card);
            player.setHand(hand);
            playerService.save(player);
        }
        return ResponseEntity.ok("Card removed");
    }

    @PostMapping("/game/{id}/ships-wheel")
    public ResponseEntity<String> shipsWheel(@PathVariable("id") Long id, @RequestBody Player player){
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        Card c = p.getHand().stream()
                .filter(ca -> ca.getName().equals("Ship's Wheel"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, c);
        if(gameController.drawCardTurn(id).getStatusCode().equals(HttpStatus.OK)){
            List<Card> deck = game.getDeck();
            Card card = deck.get(0);
            deck.remove(card);
            game.setDeck(deck);
            gameService.save(game);
            List<Card> hand = player.getHand();
            hand.add(card);
            player.setHand(hand);
            playerService.save(player);
        }
        return ResponseEntity.ok("Card removed");
    }

    @PostMapping("/game/{id}/cutlass")
    public ResponseEntity<String> cutlass(@PathVariable("id") Long id, @RequestBody Player player, @RequestBody Card card){
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        Card c = p.getHand().stream()
                .filter(ca -> ca.getName().equals("Cutlass"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, c);
        if(card.getType().equals(CardType.PERMANENT) && !card.isProtected()){
            card.setDestroyed(true);
            player.getPlayedCards().remove(card);
            game.getDiscardDeck().add(card);
            cardService.save(card);
            playerService.save(player);
            gameService.save(game);
        }
        gameController.drawCardTurn(id);
        return ResponseEntity.ok("Card removed");
    }

    @PostMapping("/game/{id}/monkey")
    public ResponseEntity<String> monkey(@PathVariable("id") Long id, @RequestBody Card card){
        Game game = gameService.findById(id);
        Player player = gameController.getPlayerTurn(game);
        Card c = player.getHand().stream()
                .filter(ca -> ca.getName().equals("Monkey"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, c);
        Card recoveredCard = game.getDiscardDeck().get(card.getId());
        recoveredCard.setDestroyed(false);
        game.getDiscardDeck().remove(recoveredCard);
        gameService.save(game);
        player.getHand().add(recoveredCard);
        playerService.save(player);
        return ResponseEntity.ok("Card recovered");
    }

    @PostMapping("/game/{id}/cannon")
    public ResponseEntity<String> Cannon(@PathVariable("id") Long id, @RequestBody Player player, @RequestBody List<Card> cards){  
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        Card c = p.getHand().stream()
                .filter(ca -> ca.getName().equals("Cannon"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, c);
        player.getHand().removeAll(cards);
        for(Card card:cards){
            if(!card.isProtected()){
                game.getDiscardDeck().add(card);
                playerService.save(player);
                gameService.save(game);
            }
        }
        return ResponseEntity.ok("Cards removed");
    }

    @PostMapping("/game/{id}/treasure-map")
    public ResponseEntity<String> TreasureMap(@PathVariable("id") Long id){
        Game game = gameService.findById(id);
        Player player = gameController.getPlayerTurn(game);
        Card c = player.getHand().stream()
                .filter(ca -> ca.getName().equals("Treasure Map"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, c);
        gameController.drawCardTurn(id);
        gameController.drawCardTurn(id);
        return ResponseEntity.ok("Cards added");
    }

    @GetMapping("/game/{id}/kraken")
    public ResponseEntity<String> kraken(@PathVariable("id") Long id){
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        Card c = p.getHand().stream()
                .filter(ca -> ca.getName().equals("Kraken"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, c);
        for(Player player:game.getPlayers()){
            player.getPlayedCards().forEach(card -> {
                if(card.getType().equals(CardType.PERMANENT) && !card.isProtected()){
                    card.setDestroyed(true);
                    player.getPlayedCards().remove(card);
                    game.getDiscardDeck().add(card);
                    cardService.save(card);
                    playerService.save(player);
                    gameService.save(game);
                }
            });
        }
        return ResponseEntity.ok("Cards removed");
    }

    @PostMapping("/game/{id}/skeleton-key")
    public ResponseEntity<Card> skeletonKey(@PathVariable("id") Long id){
        Game game = gameService.findById(id);
        Player player = gameController.getPlayerTurn(game);
        Card c = player.getHand().stream()
                .filter(ca -> ca.getName().equals("Skeleton Key"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, c);
        return gameController.drawCardTurn(id);
    }

    @PostMapping("/game/{id}/spyglass")
    public ResponseEntity<String> spyglass(@PathVariable("id") Long id){
        Game game = gameService.findById(id);
        Player player = gameController.getPlayerTurn(game);
        Card c = player.getHand().stream()
                .filter(ca -> ca.getName().equals("Spyglass"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, c);
        for(Player p:game.getPlayers()){
            if(!p.getUser().getName().equals(game.getTurn())){
                for(Card card:p.getHand()){
                    card.setRevealed(true);
                    cardService.save(card);
                }
            }
        }
        return ResponseEntity.ok("Cards revealed");
    }

    @PostMapping("/game/{id}/pirate-code")
    public ResponseEntity<String> pirateCode(@PathVariable("id") Long id, @RequestBody Card card){
        Game game = gameService.findById(id);
        Player player = gameController.getPlayerTurn(game);
        Card c = player.getHand().stream()
                .filter(ca -> ca.getName().equals("Pirate Code"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, c);
        if(card.isTreasure()){
            card.setProtected(true);
            cardService.save(card);
        }
        return ResponseEntity.ok("Card protected");
    }

    @PostMapping("/game/{id}/lookout")
    public ResponseEntity<String> lookout(@PathVariable("id") Long id, @RequestBody Player player, @RequestBody Card card){ 
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        Card c = p.getHand().stream()
                .filter(ca -> ca.getName().equals("Lookout"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, c);
        player.getPlayedCards().remove(card);
        p.getPlayedCards().add(card);
        playerService.save(player);
        playerService.save(p);
        return ResponseEntity.ok("Card removed");
    }

    @PostMapping("/game/{id}/first-mate")
    public ResponseEntity<String> firstMate(@PathVariable("id") Long id){
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        Card c = p.getHand().stream()
                .filter(ca -> ca.getName().equals("First Mate"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, c);
        for(Card card:p.getPlayedCards()){
            if(card.isTreasure()){
                card.setProtected(true);
                cardService.save(card);
            }
        }
        return ResponseEntity.ok("Card protected");
    }

    @PostMapping("/game/{id}/pirate-king")
    public ResponseEntity<String> pirateKing(@PathVariable("id") Long id){
        Game game = gameService.findById(id);
        Player p = gameController.getPlayerTurn(game);
        Card c = p.getHand().stream()
                .filter(ca -> ca.getName().equals("Pirate King"))
                .findFirst()
                .orElse(null);
        gameController.playAbilityCard(id, c);
        for(Card card:p.getPlayedCards()){
            if(card.isTreasure()){
                card.setValue(String.valueOf((Integer.parseInt(card.getValue())+3)));
                cardService.save(card);
            }
        }
        return ResponseEntity.ok("Increased card value");
    }
}

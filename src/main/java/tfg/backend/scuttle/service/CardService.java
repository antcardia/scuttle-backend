package tfg.backend.scuttle.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tfg.backend.scuttle.entity.Card;
import tfg.backend.scuttle.repository.CardRepository;

@Service
public class CardService {
    
    @Autowired
    private CardRepository cardRepository;

    public List<Card> getCards() {
        Card davyJones = cardRepository.findByName("DavyJones");
        Card robotPirate = cardRepository.findByName("RobotPirate");
        return cardRepository.findAll().stream().filter(card -> !card.equals(davyJones) && !card.equals(robotPirate)).toList();
    }

    public List<Card> getSoloCards() {
        return cardRepository.findAll();
    }

    public Card findByName(String name) {
        return cardRepository.findByName(name);
    }

    public void save(Card card) {
        cardRepository.save(card);
    }
}

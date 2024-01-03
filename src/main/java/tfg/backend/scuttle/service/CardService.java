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
        return cardRepository.findAll().stream().filter(card -> card.getName() != "Davy Jones" && card.getName() != "Robot Pirate").toList();
    }

    public List<Card> getSoloCards() {
        return cardRepository.findAll();
    }

    public void save(Card card) {
        cardRepository.save(card);
    }
}

package tfg.backend.scuttle.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import tfg.backend.scuttle.model.BaseEntity;

@Entity
@Getter
@Setter
@Table(name = "games")
public class Game extends BaseEntity{

    private Integer round;

    private String turn;

    private Integer numPlayers;

    private String host;

    private GameMode mode;

    private Integer time;

    private boolean isActive;

    @OneToMany
    private List<Card> deck;

    @OneToMany
    private List<Card> discardDeck;

    @ManyToMany
    private List<Player> players;

}

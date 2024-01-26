package tfg.backend.scuttle.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import tfg.backend.scuttle.model.BaseEntity;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
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

    private String time;

    private boolean isActive;

    @OneToMany
    private List<Card> deck;

    @OneToMany
    private List<Card> discardDeck;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<Player> players;

}

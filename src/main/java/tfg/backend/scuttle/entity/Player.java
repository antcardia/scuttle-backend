package tfg.backend.scuttle.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import tfg.backend.scuttle.model.BaseEntity;

@Entity
@Getter
@Setter
@Table(name = "players")
public class Player extends BaseEntity{

    private Integer points;

    private boolean isHost;

    private boolean isInGame;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToMany
    private List<Card> hand;

    @OneToMany
    private List<Card> playedCards;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private List<Player> friends;
    
    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "players")
    private List<Game> game;


}

package tfg.backend.scuttle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import tfg.backend.scuttle.model.BaseEntity;

@Entity
@Getter
@Setter
@Table(name = "cards")
public class Card extends BaseEntity{

    @Column(name = "card_value")
    private String value;

    private String image;

    private String name;

    private String description;

    private CardType type;

    private CardCategory category;

    private boolean isProtected;

    private boolean isTreasure;

    private boolean isDestroyed;
    
}

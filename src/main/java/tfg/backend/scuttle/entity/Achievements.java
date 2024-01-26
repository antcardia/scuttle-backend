package tfg.backend.scuttle.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import tfg.backend.scuttle.model.BaseEntity;

@Entity
@Getter
@Setter
@Table(name = "achievements")
public class Achievements extends BaseEntity{

    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "player_id", referencedColumnName = "id")
    private Player player;
}

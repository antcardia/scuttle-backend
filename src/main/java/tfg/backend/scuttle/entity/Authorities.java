package tfg.backend.scuttle.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tfg.backend.scuttle.model.BaseEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Authorities extends BaseEntity{
	
	private String username;
	private String password;
	private String token;
	
}

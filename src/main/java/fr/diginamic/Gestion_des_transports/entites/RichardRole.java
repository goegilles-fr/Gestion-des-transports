package fr.diginamic.Gestion_des_transports.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!

/**
 * Représente un rôle de l'utilisateur
 */
@Entity
public class RichardRole {
    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** ADMIN, USER, etc. */
    private String name; 

	/** Getter
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/** Setter
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/** Getter
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/** Setter
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
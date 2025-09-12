package fr.diginamic.Gestion_des_transports.entites;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!

/**
 * Représente un utilisateur
 */
@Entity
public class RichardUser {
    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** username : email par exemple */
    private String username;
    /** password */
    private String password;
    /** permet de désactiver un utilisateur par exemple */
    private boolean enabled;

    /** Liste des rôles */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "richard_user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RichardRole> roles;

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
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/** Setter
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/** Getter
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/** Setter
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/** Getter
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/** Setter
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/** Getter
	 * @return the roles
	 */
	public Set<RichardRole> getRoles() {
		return roles;
	}

	/** Setter
	 * @param roles the roles to set
	 */
	public void setRoles(Set<RichardRole> roles) {
		this.roles = roles;
	}
}
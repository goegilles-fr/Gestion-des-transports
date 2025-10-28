package fr.diginamic.gestiondestransports.controllers;


/**
 * Classe de requête contenant les informations d'authentification.
 * Utilisée pour transmettre les identifiants (username et password) lors de la connexion.
 * Le username correspond à l'adresse email de l'utilisateur.
 * Le password est transmis en clair dans la requête et sera vérifié contre le hash BCrypt stocké en base.
 */
public class AuthRequest {
    
	/** username */
    private String username;
    /** password */
    private String password;

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
    
}
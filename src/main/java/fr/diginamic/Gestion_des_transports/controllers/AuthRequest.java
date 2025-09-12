package fr.diginamic.Gestion_des_transports.controllers;

/**
 * Objet transmis dans le body de la requête HTTP lors de l'authentification
 *
 *
 * http://localhost:8080/api/auth/login
 *
 *///    {  *"username":"a@a.com","password":"a"}
    //

//INSERT INTO `utilisateur` (email, est_banni, est_verifie, est_supprime, nom, password, prenom, role, adresse_id)
//VALUES ('a@a.com', 0, 1, 0, 'Dmitry', '$2a$10$OnY5DzwXaRl9eF8evJ0RsOOG2FDkFsxBTjT3/VT4PRARem08CJ8ri', 'K', 'ROLE_USER', 1);
//a
//$2a$10$OnY5DzwXaRl9eF8evJ0RsOOG2FDkFsxBTjT3/VT4PRARem08CJ8ri

//INSERT INTO `adresse` (id, code_postal, libelle, numero, ville)
//VALUES (1, '75001', '123 Main Street', '123', 'Paris');


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
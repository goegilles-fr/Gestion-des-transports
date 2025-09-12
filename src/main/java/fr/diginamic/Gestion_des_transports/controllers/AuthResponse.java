package fr.diginamic.Gestion_des_transports.controllers;
// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!

/**
 * Sert à retourner le token JWT dans le body de la réponse
 */
class AuthResponse {
	
    /** token jwt */
    private String jwt;
    
    /** Constructeur
     * @param jwt valeur du token JWT
     */
    public AuthResponse(String jwt) { 
    	this.jwt = jwt; 
    }

	/** Getter
	 * @return the jwt
	 */
	public String getJwt() {
		return jwt;
	}
    
}
package fr.diginamic.Gestion_des_transports.tools;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!
// MADE BY RICHARD !!!!!!!!

/**
 * Petit utilitaire qui sert simplement à encrypter une chaine de caractères
 */
public class BCryptEncodeMain {

	public static void main(String[] args) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.out.println(encoder.encode("a"));
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

	}

}

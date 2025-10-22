package fr.diginamic.gestiondestransports.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.diginamic.gestiondestransports.entites.Adresse;
import fr.diginamic.gestiondestransports.entites.AnnonceCovoiturage;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Locale;

/**
 * Service pour interagir avec l'API OpenStreetMap (Nominatim et OSRM)
 * Permet d'obtenir des coordonnées géographiques et de calculer des itinéraires
 */
@Service
public class OsmApi {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final boolean apiDebug=false;
    // URLs de base pour les APIs
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final String OSRM_URL = "http://router.project-osrm.org/route/v1/driving";

    public OsmApi() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Classe interne pour stocker les coordonnées géographiques
     */
    public static class Coordonnees {
        private final double latitude;
        private final double longitude;

        public Coordonnees(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        @Override
        public String toString() {
            return "Coordonnees{lat=" + latitude + ", lon=" + longitude + "}";
        }
    }

    /**
     * Classe interne pour stocker le résultat du calcul d'itinéraire
     */
    public static class ResultatItineraire {
        private final Integer distanceKm;
        private final Integer dureeMinutes;

        public ResultatItineraire(Integer distanceKm, Integer dureeMinutes) {
            this.distanceKm = distanceKm;
            this.dureeMinutes = dureeMinutes;
        }

        public Integer getDistanceKm() {
            return distanceKm;
        }

        public Integer getDureeMinutes() {
            return dureeMinutes;
        }

        @Override
        public String toString() {
            return "ResultatItineraire{distance=" + distanceKm + " km, duree=" + dureeMinutes + " min}";
        }
    }

    /**
     * Obtient les coordonnées géographiques d'une adresse via l'API Nominatim
     *
     * @param adresse L'adresse à géolocaliser
     * @return Les coordonnées (latitude, longitude) ou null si non trouvée
     */
    public Coordonnees obtenirCoordonnees(Adresse adresse) {
        if (apiDebug) System.out.println("═══════════════════════════════════════════════════════");
        if (apiDebug) System.out.println("🌍 DEBUT obtenirCoordonnees()");
        if (apiDebug) System.out.println("═══════════════════════════════════════════════════════");

        try {
            // Construction de la requête d'adresse
            StringBuilder requeteAdresse = new StringBuilder();

            if (adresse.getNumero() != null) {
                requeteAdresse.append(adresse.getNumero()).append(" ");
            }

            if (adresse.getLibelle() != null && !adresse.getLibelle().trim().isEmpty()) {
                requeteAdresse.append(adresse.getLibelle()).append(", ");
            }

            if (adresse.getVille() != null && !adresse.getVille().trim().isEmpty()) {
                requeteAdresse.append(adresse.getVille());
            }

            String adresseFormatee = requeteAdresse.toString().trim();
            if (apiDebug) System.out.println("📍 Adresse à géolocaliser : " + adresseFormatee);

            // Construction de l'URL pour Nominatim
            String url = NOMINATIM_URL + "?q=" + adresseFormatee.replace(" ", "+")
                    + "&format=json&limit=1";

            if (apiDebug) System.out.println("🔗 URL Nominatim : " + url);

            // Appel à l'API
            String reponse = restTemplate.getForObject(url, String.class);
            if (apiDebug) System.out.println("📦 Réponse brute de Nominatim :");
            if (apiDebug) System.out.println(reponse);

            // Parsing de la réponse JSON
            JsonNode rootNode = objectMapper.readTree(reponse);

            if (rootNode.isArray() && rootNode.size() > 0) {
                JsonNode premierResultat = rootNode.get(0);

                double latitude = premierResultat.get("lat").asDouble();
                double longitude = premierResultat.get("lon").asDouble();

                Coordonnees coords = new Coordonnees(latitude, longitude);
                if (apiDebug) System.out.println("✅ Coordonnées trouvées : " + coords);
                if (apiDebug) System.out.println("═══════════════════════════════════════════════════════");

                return coords;
            } else {
                if (apiDebug) System.out.println("❌ Aucune coordonnée trouvée pour cette adresse");
                if (apiDebug) System.out.println("═══════════════════════════════════════════════════════");
                return null;
            }

        } catch (Exception e) {
            if (apiDebug) System.out.println("❌ ERREUR lors de l'obtention des coordonnées : " + e.getMessage());
            e.printStackTrace();
            if (apiDebug) System.out.println("═══════════════════════════════════════════════════════");
            return null;
        }
    }

    /**
     * Calcule la distance et la durée d'un trajet entre deux points géographiques
     * via l'API OSRM (Open Source Routing Machine)
     *
     * @param latDepart Latitude du point de départ
     * @param lonDepart Longitude du point de départ
     * @param latArrivee Latitude du point d'arrivée
     * @param lonArrivee Longitude du point d'arrivée
     * @return Le résultat contenant la distance en km et la durée en minutes
     */
    public ResultatItineraire calculerDistanceEtDuree(double latDepart, double lonDepart,
                                                      double latArrivee, double lonArrivee) {
        if (apiDebug) System.out.println("═══════════════════════════════════════════════════════");
        if (apiDebug) System.out.println("🚗 DEBUT calculerDistanceEtDuree()");
        if (apiDebug) System.out.println("═══════════════════════════════════════════════════════");

        try {
            // Construction de l'URL OSRM
            // Format : lon,lat;lon,lat (ATTENTION : longitude d'abord, puis latitude !)
            String url = String.format(Locale.US, "%s/%.7f,%.7f;%.7f,%.7f?overview=false&steps=false",
                    OSRM_URL, lonDepart, latDepart, lonArrivee, latArrivee);

            if (apiDebug) System.out.println("🔗 URL OSRM : " + url);
            if (apiDebug) System.out.println("📍 Point de départ : lat=" + latDepart + ", lon=" + lonDepart);
            if (apiDebug) System.out.println("📍 Point d'arrivée : lat=" + latArrivee + ", lon=" + lonArrivee);

            // Appel à l'API
            String reponse = restTemplate.getForObject(url, String.class);
            if (apiDebug) System.out.println("📦 Réponse brute d'OSRM :");
            if (apiDebug) System.out.println(reponse);

            // Parsing de la réponse JSON
            JsonNode rootNode = objectMapper.readTree(reponse);

            String code = rootNode.get("code").asText();
            if (apiDebug) System.out.println("📊 Code de réponse OSRM : " + code);

            if ("Ok".equals(code)) {
                JsonNode routes = rootNode.get("routes");
                if (routes != null && routes.isArray() && routes.size() > 0) {
                    JsonNode premiereRoute = routes.get(0);
                    JsonNode legs = premiereRoute.get("legs");

                    if (legs != null && legs.isArray() && legs.size() > 0) {
                        JsonNode premierLeg = legs.get(0);

                        // Distance en mètres
                        double distanceMetres = premierLeg.get("distance").asDouble();
                        // Durée en secondes
                        double dureeSecondes = premierLeg.get("duration").asDouble();

                        if (apiDebug) System.out.println("📏 Distance brute : " + distanceMetres + " mètres");
                        if (apiDebug) System.out.println("⏱️  Durée brute : " + dureeSecondes + " secondes");

                        // Conversion en km et minutes (arrondi)
                        Integer distanceKm = (int) Math.round(distanceMetres / 1000.0);
                        Integer dureeMinutes = (int) Math.round(dureeSecondes / 60.0);

                        if (apiDebug) System.out.println("📏 Distance convertie : " + distanceKm + " km");
                        if (apiDebug) System.out.println("⏱️  Durée convertie : " + dureeMinutes + " minutes");

                        ResultatItineraire resultat = new ResultatItineraire(distanceKm, dureeMinutes);
                        if (apiDebug) System.out.println("✅ Résultat final : " + resultat);
                        if (apiDebug) System.out.println("═══════════════════════════════════════════════════════");

                        return resultat;
                    }
                }
            }

            if (apiDebug) System.out.println("❌ Impossible de calculer l'itinéraire (code: " + code + ")");
            if (apiDebug) System.out.println("═══════════════════════════════════════════════════════");
            return null;

        } catch (Exception e) {
            if (apiDebug) System.out.println("❌ ERREUR lors du calcul de l'itinéraire : " + e.getMessage());
            e.printStackTrace();
            if (apiDebug) System.out.println("═══════════════════════════════════════════════════════");
            return null;
        }
    }

    /**
     * Enrichit une annonce de covoiturage avec les informations de distance et durée
     * calculées à partir des adresses de départ et d'arrivée
     *
     * @param annonce L'annonce de covoiturage à enrichir
     * @return true si l'enrichissement a réussi, false sinon
     */
    public boolean enrichirAnnonceAvecItineraire(AnnonceCovoiturage annonce) {
        if (apiDebug) System.out.println("\n\n");
        if (apiDebug) System.out.println("╔═══════════════════════════════════════════════════════╗");
        if (apiDebug) System.out.println("║  🚀 ENRICHISSEMENT DE L'ANNONCE DE COVOITURAGE       ║");
        if (apiDebug) System.out.println("╚═══════════════════════════════════════════════════════╝");
        if (apiDebug) System.out.println();

        try {
            // Vérification des adresses
            if (annonce.getAdresseDepart() == null || annonce.getAdresseArrivee() == null) {
                if (apiDebug) System.out.println("❌ ERREUR : L'annonce ne contient pas d'adresse de départ ou d'arrivée");
                return false;
            }

            if (apiDebug) System.out.println("📋 Informations de l'annonce :");
            if (apiDebug) System.out.println("   - ID : " + annonce.getId());
            if (apiDebug) System.out.println("   - Responsable : " + annonce.getResponsable().getPrenom() + " " +
                    annonce.getResponsable().getNom());
            if (apiDebug) System.out.println("   - Départ : " + formatAdresse(annonce.getAdresseDepart()));
            if (apiDebug) System.out.println("   - Arrivée : " + formatAdresse(annonce.getAdresseArrivee()));
            if (apiDebug) System.out.println();

            // Étape 1 : Obtenir les coordonnées de l'adresse de départ
            if (apiDebug) System.out.println("🔍 ÉTAPE 1 : Géolocalisation de l'adresse de DÉPART");
            Coordonnees coordsDepart = obtenirCoordonnees(annonce.getAdresseDepart());

            if (coordsDepart == null) {
                if (apiDebug) System.out.println("❌ Impossible d'obtenir les coordonnées de l'adresse de départ");
                return false;
            }
            if (apiDebug) System.out.println();

            // Étape 2 : Obtenir les coordonnées de l'adresse d'arrivée
            if (apiDebug) System.out.println("🔍 ÉTAPE 2 : Géolocalisation de l'adresse d'ARRIVÉE");
            Coordonnees coordsArrivee = obtenirCoordonnees(annonce.getAdresseArrivee());

            if (coordsArrivee == null) {
                if (apiDebug) System.out.println("❌ Impossible d'obtenir les coordonnées de l'adresse d'arrivée");
                return false;
            }
            if (apiDebug) System.out.println();

            // Étape 3 : Calculer l'itinéraire
            if (apiDebug) System.out.println("🔍 ÉTAPE 3 : Calcul de l'itinéraire");
            ResultatItineraire itineraire = calculerDistanceEtDuree(
                    coordsDepart.getLatitude(),
                    coordsDepart.getLongitude(),
                    coordsArrivee.getLatitude(),
                    coordsArrivee.getLongitude()
            );

            if (itineraire == null) {
                if (apiDebug) System.out.println("❌ Impossible de calculer l'itinéraire");
                return false;
            }
            if (apiDebug) System.out.println();

            // Étape 4 : Mise à jour de l'annonce
            if (apiDebug) System.out.println("🔍 ÉTAPE 4 : Mise à jour de l'annonce");
            if (apiDebug) System.out.println("   📏 Distance calculée : " + itineraire.getDistanceKm() + " km");
            if (apiDebug) System.out.println("   ⏱️  Durée calculée : " + itineraire.getDureeMinutes() + " minutes");

            annonce.setDistance(itineraire.getDistanceKm());
            annonce.setDureeTrajet(itineraire.getDureeMinutes());

            if (apiDebug) System.out.println("✅ Annonce mise à jour avec succès !");
            if (apiDebug) System.out.println();
            if (apiDebug) System.out.println("╔═══════════════════════════════════════════════════════╗");
            if (apiDebug) System.out.println("║  ✨ ENRICHISSEMENT TERMINÉ AVEC SUCCÈS               ║");
            if (apiDebug) System.out.println("╚═══════════════════════════════════════════════════════╝");
            if (apiDebug) System.out.println("\n\n");

            return true;

        } catch (Exception e) {
            if (apiDebug) System.out.println("❌ ERREUR CRITIQUE lors de l'enrichissement : " + e.getMessage());
            e.printStackTrace();
            if (apiDebug) System.out.println();
            if (apiDebug) System.out.println("╔═══════════════════════════════════════════════════════╗");
            if (apiDebug) System.out.println("║  ❌ ENRICHISSEMENT ÉCHOUÉ                            ║");
            if (apiDebug) System.out.println("╚═══════════════════════════════════════════════════════╝");
            if (apiDebug) System.out.println("\n\n");
            return false;
        }
    }

    /**
     * Méthode utilitaire pour formater une adresse de manière lisible
     */
    private String formatAdresse(Adresse adresse) {
        StringBuilder sb = new StringBuilder();
        if (adresse.getNumero() != null) {
            sb.append(adresse.getNumero()).append(" ");
        }
        if (adresse.getLibelle() != null) {
            sb.append(adresse.getLibelle()).append(", ");
        }
        if (adresse.getVille() != null) {
            sb.append(adresse.getVille());
        }
        return sb.toString();
    }
}

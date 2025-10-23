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

            // Construction de l'URL pour Nominatim
            String url = NOMINATIM_URL + "?q=" + adresseFormatee.replace(" ", "+")
                    + "&format=json&limit=1";


            // Appel à l'API
            String reponse = restTemplate.getForObject(url, String.class);


            // Parsing de la réponse JSON
            JsonNode rootNode = objectMapper.readTree(reponse);

            if (rootNode.isArray() && rootNode.size() > 0) {
                JsonNode premierResultat = rootNode.get(0);

                double latitude = premierResultat.get("lat").asDouble();
                double longitude = premierResultat.get("lon").asDouble();

                Coordonnees coords = new Coordonnees(latitude, longitude);


                return coords;
            } else {

                return null;
            }

        } catch (Exception e) {

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

        try {
            // Construction de l'URL OSRM
            // Format : lon,lat;lon,lat (ATTENTION : longitude d'abord, puis latitude !)
            String url = String.format(Locale.US, "%s/%.7f,%.7f;%.7f,%.7f?overview=false&steps=false",
                    OSRM_URL, lonDepart, latDepart, lonArrivee, latArrivee);



            // Appel à l'API
            String reponse = restTemplate.getForObject(url, String.class);

            // Parsing de la réponse JSON
            JsonNode rootNode = objectMapper.readTree(reponse);

            String code = rootNode.get("code").asText();


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


                        // Conversion en km et minutes (arrondi)
                        Integer distanceKm = (int) Math.round(distanceMetres / 1000.0);
                        Integer dureeMinutes = (int) Math.round(dureeSecondes / 60.0);


                        ResultatItineraire resultat = new ResultatItineraire(distanceKm, dureeMinutes);


                        return resultat;
                    }
                }
            }


            return null;

        } catch (Exception e) {

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


        try {
            // Vérification des adresses
            if (annonce.getAdresseDepart() == null || annonce.getAdresseArrivee() == null) {

                return false;
            }


            // Étape 1 : Obtenir les coordonnées de l'adresse de départ

            Coordonnees coordsDepart = obtenirCoordonnees(annonce.getAdresseDepart());

            if (coordsDepart == null) {

                return false;
            }


            // Étape 2 : Obtenir les coordonnées de l'adresse d'arrivée

            Coordonnees coordsArrivee = obtenirCoordonnees(annonce.getAdresseArrivee());

            if (coordsArrivee == null) {

                return false;
            }

            // Étape 3 : Calculer l'itinéraire

            ResultatItineraire itineraire = calculerDistanceEtDuree(
                    coordsDepart.getLatitude(),
                    coordsDepart.getLongitude(),
                    coordsArrivee.getLatitude(),
                    coordsArrivee.getLongitude()
            );

            if (itineraire == null) {

                return false;
            }


            // Étape 4 : Mise à jour de l'annonce

            annonce.setDistance(itineraire.getDistanceKm());
            annonce.setDureeTrajet(itineraire.getDureeMinutes());


            return true;

        } catch (Exception e) {

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

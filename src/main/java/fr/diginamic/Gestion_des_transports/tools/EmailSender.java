package fr.diginamic.Gestion_des_transports.tools;


import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service pour envoyer des emails via Mailjet
 */
@Service
public class EmailSender {

    private final MailjetClient clientMailjet;
    private final String emailExpediteur;
    private final String nomExpediteur;
    private final Long idTemplate;

    /**
     * Constructeur du service EmailSender
     */
    public EmailSender(
            @Value("${mailjet.api.key.public}") String cleApiPublique,
            @Value("${mailjet.api.key.private}") String cleApiPrivee,
            @Value("${mailjet.email.expediteur}") String emailExpediteur,
            @Value("${mailjet.email.nom.expediteur}") String nomExpediteur,
            @Value("${mailjet.template.id}") Long idTemplate) {

        this.emailExpediteur = emailExpediteur;
        this.nomExpediteur = nomExpediteur;
        this.idTemplate = idTemplate;


        // Configuration du client Mailjet
        ClientOptions options = ClientOptions.builder()
                .apiKey(cleApiPublique)
                .apiSecretKey(cleApiPrivee)
                .build();

        this.clientMailjet = new MailjetClient(options);
    }

    /**
     * Envoie un email via Mailjet
     *
     * @param email Email du destinataire
     * @param text Texte personnalisé (variable personalmessage)
     * @param heading Titre personnalisé (variable personalheading)
     * @param subject Sujet de l'email
     */
    public void send(String email, String text, String heading, String subject) {
        System.out.println("sending email to " + email);
        System.out.println("subject = " + subject);
        System.out.println("heading = " + heading);
        System.out.println("text = " + text);

        try {

            // Construction de la requête Mailjet
            MailjetRequest requete = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, new JSONArray()
                            .put(new JSONObject()
                                    .put(Emailv31.Message.FROM, new JSONObject()
                                            .put("Email", emailExpediteur)
                                            .put("Name", nomExpediteur))
                                    .put(Emailv31.Message.TO, new JSONArray()
                                            .put(new JSONObject()
                                                    .put("Email", email)))
                                    .put(Emailv31.Message.VARIABLES, new JSONObject()
                                            .put("personalmessage", text)
                                            .put("personalheading", heading))
                                    .put(Emailv31.Message.TEMPLATEID, idTemplate)
                                    .put(Emailv31.Message.TEMPLATELANGUAGE, true)
                                    .put(Emailv31.Message.SUBJECT, subject)));

            // Envoi de l'email
            MailjetResponse reponse = clientMailjet.post(requete);

            // Affichage du résultat
            if (reponse.getStatus() == 200) {
                System.out.println("Email envoyé avec succès à : " + email);
            } else {
                System.out.println("Erreur lors de l'envoi de l'email. Statut: " + reponse.getStatus());
                System.out.println("Réponse Mailjet: " + reponse.getData());
            }

        } catch (MailjetException e) {
            System.out.println("Erreur Mailjet lors de l'envoi de l'email à " + email + ": " + e.getMessage());
        }
    }
}

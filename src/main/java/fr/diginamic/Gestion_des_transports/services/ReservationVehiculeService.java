package fr.diginamic.Gestion_des_transports.services;

import fr.diginamic.Gestion_des_transports.dto.ReservationVehiculeDTO;
import fr.diginamic.Gestion_des_transports.entites.Utilisateur;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationVehiculeService {
    List<ReservationVehiculeDTO> findAll();
    ReservationVehiculeDTO findById(Utilisateur user, Long id);
    ReservationVehiculeDTO create(Utilisateur user, ReservationVehiculeDTO dto);
    ReservationVehiculeDTO update(Utilisateur user, Long id, ReservationVehiculeDTO dto);
    void delete(Utilisateur user, Long id);
    List<ReservationVehiculeDTO> findByUtilisateurId(Utilisateur user);
    List<ReservationVehiculeDTO> findByVehiculeId(Long vehiculeId);
    ReservationVehiculeDTO findByUtilisateurAndPeriode(Utilisateur user, LocalDateTime dateDebut, Integer dureeMinutes);
}

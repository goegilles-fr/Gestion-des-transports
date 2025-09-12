package fr.diginamic.Gestion_des_transports.service;

import fr.diginamic.Gestion_des_transports.dto.ReservationVehiculeDTO;
import java.util.List;

public interface ReservationVehiculeService {
    List<ReservationVehiculeDTO> findAll();
    ReservationVehiculeDTO findById(Long id);
    ReservationVehiculeDTO create(ReservationVehiculeDTO dto);
    ReservationVehiculeDTO update(Long id, ReservationVehiculeDTO dto);
    void delete(Long id);
    List<ReservationVehiculeDTO> findByUtilisateurId(Long utilisateurId);
    List<ReservationVehiculeDTO> findByVehiculeId(Long vehiculeId);
}

package fr.diginamic.Gestion_des_transports.services;

import fr.diginamic.Gestion_des_transports.dto.VehiculeDTO;
import java.util.List;

public interface VehiculeEntrepriseService {
    List<VehiculeDTO> findAll();
    VehiculeDTO findById(Long id);
    VehiculeDTO create(VehiculeDTO dto);
    VehiculeDTO update(Long id, VehiculeDTO dto);
    void delete(Long id);
    List<VehiculeDTO> findByStatut(String statut);
    List<VehiculeDTO> findByDisponible(String dateDebut, String dateFin);
}

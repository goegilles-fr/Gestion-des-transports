package fr.diginamic.Gestion_des_transports.service;

import fr.diginamic.Gestion_des_transports.dto.VehiculeDTO;
import java.util.List;

public interface VehiculePersonnelService {
    List<VehiculeDTO> findAll();
    VehiculeDTO findById(Long id);
    VehiculeDTO create(VehiculeDTO dto);
    VehiculeDTO update(Long id, VehiculeDTO dto);
    void delete(Long id);
    List<VehiculeDTO> findByUtilisateurId(Long utilisateurId);
}

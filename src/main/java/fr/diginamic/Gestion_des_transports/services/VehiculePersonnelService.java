package fr.diginamic.Gestion_des_transports.services;

import fr.diginamic.Gestion_des_transports.dto.VehiculeDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface VehiculePersonnelService {
    List<VehiculeDTO> findAll();
    VehiculeDTO findById(Long id);
    VehiculeDTO create(Long id, VehiculeDTO dto);
    VehiculeDTO update(Long id, VehiculeDTO dto);
    void delete(Long id);
    void deleteByUtilisateurId(Long utilisateurId);
    List<VehiculeDTO> findByUtilisateurId(Long utilisateurId);
}

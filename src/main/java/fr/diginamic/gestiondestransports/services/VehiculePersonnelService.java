package fr.diginamic.gestiondestransports.services;

import fr.diginamic.gestiondestransports.dto.VehiculeDTO;

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

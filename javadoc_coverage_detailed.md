# 📊 Rapport de Couverture Javadoc

*Généré automatiquement par scan des fichiers sources*

## 📈 Résumé Global

- **Couverture Globale**: 53.8%
- **Classes**: 19/56 (33.9%)
- **Méthodes**: 122/206 (59.2%)
- **Packages analysés**: 12

**Statut**: ⚠️ MOYEN

## 📦 Couverture par Package

| Package | Classes | Méthodes | Couverture | Statut |
|---------|---------|----------|------------|--------|
| gestiondestransports | 0/1 | 0/1 | 0.0% | ❌ |
| config | 0/1 | 0/1 | 0.0% | ❌ |
| controllers | 2/7 | 9/51 | 19.0% | ❌ |
| entites | 7/7 | 72/72 | 100.0% | ✅ |
| enums | 4/4 | 0/0 | 100.0% | ✅ |
| mapper | 0/6 | 0/0 | 0.0% | ❌ |
| repositories | 0/7 | 0/0 | 0.0% | ❌ |
| security | 3/5 | 3/8 | 46.2% | ❌ |
| services | 0/6 | 0/0 | 0.0% | ❌ |
| impl | 1/6 | 34/57 | 55.6% | ⚠️ |
| shared | 0/4 | 0/6 | 0.0% | ❌ |
| tools | 2/2 | 4/10 | 50.0% | ⚠️ |

## 📄 Détails par Fichier

### Package: fr.diginamic.gestiondestransports

#### ❌ GestionDesTransportsApplication.java (0%)

**Non documenté**:
- 🔴 Classe `GestionDesTransportsApplication` (ligne 12)
- 🔴 Méthode `init()` (ligne 19)

### Package: fr.diginamic.gestiondestransports.config

#### ❌ OpenApiConfig.java (0%)

**Non documenté**:
- 🔴 Classe `OpenApiConfig` (ligne 10)
- 🔴 Méthode `customOpenAPI()` (ligne 13)

### Package: fr.diginamic.gestiondestransports.controllers

#### ❌ AnnonceCovoiturageController.java (18%)

**Non documenté**:
- 🔴 Méthode `modifierAnnonce()` (ligne 72)
- 🔴 Méthode `supprimerAnnonce()` (ligne 103)
- 🔴 Méthode `obtenirAnnonce()` (ligne 132)
- 🔴 Méthode `reserverPlace()` (ligne 158)
- 🔴 Méthode `annulerReservation()` (ligne 190)
- 🔴 Méthode `obtenirToutesLesAnnonces()` (ligne 220)
- 🔴 Méthode `obtenirToutesLesReservationsUtilisateur()` (ligne 238)
- 🔴 Méthode `obtenirToutesLesAnnoncesUtilisateur()` (ligne 262)
- 🔴 Méthode `obtenirParticipants()` (ligne 289)

#### ⚠️ AuthController.java (50%)

**Non documenté**:
- 🔴 Méthode `login()` (ligne 61)
- 🔴 Méthode `register()` (ligne 90)

#### ⚠️ AuthRequest.java (80%)

**Non documenté**:
- 🔴 Classe `AuthRequest` (ligne 5)

#### ❌ ReservationVehiculeController.java (11%)

**Non documenté**:
- 🔴 Classe `ReservationVehiculeController` (ligne 27)
- 🔴 Méthode `getAll()` (ligne 39)
- 🔴 Méthode `getById()` (ligne 46)
- 🔴 Méthode `create()` (ligne 54)
- 🔴 Méthode `update()` (ligne 66)
- 🔴 Méthode `getByUtilisateur()` (ligne 90)
- 🔴 Méthode `getByUtilisateurAndPeriode()` (ligne 102)
- 🔴 Méthode `getByVehicule()` (ligne 118)

#### ❌ UtilisateurController.java (14%)

**Non documenté**:
- 🔴 Classe `UtilisateurController` (ligne 25)
- 🔴 Méthode `obtenirTousLesUtilisateurs()` (ligne 42)
- 🔴 Méthode `obtenirUtilisateurParId()` (ligne 122)
- 🔴 Méthode `obtenirUtilisateurParEmail()` (ligne 140)
- 🔴 Méthode `obtenirProfilUtilisateurConnecte()` (ligne 164)
- 🔴 Méthode `obtenirMaVoiture()` (ligne 194)
- 🔴 Méthode `getUtilisateursByRole()` (ligne 237)
- 🔴 Méthode `getUtilisateursNonVerifies()` (ligne 257)
- 🔴 Méthode `bannirUtilisateur()` (ligne 275)
- 🔴 Méthode `verifierUtilisateur()` (ligne 303)
- 🔴 Méthode `modifierProfilUtilisateurConnecte()` (ligne 333)
- 🔴 Méthode `supprimerUtilisateur()` (ligne 373)

#### ❌ VehiculeEntrepriseController.java (0%)

**Non documenté**:
- 🔴 Classe `VehiculeEntrepriseController` (ligne 22)
- 🔴 Méthode `getAll()` (ligne 32)
- 🔴 Méthode `getVehiculesEntrepriseDisponibles()` (ligne 39)
- 🔴 Méthode `getById()` (ligne 54)
- 🔴 Méthode `create()` (ligne 60)
- 🔴 Méthode `update()` (ligne 70)
- 🔴 Méthode `delete()` (ligne 77)
- 🔴 Méthode `getByStatut()` (ligne 85)

#### ❌ VehiculePersonnelController.java (0%)

**Non documenté**:
- 🔴 Classe `VehiculePersonnelController` (ligne 23)
- 🔴 Méthode `getAll()` (ligne 35)
- 🔴 Méthode `getById()` (ligne 41)
- 🔴 Méthode `create()` (ligne 48)
- 🔴 Méthode `update()` (ligne 58)
- 🔴 Méthode `delete()` (ligne 67)
- 🔴 Méthode `getByUtilisateur()` (ligne 76)

### Package: fr.diginamic.gestiondestransports.entites

#### ✅ Adresse.java (100%)

#### ✅ AnnonceCovoiturage.java (100%)

#### ✅ CovoituragePassagers.java (100%)

#### ✅ ReservationVehicule.java (100%)

#### ✅ Utilisateur.java (100%)

#### ✅ VehiculeEntreprise.java (100%)

#### ✅ VehiculePersonnel.java (100%)

### Package: fr.diginamic.gestiondestransports.enums

#### ✅ Categorie.java (100%)

#### ✅ Motorisation.java (100%)

#### ✅ RoleEnum.java (100%)

#### ✅ StatutVehicule.java (100%)

### Package: fr.diginamic.gestiondestransports.mapper

#### ❌ AdresseMapper.java (0%)

**Non documenté**:
- 🔴 Classe `AdresseMapper` (ligne 16)

#### ❌ AnnonceCovoiturageMapper.java (0%)

**Non documenté**:
- 🔴 Classe `AnnonceCovoiturageMapper` (ligne 18)

#### ❌ ModifierProfilMapper.java (0%)

**Non documenté**:
- 🔴 Classe `ModifierProfilMapper` (ligne 16)

#### ❌ ReservationVehiculeMapper.java (0%)

**Non documenté**:
- 🔴 Classe `ReservationVehiculeMapper` (ligne 16)

#### ❌ UtilisateurMapper.java (0%)

**Non documenté**:
- 🔴 Classe `UtilisateurMapper` (ligne 19)

#### ❌ VehiculeMapper.java (0%)

**Non documenté**:
- 🔴 Classe `VehiculeMapper` (ligne 18)

### Package: fr.diginamic.gestiondestransports.repositories

#### ❌ AdresseRepository.java (0%)

**Non documenté**:
- 🔴 Classe `AdresseRepository` (ligne 12)

#### ❌ AnnonceCovoiturageRepository.java (0%)

**Non documenté**:
- 🔴 Classe `AnnonceCovoiturageRepository` (ligne 14)

#### ❌ CovoituragePassagersRepository.java (0%)

**Non documenté**:
- 🔴 Classe `CovoituragePassagersRepository` (ligne 14)

#### ❌ ReservationVehiculeRepository.java (0%)

**Non documenté**:
- 🔴 Classe `ReservationVehiculeRepository` (ligne 13)

#### ❌ UtilisateurRepository.java (0%)

**Non documenté**:
- 🔴 Classe `UtilisateurRepository` (ligne 14)

#### ❌ VehiculeEntrepriseRepository.java (0%)

**Non documenté**:
- 🔴 Classe `VehiculeEntrepriseRepository` (ligne 8)

#### ❌ VehiculePersonnelRepository.java (0%)

**Non documenté**:
- 🔴 Classe `VehiculePersonnelRepository` (ligne 10)

### Package: fr.diginamic.gestiondestransports.security

#### ❌ CorsConfig.java (0%)

**Non documenté**:
- 🔴 Classe `CorsConfig` (ligne 12)
- 🔴 Méthode `corsConfigurationSource()` (ligne 15)

#### ⚠️ CustomUserDetailsService.java (50%)

**Non documenté**:
- 🔴 Méthode `loadUserByUsername()` (ligne 27)

#### ✅ JwtAuthenticationFilter.java (100%)

#### ✅ JwtUtil.java (100%)

#### ❌ SecurityConfig.java (0%)

**Non documenté**:
- 🔴 Classe `SecurityConfig` (ligne 23)
- 🔴 Méthode `securityFilterChain()` (ligne 33)
- 🔴 Méthode `authenticationManager()` (ligne 66)
- 🔴 Méthode `passwordEncoder()` (ligne 71)

### Package: fr.diginamic.gestiondestransports.services

#### ❌ AdresseService.java (0%)

**Non documenté**:
- 🔴 Classe `AdresseService` (ligne 8)

#### ❌ AnnonceCovoiturageService.java (0%)

**Non documenté**:
- 🔴 Classe `AnnonceCovoiturageService` (ligne 9)

#### ❌ ReservationVehiculeService.java (0%)

**Non documenté**:
- 🔴 Classe `ReservationVehiculeService` (ligne 9)

#### ❌ UtilisateurService.java (0%)

**Non documenté**:
- 🔴 Classe `UtilisateurService` (ligne 13)

#### ❌ VehiculeEntrepriseService.java (0%)

**Non documenté**:
- 🔴 Classe `VehiculeEntrepriseService` (ligne 8)

#### ❌ VehiculePersonnelService.java (0%)

**Non documenté**:
- 🔴 Classe `VehiculePersonnelService` (ligne 7)

### Package: fr.diginamic.gestiondestransports.services.impl

#### ⚠️ AdresseServiceImpl.java (86%)

**Non documenté**:
- 🔴 Classe `AdresseServiceImpl` (ligne 15)

#### ⚠️ AnnonceCovoiturageServiceImpl.java (92%)

**Non documenté**:
- 🔴 Méthode `annulerReservation()` (ligne 343)

#### ❌ ReservationVehiculeServiceImpl.java (0%)

**Non documenté**:
- 🔴 Classe `ReservationVehiculeServiceImpl` (ligne 24)
- 🔴 Méthode `findAll()` (ligne 42)
- 🔴 Méthode `findById()` (ligne 47)
- 🔴 Méthode `create()` (ligne 57)
- 🔴 Méthode `update()` (ligne 76)
- 🔴 Méthode `findByUtilisateurAndPeriode()` (ligne 107)
- 🔴 Méthode `delete()` (ligne 137)
- 🔴 Méthode `findByUtilisateurId()` (ligne 206)
- 🔴 Méthode `findByVehiculeId()` (ligne 211)

#### ⚠️ UtilisateurServiceImpl.java (94%)

**Non documenté**:
- 🔴 Classe `UtilisateurServiceImpl` (ligne 28)

#### ❌ VehiculeEntrepriseServiceImpl.java (0%)

**Non documenté**:
- 🔴 Classe `VehiculeEntrepriseServiceImpl` (ligne 22)
- 🔴 Méthode `findAll()` (ligne 38)
- 🔴 Méthode `findByAvailability()` (ligne 42)
- 🔴 Méthode `findById()` (ligne 106)
- 🔴 Méthode `create()` (ligne 113)
- 🔴 Méthode `update()` (ligne 142)
- 🔴 Méthode `delete()` (ligne 178)
- 🔴 Méthode `findByStatut()` (ligne 186)

#### ❌ VehiculePersonnelServiceImpl.java (0%)

**Non documenté**:
- 🔴 Classe `VehiculePersonnelServiceImpl` (ligne 23)
- 🔴 Méthode `findAll()` (ligne 39)
- 🔴 Méthode `findById()` (ligne 44)
- 🔴 Méthode `create()` (ligne 51)
- 🔴 Méthode `update()` (ligne 95)
- 🔴 Méthode `delete()` (ligne 127)
- 🔴 Méthode `deleteByUtilisateurId()` (ligne 135)
- 🔴 Méthode `findByUtilisateurId()` (ligne 145)

### Package: fr.diginamic.gestiondestransports.shared

#### ❌ ApiExceptionHandler.java (0%)

**Non documenté**:
- 🔴 Classe `ApiExceptionHandler` (ligne 17)
- 🔴 Méthode `handleBadRequest()` (ligne 20)
- 🔴 Méthode `handleConflict()` (ligne 25)
- 🔴 Méthode `handleNotFound()` (ligne 30)
- 🔴 Méthode `handleValidation()` (ligne 35)
- 🔴 Méthode `handleConstraint()` (ligne 44)
- 🔴 Méthode `handleOther()` (ligne 52)

#### ❌ BadRequestException.java (0%)

**Non documenté**:
- 🔴 Classe `BadRequestException` (ligne 3)

#### ❌ ConflictException.java (0%)

**Non documenté**:
- 🔴 Classe `ConflictException` (ligne 3)

#### ❌ NotFoundException.java (0%)

**Non documenté**:
- 🔴 Classe `NotFoundException` (ligne 3)

### Package: fr.diginamic.gestiondestransports.tools

#### ✅ EmailSender.java (100%)

#### ❌ OsmApi.java (40%)

**Non documenté**:
- 🔴 Méthode `getLatitude()` (ligne 42)
- 🔴 Méthode `getLongitude()` (ligne 46)
- 🔴 Méthode `toString()` (ligne 51)
- 🔴 Méthode `getDistanceKm()` (ligne 68)
- 🔴 Méthode `getDureeMinutes()` (ligne 72)
- 🔴 Méthode `toString()` (ligne 77)

## 🎯 Plan d'Action

1. **Documenter 37 classe(s)**
2. **Documenter 84 méthode(s)**

**Recommandations**:
- Utiliser `add_javadoc.py` pour automatiser les getters/setters
- Documenter manuellement les méthodes métier importantes
- Prioriser les classes de service (mentionnées dans le cahier des charges)
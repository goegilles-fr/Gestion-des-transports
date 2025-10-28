# 📊 Rapport de Couverture Javadoc

*Généré automatiquement par scan des fichiers sources*

- **Couverture Globale**: 92.7%
- **Classes**: 52/56 (92.9%)
- **Méthodes**: 191/206 (92.7%)
- **Packages analysés**: 12

## 📦 Couverture par Package

| Package | Classes | Méthodes | Couverture | Statut |
|---------|---------|----------|------------|--------|
| gestiondestransports | 1/1 | 1/1 | 100.0% | ✅ |
| config | 1/1 | 1/1 | 100.0% | ✅ |
| controllers | 7/7 | 51/51 | 100.0% | ✅ |
| entites | 7/7 | 72/72 | 100.0% | ✅ |
| enums | 4/4 | 0/0 | 100.0% | ✅ |
| mapper | 6/6 | 0/0 | 100.0% | ✅ |
| repositories | 7/7 | 0/0 | 100.0% | ✅ |
| security | 5/5 | 8/8 | 100.0% | ✅ |
| services | 6/6 | 0/0 | 100.0% | ✅ |
| impl | 2/6 | 42/57 | 69.8% | ⚠️ |
| shared | 4/4 | 6/6 | 100.0% | ✅ |
| tools | 2/2 | 10/10 | 100.0% | ✅ |

## 📄 Détails par Fichier

### Package: fr.diginamic.gestiondestransports

#### ✅ GestionDesTransportsApplication.java (100%)

### Package: fr.diginamic.gestiondestransports.config

#### ✅ OpenApiConfig.java (100%)

### Package: fr.diginamic.gestiondestransports.controllers

#### ✅ AnnonceCovoiturageController.java (100%)

#### ✅ AuthController.java (100%)

#### ✅ AuthRequest.java (100%)

#### ✅ ReservationVehiculeController.java (100%)

#### ✅ UtilisateurController.java (100%)

#### ✅ VehiculeEntrepriseController.java (100%)

#### ✅ VehiculePersonnelController.java (100%)

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

#### ✅ AdresseMapper.java (100%)

#### ✅ AnnonceCovoiturageMapper.java (100%)

#### ✅ ModifierProfilMapper.java (100%)

#### ✅ ReservationVehiculeMapper.java (100%)

#### ✅ UtilisateurMapper.java (100%)

#### ✅ VehiculeMapper.java (100%)

### Package: fr.diginamic.gestiondestransports.repositories

#### ✅ AdresseRepository.java (100%)

#### ✅ AnnonceCovoiturageRepository.java (100%)

#### ✅ CovoituragePassagersRepository.java (100%)

#### ✅ ReservationVehiculeRepository.java (100%)

#### ✅ UtilisateurRepository.java (100%)

#### ✅ VehiculeEntrepriseRepository.java (100%)

#### ✅ VehiculePersonnelRepository.java (100%)

### Package: fr.diginamic.gestiondestransports.security

#### ✅ CorsConfig.java (100%)

#### ✅ CustomUserDetailsService.java (100%)

#### ✅ JwtAuthenticationFilter.java (100%)

#### ✅ JwtUtil.java (100%)

#### ✅ SecurityConfig.java (100%)

### Package: fr.diginamic.gestiondestransports.services

#### ✅ AdresseService.java (100%)

#### ✅ AnnonceCovoiturageService.java (100%)

#### ✅ ReservationVehiculeService.java (100%)

#### ✅ UtilisateurService.java (100%)

#### ✅ VehiculeEntrepriseService.java (100%)

#### ✅ VehiculePersonnelService.java (100%)

### Package: fr.diginamic.gestiondestransports.services.impl

#### ⚠️ AdresseServiceImpl.java (86%)

**Non documenté**:
- 🔴 Classe `AdresseServiceImpl` (ligne 15)

#### ⚠️ AnnonceCovoiturageServiceImpl.java (92%)

**Non documenté**:
- 🔴 Méthode `annulerReservation()` (ligne 343)

#### ✅ ReservationVehiculeServiceImpl.java (100%)

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

#### ✅ ApiExceptionHandler.java (100%)

#### ✅ BadRequestException.java (100%)

#### ✅ ConflictException.java (100%)

#### ✅ NotFoundException.java (100%)

### Package: fr.diginamic.gestiondestransports.tools

#### ✅ EmailSender.java (100%)

#### ✅ OsmApi.java (100%)

# 🚗 Application de Gestion des Transports (Covoiturage)

## 📋 Description

Application web de gestion des transports permettant aux collaborateurs d'organiser des covoiturages et de réserver des véhicules de service pour leurs déplacements professionnels.

### Fonctionnalités principales

- **Covoiturage** : Organisation et réservation de places dans des covoiturages
- **Véhicules de service** : Réservation et gestion du parc de véhicules d'entreprise
- **Gestion des utilisateurs** : Création de comptes, authentification et gestion des profils
- **Administration** : Gestion du parc de véhicules (ajout, modification, suppression)

## 🛠️ Technologies utilisées

- **Backend** : Java 21, Spring Boot 3.x, Hibernate/JPA
- **Base de données** : MySQL
- **Build** : Maven
- **Tests** : JUnit, JaCoCo (couverture de code)
- **Qualité de code** : SonarQube
- **CI/CD** : GitHub Actions
- **Conteneurisation** : Docker
- **API Email** : Mailjet (notifications)

## 🏗️ Architecture

L'application suit une architecture en couches :

```
Controllers → DTOs → Mappers → Services → Repositories → Entités JPA
```

- **Controllers** : Gestion des endpoints REST
- **DTOs** : Objets de transfert de données
- **Mappers** : Conversion entre entités et DTOs
- **Services** : Logique métier
- **Repositories** : Accès aux données via JPA
- **Entités** : Modèle de données persisté

### Modèle de données

- **Utilisateur** : Gestion des collaborateurs et administrateurs
- **Adresse** : Adresses de départ et d'arrivée
- **AnnonceCovoiturage** : Annonces de covoiturage avec véhicule associé
- **CovoituragePassagers** : Table d'association pour les réservations
- **VehiculePersonnel** : Véhicules personnels des collaborateurs
- **VehiculeService** : Parc de véhicules d'entreprise
- **VehiculeServiceReservations** : Réservations des véhicules de service

## 🚀 Installation et exécution

### Prérequis

- Java 21
- Maven 3.8+
- MySQL 8.0+
- Docker (optionnel)

### Configuration

1. Cloner le repository :
```bash
git clone <url-du-repo>
cd gestion-transports
```

2. Configurer la base de données MySQL :
```sql
CREATE DATABASE covoit_db;
```

3. Configurer les variables d'environnement dans `application.properties` ou via des variables d'environnement :
```properties
DB_URL_COVOIT=jdbc:mysql://localhost:3306/covoit_db
DB_USER_COVOIT=votre_user
DB_PASS_COVOIT=votre_password
MJ_APIKEY_PUBLIC=votre_cle_publique_mailjet
MJ_APIKEY_PRIVATE=votre_cle_privee_mailjet
```

### Exécution en local

```bash
# Compiler et lancer l'application
mvn spring-boot:run
```

L'application sera accessible sur `http://localhost:8080`

### Exécution avec Docker

```bash
# Build de l'image Docker
docker build -t covoit-app .

# Lancement du conteneur
docker run -p 8080:8080 \
  -e DB_URL_COVOIT=jdbc:mysql://host.docker.internal:3306/covoit_db \
  -e DB_USER_COVOIT=votre_user \
  -e DB_PASS_COVOIT=votre_password \
  -e MJ_APIKEY_PUBLIC=votre_cle_publique \
  -e MJ_APIKEY_PRIVATE=votre_cle_privee \
  covoit-app
```

Ou utiliser Docker Compose :
```bash
docker-compose up -d
```

## 🧪 Tests

### Exécuter tous les tests

```bash
mvn test
```

### Exécuter les tests avec rapport de couverture JaCoCo

```bash
mvn clean verify
```

### Consulter le rapport JaCoCo

Après avoir exécuté `mvn verify`, le rapport de couverture est généré dans :

```
target/site/jacoco/index.html
```

Ouvrez ce fichier dans un navigateur pour visualiser le rapport détaillé de couverture de code.

### Analyse de qualité avec SonarQube

```bash
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=votre_project_key \
  -Dsonar.host.url=http://votre-sonarqube-url \
  -Dsonar.login=votre_token
```

## 📦 Déploiement

### Déploiement automatique via GitHub Actions

Le déploiement est automatisé via GitHub Actions. À chaque push sur la branche `main` :

1. **Build & Test** : Compilation, tests unitaires et analyse SonarQube
2. **Docker Build** : Construction de l'image Docker
3. **Docker Push** : Envoi vers Docker Hub (penumbriel/covoit)
4. **Deploy** : Déploiement automatique sur le serveur de production

### Déploiement manuel

```bash
# Pull de la dernière image
docker pull penumbriel/covoit:latest

# Arrêt des conteneurs existants
docker-compose down

# Démarrage avec la nouvelle version
docker-compose up -d
```

## 📚 Documentation API

La documentation Swagger de l'API est accessible à l'adresse :

```
http://localhost:8080/swagger-ui/index.html
```

En production : `https://dev.goegilles.fr/swagger-ui/index.html`

## 🔐 Sécurité

- Authentification par JWT
- Gestion des rôles (COLLABORATEUR, ADMINISTRATEUR)
- Validation des données côté serveur
- Protection contre les injections SQL via JPA

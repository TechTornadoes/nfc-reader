# Projet Android - Validation NFC avec Application Web

Ce projet Android est conçu pour fonctionner de paire avec une application web. Son but est de vérifier l'identité d'une personne en validant un code généré par l'application web, puis en scannant un tag NFC. Toutes les vérifications sont effectuées via une API pour s'assurer que le tag NFC est valide et correspond à une personne habilitée.

## Fonctionnalités

1. **Validation de code** : Saisie d'un code généré par l'application web et validation via une API.
2. **Scan NFC** : Lecture des données NFC après validation du code.
3. **Vérification d'identité** : Confirmation que le tag NFC correspond bien à une personne habilitée.
4. **Popup de validation** : Affichage d'une popup indiquant si le scan NFC est accepté ou refusé.
5. **Communication API** : Envoi des résultats de validation (code et NFC) à l'API pour validation ou refus de la connexion en fonction du token.

## Structure du projet

### Fichiers principaux

- **MainActivity.kt** : Gère l'activité principale, y compris la validation du code.
- **NfcScanActivity.kt** : Gère l'activité de scan NFC.
- **ApiService.kt** : Service pour les appels API.
- **JwtResponse.kt** : Modèle de réponse pour le JWT.
- **ValidationResponse.kt** : Modèle de réponse pour la validation.

### Layouts XML

- **activity_main.xml** : Interface utilisateur pour l'activité principale avec un champ de saisie de code et un bouton de validation.
- **activity_nfc_scan.xml** : Interface utilisateur pour l'activité de scan NFC avec une TextView pour afficher les données NFC.
- **rounded_border.xml** : Définition des bordures arrondies pour la mise en forme des éléments UI.

## Configuration requise

- Android Studio 4.1 ou plus récent
- SDK Android 21 ou supérieur

## Instructions d'installation

1. Clonez le dépôt.
   ```bash
   git clone https://github.com/HugoBiegas/nfc-mobile-code-hackaton.git
   ```
2. Ouvrez le projet avec Android Studio.
3. Synchronisez les dépendances du projet.
4. Exécutez l'application sur un émulateur ou un appareil Android physique.

## Utilisation

1. Lancez l'application.
2. Entrez le code généré par l'application web dans le champ prévu et appuyez sur le bouton "Validate".
3. Si le code est valide, accédez à l'écran de scan NFC et approchez un tag NFC compatible de l'appareil.
4. Une popup s'affichera pour indiquer si le tag NFC est accepté ou refusé.

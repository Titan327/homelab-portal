# Configuration SSH pour Hosting Portal

## Prérequis

- Accès root au serveur Proxmox
- Docker installé sur votre machine de développement

## Étape 1 : Créer l'utilisateur sur le serveur Proxmox

### 1.1 Créer l'utilisateur hosting-portal
```bash
# Sur le serveur Proxmox en tant que root
useradd -m -s /bin/bash hosting-portal

# Définir un mot de passe (optionnel, car on utilisera les clés SSH)
passwd hosting-portal
```

### 1.2 Ajouter l'utilisateur aux groupes nécessaires
```bash
# Ajouter au groupe sudo (pour les commandes administratives)
usermod -aG sudo hosting-portal

# Ajouter au groupe www-data (accès Proxmox)
usermod -aG www-data hosting-portal
```

### 1.3 Configurer le PATH de l'utilisateur
```bash
# Ajouter /usr/sbin et /sbin au PATH
echo 'export PATH=$PATH:/usr/sbin:/sbin' >> /home/hosting-portal/.bashrc
```

### 1.4 Configurer sudo sans mot de passe
```bash
# Créer un fichier sudoers dédié pour hosting-portal
sudo visudo -f /etc/sudoers.d/hosting-portal
```

**Ajoutez cette ligne dans le fichier :**
```
hosting-portal ALL=(ALL) NOPASSWD: /usr/sbin/pct, /usr/bin/pvesh
```

Sauvegardez avec `Ctrl+X`, puis `Y`, puis `Entrée`.

### 1.5 Vérifier la configuration
```bash
# Se connecter en tant que hosting-portal
su - hosting-portal

# Vérifier les groupes
groups
# Doit afficher : hosting-portal sudo www-data

# Vérifier le PATH
echo $PATH
# Doit contenir /usr/sbin et /sbin

# Tester sudo SANS mot de passe
sudo pct list
sudo pvesh get /nodes
# Ne doit PAS demander de mot de passe

# Retourner en root
exit
```

## Étape 2 : Créer le fichier known_hosts

Le fichier `known_hosts` permet de vérifier l'identité du serveur SSH et d'éviter les attaques man-in-the-middle.

### 2.1 Scanner les clés SSH du serveur

Sur votre **machine locale** :
```bash
# Créer le dossier ssh_credentials s'il n'existe pas
mkdir -p ssh_credentials

# Scanner les clés SSH du serveur
ssh-keyscan <IP_SERVEUR> > ssh_credentials/known_hosts

# Exemple avec l'IP 192.168.1.28
ssh-keyscan 192.168.1.28 > ssh_credentials/known_hosts
```

## Étape 3 : Générer et configurer les clés SSH

### 3.1 Générer la paire de clés

Sur votre **machine locale** :
```bash
# Générer une clé SSH (sans passphrase pour l'automatisation)
ssh-keygen -t rsa -b 4096 -f ssh_credentials/hosting-portal-key -N ""
```

Cela créera deux fichiers :
- `hosting-portal-key` (clé privée)️ **NE JAMAIS PARTAGER**
- `hosting-portal-key.pub` (clé publique)

### 3.2 Sécuriser la clé privée
```bash
# Définir les bonnes permissions
chmod 600 ssh_credentials/hosting-portal-key
chmod 644 ssh_credentials/hosting-portal-key.pub
```

### 3.3 Copier la clé publique sur le serveur
```bash
# Méthode 1 : Avec ssh-copy-id (recommandé)
ssh-copy-id -i ssh_credentials/hosting-portal-key.pub hosting-portal@<IP_SERVEUR>

# Méthode 2 : Manuellement
cat ssh_credentials/hosting-portal-key.pub | ssh hosting-portal@<IP_SERVEUR> "mkdir -p ~/.ssh && chmod 700 ~/.ssh && cat >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys"
```

### 3.4 Tester la connexion SSH
```bash
# Tester la connexion avec la clé privée
ssh -i ssh_credentials/hosting-portal-key hosting-portal@<IP_SERVEUR>
```

Si la connexion fonctionne **sans demander de mot de passe**, c'est bon !

## Étape 4 : Vérifier les permissions Proxmox

### 4.1 Se connecter au serveur
```bash
# Depuis votre machine locale
ssh -i ssh_credentials/hosting-portal-key hosting-portal@<IP_SERVEUR>
```

### 4.2 Tester les commandes Proxmox avec sudo
```bash
# Tester sudo pct (NE DOIT PAS demander de mot de passe)
sudo pct list

# Tester sudo pvesh (NE DOIT PAS demander de mot de passe)
sudo pvesh get /nodes

# Si ça demande un mot de passe, vérifiez /etc/sudoers.d/hosting-portal
```

Si tout fonctionne **sans demander de mot de passe**, parfait !

## Étape 5 : Structure des fichiers dans le projet

### 5.1 Structure du dossier ssh_credentials

Votre dossier `ssh_credentials/` doit contenir :
```
ssh_credentials/
├── known_hosts              # Clés publiques du serveur
├── hosting-portal-key       # Clé privée (ne pas commit)
└── hosting-portal-key.pub   # Clé publique
```

### 5.2 Configurer .gitignore

**Important :** Ajoutez ces lignes à votre `.gitignore` :
```gitignore
# Ne jamais commiter les credentials SSH sensibles
ssh_credentials/hosting-portal-key
ssh_credentials/known_hosts
```

La clé publique peut être commitée si nécessaire, mais **jamais la clé privée** !

## Ressources

- [Documentation SSH](https://www.ssh.com/academy/ssh)
- [Documentation Proxmox API](https://pve.proxmox.com/wiki/Proxmox_VE_API)
- [Guide Proxmox PCT](https://pve.proxmox.com/wiki/Linux_Container)
- [Guide JSch](http://www.jcraft.com/jsch/)
- [Best practices SSH](https://www.ssh.com/academy/ssh/keygen)
- [Sudoers Documentation](https://www.sudo.ws/docs/man/sudoers.man/)

## Checklist rapide

**Configuration serveur Proxmox :**
- [ ] Utilisateur `hosting-portal` créé
- [ ] Utilisateur ajouté aux groupes `sudo` et `www-data`
- [ ] PATH configuré avec `/usr/sbin` et `/sbin`
- [ ] Fichier `/etc/sudoers.d/hosting-portal` créé avec NOPASSWD
- [ ] `sudo pct list` fonctionne sans mot de passe

**Configuration machine locale :**
- [ ] Dossier `ssh_credentials/` créé
- [ ] Fichier `known_hosts` généré et placé
- [ ] Paire de clés SSH générée
- [ ] Permissions correctes sur la clé privée (600)
- [ ] Clé publique copiée sur le serveur
- [ ] Connexion SSH testée avec succès
- [ ] `.gitignore` configuré

**Configuration application :**
- [ ] Script `test.sh` utilise `sudo` devant `pct`
- [ ] `SshService.java` modifié pour logger stdout+stderr
- [ ] Volumes Docker configurés correctement
- [ ] Application Docker testée
- [ ] LXC créé et démarré avec succès

## Résumé des commandes clés
```bash
# Sur le serveur Proxmox (en tant que root)
useradd -m -s /bin/bash hosting-portal
usermod -aG sudo,www-data hosting-portal
echo 'export PATH=$PATH:/usr/sbin:/sbin' >> /home/hosting-portal/.bashrc
visudo -f /etc/sudoers.d/hosting-portal
# Ajouter : hosting-portal ALL=(ALL) NOPASSWD: /usr/sbin/pct, /usr/bin/pvesh

# Sur votre machine locale
mkdir -p ssh_credentials
ssh-keyscan <IP_SERVEUR> > ssh_credentials/known_hosts
ssh-keygen -t rsa -b 4096 -f ssh_credentials/hosting-portal-key -N ""
chmod 600 ssh_credentials/hosting-portal-key
ssh-copy-id -i ssh_credentials/hosting-portal-key.pub hosting-portal@<IP_SERVEUR>

# Tester
ssh -i ssh_credentials/hosting-portal-key hosting-portal@<IP_SERVEUR>
sudo pct list  # Doit fonctionner sans mot de passe
```
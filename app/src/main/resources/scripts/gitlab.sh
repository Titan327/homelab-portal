#!/bin/bash

set -e  # Arrêt en cas d'erreur

# Configuration locale AVANT tout
apt-get update
apt-get install -y locales curl

# Générer et configurer les locales
sed -i '/en_US.UTF-8/s/^# //g' /etc/locale.gen
locale-gen en_US.UTF-8
update-locale LANG=en_US.UTF-8 LC_ALL=en_US.UTF-8

# CRITIQUE: Exporter pour la session actuelle ET tous les sous-processus
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
export LANGUAGE=en_US.UTF-8

# Vérification
echo "Locale configurée: $(locale)"

# Installation GitLab
curl -sS https://packages.gitlab.com/install/repositories/gitlab/gitlab-ce/script.deb.sh | bash
apt-get install -y gitlab-ce

# Configuration pour LXC
cat >> /etc/gitlab/gitlab.rb <<'EOF'

# Configuration pour LXC
postgresql['shared_buffers'] = "256MB"
postgresql['shmmax'] = nil
postgresql['shmall'] = nil
package['modify_kernel_parameters'] = false

# Optimisation homelab
prometheus_monitoring['enable'] = false
gitlab_kas['enable'] = false
puma['worker_processes'] = 2
puma['max_threads'] = 4
sidekiq['max_concurrency'] = 10

EOF

# Reconfigure avec les bonnes locales
gitlab-ctl reconfigure

sleep 10
gitlab-ctl status

echo ""
echo "==========================================================================="
echo "GitLab installé dans LXC !"
echo "==========================================================================="
echo ""
echo "URL : http://$(hostname -I | awk '{print $1}')"
echo "User: root"
if [ -f /etc/gitlab/initial_root_password ]; then
    PASS=$(grep 'Password:' /etc/gitlab/initial_root_password | awk '{print $2}')
    echo "Pass: $PASS"
    # Sauvegarde du mot de passe
    echo "$PASS" > /root/gitlab_initial_pass.txt
    chmod 600 /root/gitlab_initial_pass.txt
    echo "(Mot de passe sauvegardé dans /root/gitlab_initial_pass.txt)"
else
    echo "Pass: Fichier de mot de passe non trouvé"
fi
echo ""
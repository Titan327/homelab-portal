#!/bin/bash

HOSTNAME="LXC-$CTID"

echo $HOSTNAME

sudo pct create $CTID local:vztmpl/debian-12-standard_12.12-1_amd64.tar.zst \
  --hostname $HOSTNAME \
  --password root1234 \
  --storage local-lvm \
  --rootfs local-lvm:8 \
  --memory $MEMORY \
  --cores 1 \
  --net0 name=eth0,bridge=vmbr0,ip=dhcp \
  --onboot 1 \
  --unprivileged 1 > /dev/null 2>&1

sudo pct start $CTID > /dev/null 2>&1

sudo pct exec $CTID -- apt-get update -y > /dev/null 2>&1
sudo pct exec $CTID -- apt-get upgrade -y > /dev/null 2>&1

#pct push $CTID ./ssh.sh /tmp/script.sh
#pct exec $CTID -- bash /tmp/script.sh
#pct exec $CTID -- rm /tmp/script.sh

#pct push $CTID ./gitlab.sh /tmp/script.sh
#pct exec $CTID -- bash /tmp/script.sh
#pct exec $CTID -- rm /tmp/script.sh

#pct push $CTID ./docker.sh /tmp/script.sh
#pct exec $CTID -- bash /tmp/script.sh
#pct exec $CTID -- rm /tmp/script.sh
#pct exec $CTID -- usermod -aG docker user
#pct set $CTID -features nesting=1

#pct push $CTID ./cloudflare.sh /tmp/script.sh
#pct exec $CTID -- bash /tmp/script.sh
#pct exec $CTID -- rm /tmp/script.sh

sudo pct exec 300 -- hostname -I | awk '{print $1}'

exit
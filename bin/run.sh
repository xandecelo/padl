#!/bin/bash
service slapd start

change_root_pwd() {
    echo "Changing root passwd"
    sleep 1
    local password=$(slappasswd -h {SSHA} -s admin)
    "${APP_DIR}/padl/bin/padl" admin-config "${APP_DIR}/padl/conf/padl.yaml" | sed "s|%%LDAP_ROOT_PASSWORD%%|$password|" | ldapmodify -Y EXTERNAL -H ldapi:/// -a 
}

change_root_pwd

echo "PADL LDAP is running. Press [q] and Enter to to quit."

while true; do IFS= read -d '' -n 1 ; [ "${REPLY,,}" = "q" ] && break; done

service slapd stop

echo  "PDAL is shutdown"

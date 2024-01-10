#!/bin/bash

export app="${APP_DIR}/padl/bin/padl"
export conf="${APP_DIR}/padl/conf/padl.yaml"
echo 
echo 
echo 

trap end_padl_app INT TERM ERR

end_padl_app() {
    echo "Ending processes."
    service slapd stop
    exit -1
}

echo() {
    command printf "%(%Y-%m-%d %H:%M:%S %Z)T - $@\n"
}

check_yaml() {
    echo "Checking YAML"
    $app "$conf" check-yaml
    echo "YAML check done."
}

get_os_variables() {
    echo "Getting OS Variables"
    eval $($app "$conf" get-os-variables)
    echo "OS configuration is done."
}

run_source_os_hooks() {
    echo "Running sources pre-os scripts."
    $app "$conf" source-os-script-list | cat
    $app "$conf" source-os-script-list | while read source_os_script
    do
        echo "Running script ${source_os_script}."
        pushd "${APP_DIR}/source-config"
        source ${source_os_script}
        popd
        echo "Script ${source_os_script} done".
    done
}

test_connectivity() {
    echo "Testing connectivity..."
    $app "$conf" check-connectivity
}

ldap_setup() {
    echo "Setting up ldap server."
    sleep 1
    local password=$(slappasswd -h {SSHA} -s ${LDAP_ADM_PASSWORD})
    $app "$conf" ldap-setup  | sed "s|%%LDAP_ROOT_PASSWORD%%|$password|" | cat
    $app "$conf" ldap-setup  | sed "s|%%LDAP_ROOT_PASSWORD%%|$password|" | ldapmodify -c -Y EXTERNAL -H ldapi:/// -a
    result=$?
    if [ $result != 0 ]; 
    then
        sleep 3
        echo
        read -p "Error configuring ldap. Press Enter to continue" < /dev/tty
    fi
    echo "done."
}

ldap_prepare_resources() {
    echo "Preparing sync processes and loading first batch"
    $app "$conf" prepare
}

ldap_start_sync() {
    echo "Running sync processes."
    $app "$conf" sync
}



ldap_apply_dyngroup() {
    cat << EOF | ldapmodify -Y EXTERNAL -H ldapi:/// -a
dn: cn=memberOfAux,cn=schema,cn=config
objectClass: olcSchemaConfig
cn: memberOfAux
olcAttributeTypes: ( 1.2.840.113556.1.4.222 NAME 'memberOf' DESC 'Group that the
 entry belongs to' EQUALITY distinguishedNameMatch SYNTAX 1.3.6.1.4.1.1466.115
 .121.1.12 )
olcObjectClasses: ( 1.2.840.113556.1.5.6 NAME 'memberOfAux' SUP top AUXILIARY MAY
  ( memberOf ) )

EOF
}


configure_user_ldifs() {
    echo "Applying user configuration"
    for userfile in ldif/*
    do
        echo "Adding user configuration file $userfile"
        cat "${userfile}" | ldapmodify -Y EXTERNAL -H ldapi:/// -a 
    done
}


check_base_org() {
    echo "Check if base organization is configured."
    $app "$conf" check-base-org
}

start_ldap() {
    nohup /usr/sbin/slapd -h "ldap:/// ldapi:///" -g openldap -u openldap -F /etc/ldap/slapd.d -d 7 >> /opt/app/logs/ldap.log 2>&1 &
}

stop_ldap() {
    killall slapd
}

check_yaml
tail -n 10 -F logs/padl.log &
get_os_variables
run_source_os_hooks

echo; echo; echo 
echo "Initializing services"
echo; echo; echo 
service mariadb start
mariadb -uroot < bin/init.sql

start_ldap

test_connectivity
ldap_apply_dyngroup
ldap_prepare_resources
configure_user_ldifs
ldap_setup
check_base_org

stop_ldap
start_ldap

ldap_start_sync

#echo "PADL LDAP is running. Press [q] and Enter to to quit."
#while true; do IFS= read -d '' -n 1 ; [ "${REPLY,,}" = "q" ] && break; done

echo  "PADL is shutdown"
exit 0
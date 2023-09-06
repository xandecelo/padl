#!/bin/bash

trap end_padl_app INT TERM ERR

end_padl_app() {
    echo "Ending processes."
    service slapd stop
    exit -1
}

export app="${APP_DIR}/padl/bin/padl"
export conf="${APP_DIR}/padl/conf/padl.yaml"
service slapd start

echo() {
    command printf "%(%Y-%m-%d %H:%M:%S %Z)T - $@\n"
}

check_yaml() {
    echo "Checking YAML"
    $app "$conf" check-yaml
}

get_os_variables() {
    eval $($app "$conf" get-os-variables)
}

run_source_os_hooks() {
    echo "Running sources pre-os scripts."
    for source_os_script in $($app "$conf" source-os-script-list)
    do
        echo "Running script ${source_os_script}."
        source "${APP_DIR}/source-config/${source_os_script}"
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
    $app "$conf" ldap-setup  | sed "s|%%LDAP_ROOT_PASSWORD%%|$password|" | ldapmodify -Y EXTERNAL -H ldapi:/// -a 
    echo "done."
}

ldap_start_sync() {
    echo "Running sync processes."
    $app "$conf" sync
}

check_yaml
get_os_variables
run_source_os_hooks
test_connectivity
ldap_setup
ldap_start_sync
# echo "PADL LDAP is running. Press [q] and Enter to to quit."
# while true; do IFS= read -d '' -n 1 ; [ "${REPLY,,}" = "q" ] && break; done
echo  "PADL is shutdown"
exit 0
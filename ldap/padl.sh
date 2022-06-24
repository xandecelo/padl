#!/bin/bash

CONFIG_FILE=$1
PADL_APP=app

if [ -e ${PADL_CONFIG_FILE} ]
then 
    CONFIG_FILE="${PADL_CONFIG_FILE}"
    echo "Using configuration file provided by PADL_CONFIG_FILE variable."
fi

if [ ! -e ${CONFIG_FILE} ]
then
    CONFIG_FILE="${PADLBRIDGE_HOME}/padlbridge.yaml"
    echo "No configuration file specified. "
fi

echo "Using configuration file located at ${CONFIG_FILE}."

pushd() {
    command pushd $@ > /dev/null
}

popd() {
    command popd $@ > /dev/null
}

pushd ${PADLBRIDGE_HOME}
if [ -e "config.sh" ]; 
then
    eval $(./${PADL_APP}/bin/${PADL_APP} config ${CONFIG_FILE}) ./config.sh
    rm -f config.sh
fi

./${PADL_APP}/bin/${PADL_APP} run ${CONFIG_FILE}

while true
do
    sudo service slapd status > /dev/null
    SLAP_LEVEL=$?
    if [ ${SLAP_LEVEL} -ne 0 ];
    then
        echo "OpenLDAP service is down."
        exit ${SLAP_LEVEL}
    fi
    sleep 5
done
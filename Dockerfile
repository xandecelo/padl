from ubuntu:22.04 as ldap_source_config
env SOURCE_DIR="/opt/source"
run mkdir -p ${SOURCE_DIR}
# Enable ubuntu source code to be downloaded and compiled
run echo "deb-src http://ports.ubuntu.com/ubuntu-ports/ jammy main restricted" >> /etc/apt/sources.list
run echo "deb-src http://ports.ubuntu.com/ubuntu-ports/ jammy-updates main restricted" >> /etc/apt/sources.list
run apt-get update && apt-get install -y dpkg-dev devscripts psmisc mariadb-server unixodbc odbcinst odbc-mariadb
workdir ${SOURCE_DIR}
# Compile openldap with added backends
env DEB_BUILD_OPTIONS='nostrip noopt parallel=4 nocheck' 
env FSG_NONFREE=true 
run apt-get source openldap
run dir=$(find . -type d -name 'open*') && mv $dir openldap-source 
workdir ${SOURCE_DIR}/openldap-source
run apt-get -y build-dep slapd
run echo '--enable-sql --enable-ldap ' >> debian/configure.options
run debuild --no-lintian -i -us -uc -b 

from ubuntu:22.04
expose 389
env APP_DIR="/opt/padl"
env SOURCE_DIR="/opt/source"
run echo 'APT::Keep-Downloaded-Packages "false";' > /etc/apt/apt.conf.d/00-disable-cache
run apt-get update && apt-get install -y psmisc mariadb-server unixodbc odbcinst odbc-mariadb
run apt install -y libsasl2-2 libkrb5-26-heimdal libkadm5srv8-heimdal libargon2-1
run mkdir -p "${SOURCE_DIR}"
workdir "${SOURCE_DIR}"
copy --from=ldap_source_config "${SOURCE_DIR}/*.deb" "${SOURCE_DIR}"
run DEBIAN_FRONTEND="noninteractive" dpkg -i *.deb   
run mkdir -p ${APP_DIR}
workdir ${APP_DIR}
add bin bin
entrypoint [ "/bin/bash", "-c" ]
cmd [ "source ./bin/run.sh" ]
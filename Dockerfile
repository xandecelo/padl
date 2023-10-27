FROM docker.io/library/ubuntu:22.04 as ldap_source_config
ENV SOURCE_DIR="/opt/source"
RUN mkdir -p ${SOURCE_DIR}
# Enable ubuntu source code to be downloaded and compiled
RUN echo "deb-src http://ports.ubuntu.com/ubuntu-ports/ jammy main restricted" >> /etc/apt/sources.list
RUN echo "deb-src http://ports.ubuntu.com/ubuntu-ports/ jammy-updates main restricted" >> /etc/apt/sources.list
RUN apt-get update && apt-get install -y dpkg-dev devscripts psmisc mariadb-server unixodbc odbcinst odbc-mariadb
WORKDIR ${SOURCE_DIR}
# Compile openldap with added backends
ENV DEB_BUILD_OPTIONS='nostrip noopt parallel=4 nocheck' 
ENV FSG_NONFREE=true 
RUN apt-get source openldap
RUN dir=$(find . -type d -name 'open*') && mv $dir openldap-source 
WORKDIR ${SOURCE_DIR}/openldap-source
RUN apt-get -y build-dep slapd
RUN echo '--enable-sql --enable-ldap ' >> debian/configure.options
RUN debuild --no-lintian -i -us -uc -b 


FROM docker.io/library/gradle:jdk17-focal as build 
ENV SOURCE_DIR="/opt/source"
ADD core "$SOURCE_DIR"/core
WORKDIR "$SOURCE_DIR"/core
RUN gradle distTar --no-daemon

FROM docker.io/library/eclipse-temurin:17-jre-jammy
ENV APP_DIR="/opt/app"
ENV SOURCE_DIR="/opt/source"
RUN echo 'APT::Keep-Downloaded-Packages "false";' > /etc/apt/apt.conf.d/00-disable-cache
RUN apt-get update && apt-get install -y psmisc mariadb-server unixodbc odbcinst odbc-mariadb
RUN apt install -y libsasl2-2 libkrb5-26-heimdal libkadm5srv8-heimdal libargon2-1
RUN mkdir -p "${SOURCE_DIR}"
WORKDIR "${SOURCE_DIR}"
COPY --from=ldap_source_config "${SOURCE_DIR}/*.deb" "${SOURCE_DIR}/"
RUN DEBIAN_FRONTEND="noninteractive" dpkg -i *.deb   
RUN mkdir -p ${APP_DIR}
WORKDIR ${APP_DIR}
COPY --from=build "$SOURCE_DIR"/core/build/distributions/padl.tar ${APP_DIR}
RUN tar xvf "${APP_DIR}"/padl.tar && rm padl.tar
ADD bin bin
ADD conf padl/conf
ADD source-config source-config
ADD ldif ldif
ENV LDAP_ADM_PASSWORD="changeme"
ENV DEV_MODE=true
EXPOSE 389
EXPOSE 3306
EXPOSE 3308

ENTRYPOINT [ "/bin/bash", "-c" ]
CMD [ "source ./bin/run.sh" ]
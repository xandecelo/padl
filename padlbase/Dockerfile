FROM ubuntu:22.04
EXPOSE 389
ENV PADLBRIDGE_HOME=/opt/padlbridge

# Image variables
ENV DEBIAN_FRONTEND=noninteractive 

# Superuser tasks
RUN apt-get update && apt -y upgrade
RUN apt-get install -y slapd
## Grant some ldap super user actions to app user (least privilege)
RUN apt-get install -y sudo && echo "padl   ALL = NOPASSWD: /usr/sbin/service slapd *, /usr/bin/ldapmodify *" > /etc/sudoers.d/padl-sudoers

# Build requirements 
RUN apt install -y openjdk-11-jdk-headless

# Configuration
RUN groupadd padl; useradd -s /bin/bash -m -g padl -G users padl; mkdir -p ${PADLBRIDGE_HOME} ${PADLBRIDGE_HOME}/source



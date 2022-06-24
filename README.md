# PADL Bridge
(An \[Open\]LDAP bridge)

Makes easier to reach through gap between LDAP and external input datasources.


# Configuration

- Run the container indicating the variable and the file location inside the container
```bash
docker run -it --rm -p 10289:389 -e PADL_CONFIG_FILE=/opt/padlbridge.yaml alefzero/padl
```
- Extend the dockerfile using an enviroment variable PADL_CONFIG_FILE:
```docker
FROM alefzero/padl
PADL_CONFIG_FILE=/opt/padlbridge.yaml
```
- UExtend the dockefile using an explicit run file configuration:
```docker
FROM alefzero/padl
CMD [ "padl.sh run /opt/padlbridge.yaml" ]

# or

FROM alefzero/padl
ENTRYPOINT [ "padl.sh", "run", "/opt/padlbridge.yaml" ]

```
The environment variable has precedence if you declare both enviroment variable and explicit run file configuration.


# Building with docker

```bash

# Build
docker build -t alefzero/padl .

# Run (example)
docker run -it --rm -p 10389:389 --name padl alefzero/padl

```


# Overview

PADL Bridge app consists of basics parts:
- An LDAP configurator: used for "administrative" setup.
- LDAP Sources: used to provide data
- LDAP Target: used to load the data into the target LDAP.

This implementation uses OpenLDAP in a container described through Docker syntax for LDAP services, but the interfaces can be extended for any LDAP compliant service.


## LDAP Sources

processor:  structural ou database
type: one of X.500 attributes

      String  X.500 AttributeType
      ------  --------------------------------------------
      CN      commonName (2.5.4.3)
      L       localityName (2.5.4.7)
      ST      stateOrProvinceName (2.5.4.8)
      O       organizationName (2.5.4.10)
      OU      organizationalUnitName (2.5.4.11)
      C       countryName (2.5.4.6)
      STREET  streetAddress (2.5.4.9)
      DC      domainComponent (0.9.2342.19200300.100.1.25)
      UID     userId (0.9.2342.19200300.100.1.1)



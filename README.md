# PADL Bridge
(An \[Open\]LDAP bridge)

Makes easier to reach through gap between LDAP and external input datasources.


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



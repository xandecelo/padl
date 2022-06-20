#!/bin/bash
# https://openldap.org/doc/admin25/slapdconf2.html

sudo service slapd start

PASS=$(slappasswd -h {SSHA} -s ${PADLBRIDGE_ROOT_CONFIG_PASSWORD})

cat << EOF | sudo ldapmodify -Y EXTERNAL -H ldapi:/// -a 
# Change cn=admin,cn=config password
dn: olcDatabase={0}config,cn=config
changetype: modify
replace: olcRootPW
olcRootPW: $PASS    
EOF

PASS=$(slappasswd -h {SSHA} -s ${PADLBRIDGE_ADMIN_PASSWORD})

cat << EOF | sudo ldapmodify -Y EXTERNAL -H ldapi:/// -a 
dn: olcDatabase={1}mdb,cn=config
changeType: modify
replace: olcSuffix
olcSuffix: $PADLBRIDGE_ROOT_DN

dn: olcDatabase={1}mdb,cn=config
changetype: modify
replace: olcRootDN
olcRootDN: cn=admin,${PADLBRIDGE_ROOT_DN}

dn: olcDatabase={1}mdb,cn=config
changetype: modify
replace: olcRootPW
olcRootPW: $PASS

EOF



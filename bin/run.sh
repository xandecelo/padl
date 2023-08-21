#!/bin/bash

service slapd start

echo "PADL LDAP is running. Press Control+C to stop."

read -r -d '' _ </dev/tty

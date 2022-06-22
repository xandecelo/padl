# TODO 

## Improvements:

- IMP: YAML configuration by type should be easily extendable (remove Jackson annotations in PadlSourceConfig)
- IMP: PadlSourceConfig subclasses type field handling.
- IMP: filter can be parametrized in YAML at LdapSource
- IMP: change dn target mapping

## Known issues:

- BUG: Running config over an already configured target includes an empty schema

package org.alefzero.padl.sources;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.alefzero.padl.core.exceptions.PadlException;
import org.alefzero.padl.core.model.DatabaseSourceConfig;
import org.alefzero.padl.core.model.PadlSourceConfig;
import org.alefzero.padl.core.services.PadlSource;
import org.alefzero.padl.core.services.PadlTarget;
import org.alefzero.padl.sources.db.DBMetadataModel;
import org.alefzero.padl.utils.LdapUtils;
import org.alefzero.padl.utils.PadlUtils;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.ObjectClassTypeEnum;
import org.apache.directory.api.ldap.model.schema.UsageEnum;
import org.apache.directory.api.ldap.model.schema.registries.Schema;
import org.apache.directory.api.ldap.model.schema.registries.SchemaLoader;
import org.apache.directory.ldap.client.api.DefaultSchemaLoader;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Database source is used to import data from any SLQ data structure and
 * convert to a ldap format.
 */
public class DatabaseSource extends PadlSource {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String ID = "database";

    // private static final String IA5_SYNTAX_OID = "1.3.6.1.4.1.1466.115.121.1.26";
    private static final String IA5_SYNTAX_OID = "1.3.6.1.4.1.1466.115.121.1.15";
    private PadlTarget target;
    private DatabaseSourceConfig config;
    private Map<String, String> columnNames = null;

    private String customObjClass;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Entry> getConfigurationEntries() {
        List<Entry> attributesToConfigure = new LinkedList<Entry>();

        // https://nightlies.apache.org/directory/api/2.1.0/apidocs/
        try {

            LdapNetworkConnection conn = target.getConnection();
            List<String> ldapAttributesToConfigure = new LinkedList<String>(columnNames.values());
            removeConfiguredAttributes(conn, ldapAttributesToConfigure);
            if (ldapAttributesToConfigure.size() > 0) {
                Entry entry = new DefaultEntry(String.format("cn=%s,cn=schema,cn=config",
                        config.getId()));
                entry.add("objectClass", "olcSchemaConfig");
                entry.add("cn", config.getId());

                customObjClass = config.getId() + "PadlClass";

                ObjectClass objClass = new ObjectClass(target.getNextOID());
                objClass.setDescription("PADL generated category for " + config.getId());
                objClass.setNames(customObjClass);
                objClass.setEnabled(true);
                objClass.setType(ObjectClassTypeEnum.AUXILIARY);
                objClass.setSuperiorOids(Arrays.asList(new String[] { "top" }));

                for (String attribute : ldapAttributesToConfigure) {
                    AttributeType at = new AttributeType(target.getNextOID());

                    at.setDescription("PADL generated attribute for " + attribute);
                    at.setNames(attribute);
                    at.setUsage(UsageEnum.USER_APPLICATIONS);
                    at.setEquality(new MatchingRule("caseIgnoreMatch"));
                    at.setSubstring(new MatchingRule("caseIgnoreSubstringsMatch"));

                    // IMPROVEMENT: create non-single-valued attributes.
                    at.setSingleValued(true);

                    // Syntax defaults to 1.3.6.1.4.1.1466.115.121.1.26 - IA5 String syntax
                    at.setSyntax(new LdapSyntax(IA5_SYNTAX_OID));

                    // IMPROVEMENT: Attribute creation should be done without raw code.
                    String data = at.toString().replaceFirst("attributetype", "").replaceAll("\n", " ");
                    entry.add("olcAttributeTypes", data);
                    attributesToConfigure.add(entry);
                    objClass.addMayAttributeTypeOids(at.getOid());
                }
                entry.add("olcObjectClasses",
                        objClass.toString().replaceFirst("objectclass", "").replaceAll("\n", " ").replaceAll("\t",
                                " "));
            }

        } catch (LdapException | IOException e) {
            logger.error("Cannot configure target attributes.", e);
        }
        return attributesToConfigure;
    }

    private void removeConfiguredAttributes(LdapNetworkConnection conn, Collection<String> ldapAttributes)
            throws LdapException, IOException {
        SchemaLoader loader = new DefaultSchemaLoader(conn);
        for (Schema schema : loader.getAllEnabled()) {
            loader.loadAttributeTypes(schema).forEach(entry -> {
                ldapAttributes.remove(entry.get("m-name").get().getString());
            });
        }
    }

    @Override
    public void entangle(PadlSourceConfig sourceConfiguration, PadlTarget targetService) {
        if (!(sourceConfiguration instanceof DatabaseSourceConfig)) {
            logger.error("Configuration for {} type is invalid.", sourceConfiguration);
            throw new IllegalArgumentException("Configuration type is invalid for this source service.");
        }
        if (!targetService.isReady()) {
            logger.error("Target [{}] type is not ready.", targetService);
            throw new IllegalArgumentException("Check your configuration and prepare your target correctly.");
        }
        this.config = (DatabaseSourceConfig) sourceConfiguration;
        this.target = targetService;
        this.columnNames = PadlUtils.split(config.getDatamap(), true);

    }

    @Override
    public PadlTarget getTarget() {
        return this.target;
    }

    @Override
    public PadlSourceConfig getConfig() throws NullPointerException {
        if (config == null) {
            throw new NullPointerException("Service is not yet configured with setup method.");
        }
        return config;
    }

    @Override
    protected void loadToTarget() throws PadlException {
        logger.debug("Starting SQL processing [{}]", config.getQuery());
        try (Connection conn = DriverManager.getConnection(config.getJdbcUrl(), config.getDbUsername(),
                config.getDbPassword())) {
            PreparedStatement ps = conn.prepareStatement(config.getQuery());
            ResultSet rs = ps.executeQuery();
            LinkedList<DBMetadataModel> collumns = this.getColumnMeta(rs);

            // separate uid collumn from the "others" collums of this resultset.
            // uid collumn is necessary for group processing (as intended for memberOf or
            // uniqueMember)
            DBMetadataModel uidCol = findUidCol(collumns);
            collumns.remove(uidCol);
            Set<String> objClassesToAdd = new TreeSet<String>(config.getObjectClasses());
            if (customObjClass != null) {
                objClassesToAdd.add(customObjClass);
            }

            try {
                while (rs.next()) {
                    logger.debug("Data found: {}", rs.getString(1));
                    String uidValue = rs.getString(uidCol.getColumnName());
                    List<Attribute> attributeList = new LinkedList<Attribute>();

                    for (DBMetadataModel col : collumns) {

                        if (rs.getObject(col.getColumnName()) == null) {
                            logger.error("Found a null value for a specified collumn ({}). Ignoring attribute...",
                                    col.getColumnName());
                            continue;
                        }

                        Attribute attribute = null;
                        String ldapColName = getLDAPAttributeName(col.getColumnName());
                        if (ldapColName != null) {
                            switch (col.getColumnType()) {
                                case Types.DATE:
                                case Types.TIME:
                                case Types.TIMESTAMP:
                                case Types.TIMESTAMP_WITH_TIMEZONE:
                                case Types.TIME_WITH_TIMEZONE:
                                    attribute = new DefaultAttribute(ldapColName,
                                            fmtTime(rs.getTimestamp(col.getColumnName())));
                                    break;

                                case Types.DOUBLE:
                                case Types.FLOAT:
                                    attribute = new DefaultAttribute(ldapColName,
                                            Double.toString(rs.getDouble(col.getColumnName())));
                                    break;
                                case Types.NUMERIC:
                                    if (col.getColumnScale() > 0) {
                                        attribute = new DefaultAttribute(ldapColName,
                                                Double.toString(rs.getDouble(col.getColumnName())));
                                        break;
                                    }
                                case Types.INTEGER:
                                case Types.SMALLINT:
                                    attribute = new DefaultAttribute(ldapColName,
                                            Integer.toString(rs.getInt(col.getColumnName())));

                                case Types.VARCHAR:
                                case Types.CHAR:
                                case Types.LONGNVARCHAR:
                                    attribute = new DefaultAttribute(ldapColName,
                                            rs.getString(col.getColumnName()));
                                    break;
                                default:
                                    attribute = new DefaultAttribute(ldapColName,
                                            rs.getString(col.getColumnName()));
                            }
                            attributeList.add(attribute);
                        }
                    }
                    Entry entry = LdapUtils.createEntry(config.getDn(), config.getLdapType(), uidValue,
                            objClassesToAdd, attributeList);
                    target.addEntry(entry, true);
                }
            } catch (LdapException e) {
                logger.error("Error processing database source", e);
                e.printStackTrace();
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("Error processing datasource:", e);
            throw new PadlException(e);
        }
    }

    private DBMetadataModel findUidCol(LinkedList<DBMetadataModel> collumns) throws PadlException {
        DBMetadataModel uidCol = null;
        for (DBMetadataModel col : collumns) {
            if (config.getUid().equalsIgnoreCase(col.getColumnName())) {
                uidCol = col;
                break;
            }
        }
        if (uidCol == null) {
            logger.error(
                    "Ignoring data from datasource {}. Uid specification not found. Check your configuration.",
                    config.getId());
            throw new PadlException("Uid specification not found");
        }
        return uidCol;
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssX");

    private String fmtTime(Date date) {
        return sdf.format(date);
    }

    private String getLDAPAttributeName(String databaseCollumnName) {
        logger.trace("Searching if database column {} has a ldap mapped match", databaseCollumnName);
        return columnNames.get(databaseCollumnName);
    }

    private LinkedList<DBMetadataModel> getColumnMeta(ResultSet rs) throws SQLException {
        logger.info("Reading metadata from database...");
        LinkedList<DBMetadataModel> collumns = new LinkedList<DBMetadataModel>();
        var meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            collumns.add(new DBMetadataModel(i, meta.getColumnName(i), meta.getColumnLabel(i),
                    meta.getColumnType(i), meta.getScale(i)));
        }
        return collumns;
    }
}

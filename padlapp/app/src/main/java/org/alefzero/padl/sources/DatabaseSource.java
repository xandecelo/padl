package org.alefzero.padl.sources;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Database source is used to import data from any SLQ data structure and
 * convert to a ldap format.
 */
public class DatabaseSource extends PadlSource {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String ID = "database";
    private PadlTarget target;
    private DatabaseSourceConfig config;
    private Map<String, String> columnNames = null;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Entry> getConfigurationEntries() {
        return new LinkedList<Entry>();
    }

    @Override
    public void entangle(PadlSourceConfig sourceConfiguration, PadlTarget targetService) {
        if (!(sourceConfiguration instanceof DatabaseSourceConfig)) {
            logger.error("Configuration for {} type is invalid.", sourceConfiguration);
            throw new IllegalArgumentException("Configuration type is invalid for this source service.");
        }
        if (!target.isReady()) {
            logger.error("Target [{}] type is not ready.", target);
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
            // uid collumn is necessary for group processing (as intended for memberOf or uniqueMember)
            DBMetadataModel uidCol = findUidCol(collumns);
            collumns.remove(uidCol);

            try {
                while (rs.next()) {

                    String uidValue = rs.getString(uidCol.getColumnName());
                    List<Attribute> attributeList = new LinkedList<Attribute>();
                    for (DBMetadataModel col : collumns) {
                        logger.debug("Getting database data {} - > {}})", col.getColumnName(),
                                rs.getString(col.getColumnName()));
                        Attribute attribute = null;
                        switch (col.getColumnType()) {
                            case Types.DATE:
                            case Types.TIME:
                            case Types.TIMESTAMP:
                            case Types.TIMESTAMP_WITH_TIMEZONE:
                            case Types.TIME_WITH_TIMEZONE:
                                attribute = new DefaultAttribute(getLDAPAttributeName(col.getColumnName()),
                                        fmtTime(rs.getTimestamp(col.getColumnName())));
                                break;

                            case Types.DOUBLE:
                            case Types.FLOAT:
                                attribute = new DefaultAttribute(getLDAPAttributeName(col.getColumnName()),
                                        Double.toString(rs.getDouble(col.getColumnName())));
                                break;
                            case Types.NUMERIC:
                                if (col.getColumnScale() > 0) {
                                    attribute = new DefaultAttribute(getLDAPAttributeName(col.getColumnName()),
                                            Double.toString(rs.getDouble(col.getColumnName())));
                                    break;
                                }
                            case Types.INTEGER:
                            case Types.SMALLINT:
                                attribute = new DefaultAttribute(getLDAPAttributeName(col.getColumnName()),
                                        Integer.toString(rs.getInt(col.getColumnName())));

                            case Types.VARCHAR:
                            case Types.CHAR:
                            case Types.LONGNVARCHAR:
                                attribute = new DefaultAttribute(getLDAPAttributeName(col.getColumnName()),
                                        rs.getString(col.getColumnName()));
                                break;
                            default:
                                attribute = new DefaultAttribute(getLDAPAttributeName(col.getColumnName()),
                                        rs.getString(col.getColumnName()));
                        }
                        attributeList.add(attribute);
                    }
                    Entry entry = LdapUtils.createEntry(config.getDn(), config.getLdapType(), uidValue,
                            config.getObjectClasses(), attributeList);
                    target.addEntry(entry);
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

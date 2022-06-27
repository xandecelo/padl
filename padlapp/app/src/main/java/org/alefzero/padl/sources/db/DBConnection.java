package org.alefzero.padl.sources.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alefzero.padl.core.model.DatabaseSourceConfig;
import org.alefzero.padl.utils.PadlUtils;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConnection extends BaseDBConnection {

    Logger logger = LoggerFactory.getLogger(DBConnection.class);

    private Map<String, String> columnNames = null;
    private DatabaseSourceConfig config;

    public DBConnection(DatabaseSourceConfig config) {
        super(config.getJdbcUrl(), config.getDbUsername(), config.getDbPassword());
        this.config = config;
        this.columnNames = PadlUtils.split(config.getDatamap(), true);
    }

    private LinkedList<DBMetadataModel> getColumnMeta(ResultSet rs) throws SQLException {
        LinkedList<DBMetadataModel> collumns = new LinkedList<DBMetadataModel>();
        var meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            collumns.add(new DBMetadataModel(i, meta.getColumnName(i), meta.getColumnLabel(i),
                    meta.getColumnType(i), meta.getScale(i)));
        }
        return collumns;
    }

    public void process(DataInserter dataInserter) throws SQLException {
        logger.debug("Starting SQL processing [{}]", config.getQuery());
        try (Connection conn = super.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(config.getQuery());
            ResultSet rs = ps.executeQuery();
            var collumns = getColumnMeta(rs);
            try {
                while (rs.next()) {
                    List<Attribute> attributeList = new LinkedList<Attribute>();
                    for (var col : collumns) {
                        logger.debug("Getting database data {} - > {}})", col.getColumnName(),
                                rs.getString(col.getColumnName()));

                        if (config.getUid().equalsIgnoreCase(col.getColumnName())) {
                            config.setName(rs.getString(col.getColumnName()));
                        }

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
                    dataInserter.process(config, attributeList);
                }
            } catch (LdapException e) {
                logger.error("Error processing database source", e);
                e.printStackTrace();
            }
            rs.close();
        }
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssX");

    public String fmtTime(Date date) {
        return sdf.format(date);
    }

    public String getLDAPAttributeName(String databaseCollumnName) {
        return columnNames.get(databaseCollumnName);
    }

}

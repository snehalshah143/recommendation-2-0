package tech.algofinserve.recommendation.common.datastore;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;
import org.hibernate.dialect.identity.IdentityColumnSupport;

import java.sql.Types;

public class SQLiteDialect extends Dialect {

    public SQLiteDialect() {
        super();
        registerColumnType(Types.INTEGER, "integer");
        registerColumnType(Types.VARCHAR, "text");
        registerColumnType(Types.DOUBLE, "double");
        registerColumnType(Types.FLOAT, "float");
        registerColumnType(Types.BOOLEAN, "boolean");
        registerColumnType(Types.DATE, "date");
        registerColumnType(Types.TIMESTAMP, "datetime");
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return new SQLiteIdentityColumnSupport();
    }
    @Override
    public boolean dropConstraints() {
        // SQLite doesn’t support dropping constraints
        return false;
    }

    @Override
    public String getAddColumnString() {
        // ✅ Allow Hibernate to generate "alter table ... add column ..."
        return "add column";
    }
    private static class SQLiteIdentityColumnSupport extends IdentityColumnSupportImpl {
        @Override
        public boolean supportsIdentityColumns() {
            return true;
        }

        @Override
        public String getIdentityColumnString(int type) throws MappingException {
            // AUTOINCREMENT for SQLite
            return "integer primary key autoincrement";
        }

        @Override
        public String getIdentitySelectString(String table, String column, int type) throws MappingException {
            return "select last_insert_rowid()";
        }
    }
}
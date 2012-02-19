/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core;

import org.restsql.core.impl.SqlResourceMetaDataMySql;
import org.restsql.core.impl.SqlResourceMetaDataPostgreSql;


public class BaseTestCase {
	static {
		if (System.getProperty(Config.KEY_RESTSQL_PROPERTIES) == null) {
			System.setProperty(Config.KEY_RESTSQL_PROPERTIES, "/resources/properties/restsql-mysql.properties");
		}
	}

	protected String getQualifiedTableName(String tableName) {
		switch (getDatabaseType()) {
			case MySQL:
				return "sakila." + tableName;
			case PostgreSql:
				return "sakila.public." + tableName;
			default:
				return "unknown";
		}
	}
	
	protected DatabaseType getDatabaseType() {
		String metaDataClass = Config.properties.getProperty(Config.KEY_SQL_RESOURCE_METADATA,
				Config.DEFAULT_SQL_RESOURCE_METADATA);
		if (metaDataClass.equals(SqlResourceMetaDataMySql.class.getName())) {
			return DatabaseType.MySQL;
		} else 	if (metaDataClass.equals(SqlResourceMetaDataPostgreSql.class.getName())) {
			return DatabaseType.PostgreSql;
		} else {
			throw new RuntimeException("metaDataClass " + metaDataClass + " unrecognized");
		}
	}
	
	public static enum DatabaseType {
		MySQL, PostgreSql;
	}
}

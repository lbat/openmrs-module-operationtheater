package org.openmrs.module.operationtheater;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.Destination;
import com.ninja_squad.dbsetup.generator.ValueGenerator;
import com.ninja_squad.dbsetup.generator.ValueGenerators;
import com.ninja_squad.dbsetup.operation.Insert;
import com.ninja_squad.dbsetup.operation.Operation;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static com.ninja_squad.dbsetup.Operations.sql;

/**
 * Utility class to simplify database setup with the library DBSetup
 * It is an alternative to DBUnit
 */
public class DbUtil {

	/**
	 * creates Insert.Builder object for the given table - method that does the heavy lifting
	 * creates ValueGenerators for all basic fields (uuid, creator, date_created and retired or voided)
	 * see params for a more detailed description
	 *
	 * @param tableName                 determines the table for which the Insert.Builder object is created
	 * @param pkField                   determines the name of the primary key field
	 * @param pkStartValue              determines that starting value of the primary key field
	 * @param metadata                  if true create ValueGenerator for field "retired" else "voided"
	 * @param additionalValueGenerators creates additional ValueGenerators that are specified in this param
	 * @param noValueGeneratorFor       excludes ValueGenerators for fields that are specified in this List
	 * @return
	 */
	private static Insert.Builder insertInto(String tableName, String pkField, int pkStartValue, boolean metadata,
	                                         Map<String, ValueGenerator<?>> additionalValueGenerators,
	                                         List<String> noValueGeneratorFor) {
		Map<String, ValueGenerator<?>> fieldGeneratorMap = new HashMap<String, ValueGenerator<?>>();
		fieldGeneratorMap.put("uuid", ValueGenerators.stringSequence(tableName));
		fieldGeneratorMap.put("creator", ValueGenerators.constant(1));
		fieldGeneratorMap.put("date_created", ValueGenerators.dateSequence());
		fieldGeneratorMap.put(pkField, ValueGenerators.sequence().startingAt(pkStartValue));
		if (metadata) {
			fieldGeneratorMap.put("retired", ValueGenerators.constant(false));
		} else {
			fieldGeneratorMap.put("voided", ValueGenerators.constant(false));
		}
		//add additional value generators
		for (String field : additionalValueGenerators.keySet()) {
			fieldGeneratorMap.put(field, additionalValueGenerators.get(field));
		}

		//check if all elements in noValueGeneratorFor are contained in fieldGeneratorMap
		Set<String> generatedFields = fieldGeneratorMap.keySet();
		for (String field : noValueGeneratorFor) {
			if (!generatedFields.contains(field)) {
				throw new IllegalArgumentException("Field (" + field
						+ ") specified in parameter noValueGeneratorFor has no defined value generator - table: "
						+ tableName + " - valid fields are: " + StringUtils.join(generatedFields, ", "));
			}
		}

		Insert.Builder builder = com.ninja_squad.dbsetup.Operations.insertInto(tableName);
		for (String field : fieldGeneratorMap.keySet()) {
			if (noValueGeneratorFor == null || !noValueGeneratorFor.contains(field)) {
				builder = builder.withGeneratedValue(field, fieldGeneratorMap.get(field));
			}
		}
		return builder;
	}

	/**
	 * creates an Insert.Builder object for the given table configuration and an optional list of fields
	 * for which no ValueGenerators should be defined
	 *
	 * @param config
	 * @param noValueGeneratorFor excludes ValueGenerators for fields that are specified in this List
	 * @return
	 */
	public static Insert.Builder insertInto(Config config, String... noValueGeneratorFor) {
		List<String> noValGenForFields = new ArrayList<String>(Arrays.asList(noValueGeneratorFor));
		noValGenForFields.addAll(config.noValueGeneratorsFor);
		return insertInto(config.tableName, config.pkField, config.pkStartValue, config.metadata,
				config.additionalValGenerators,
				noValGenForFields);
	}

	/**
	 * method that is used to create an Insert.Builder object if there exists no table configuration
	 *
	 * @param tableName           determines the table for which the Insert.Builder object is created
	 * @param pkField             determines the name of the primary key field
	 * @param pkStartValue        determines that starting value of the primary key field
	 * @param metadata            if true create ValueGenerator for field "retired" else "voided"
	 * @param noValueGeneratorFor excludes ValueGenerators for fields that are specified in this List
	 * @return
	 */
	public static Insert.Builder insertInto(String tableName, String pkField, int pkStartValue, boolean metadata,
	                                        String... noValueGeneratorFor) {
		List<String> noValGenForFields = Arrays.asList(noValueGeneratorFor);
		return insertInto(tableName, pkField, pkStartValue, metadata, null, noValGenForFields);
	}

	/**
	 * creates and returns a DBSetup object that is used to execute the inserts
	 * turns off foreign key checks before executing inserts and
	 * turns them back on afterwards
	 *
	 * @param inserts
	 * @param connection
	 * @return
	 */
	public static DbSetup buildDBSetup(Operation inserts, Connection connection, boolean inMemoryDatabase) {

		String sql_begin;
		String sql_end;
		if (inMemoryDatabase) {
			sql_begin = "SET REFERENTIAL_INTEGRITY FALSE";
			sql_end = "SET REFERENTIAL_INTEGRITY TRUE";
		} else {
			sql_begin = "SET FOREIGN_KEY_CHECKS=0;";
			sql_end = "SET FOREIGN_KEY_CHECKS=1;";
		}

		Operation operation = sequenceOf(
				sql(sql_begin),
				inserts,
				sql(sql_end)
		);

		return new DbSetup(new DestinationAdapter(connection), operation);
	}

	/**
	 * Convenience class that stores the default configuration for tables
	 */
	public enum Config {
		LOCATION("location", "location_id", 100, true, null),
		LOCATION_TAG("location_tag", "location_tag_id", 100, true, null),
		PERSON("person", "person_id", 100, false, getPersonValueGenerators()),
		PATIENT("patient", "patient_id", 100, false, null, "uuid"),
		PROCEDURE("prozedure", "procedure_id", 1, true, null),
		SCHEDULING_DATA("scheduling_data", "scheduling_data_id", 1, false, null),
		SURGERY("surgery", "surgery_id", 1, false, null),
		LOCATION_ATTRIBUTE_TYPE("location_attribute_type", "location_attribute_type_id", 100, true,
				getLocationAttributeTypeValueGenerators()),
		LOCATION_ATTRIBUTE("location_attribute", "location_attribute_id", 100, false, null);

		private String tableName;

		/**
		 * field name of the private key
		 */
		private String pkField;

		/**
		 * private key field start value
		 */
		private int pkStartValue;

		/**
		 * based on the value of this variable a ValueGenerator for "retired" (metadata=true) or
		 * "voided" (metadata=false) is created
		 */
		private boolean metadata;

		/**
		 * list of basic fields who shouldn't have a ValueGenerator defined
		 * possible values are: (uuid, creator, date_created and retired or voided)
		 */
		private List<String> noValueGeneratorsFor;

		/**
		 * map with additional ValueGenerators that are relevant for a particular table
		 */
		private Map<String, ValueGenerator<?>> additionalValGenerators;

		private Config(String tableName, String pkField, int pkStartValue, boolean metadata,
		               Map<String, ValueGenerator<?>> additionalValGenerators, String... noValueGeneratorsFor) {
			this.tableName = tableName;
			this.pkField = pkField;
			this.pkStartValue = pkStartValue;
			this.metadata = metadata;
			this.noValueGeneratorsFor = Arrays.asList(noValueGeneratorsFor);
			this.additionalValGenerators = additionalValGenerators;
			if (this.additionalValGenerators == null) {
				this.additionalValGenerators = new HashMap<String, ValueGenerator<?>>();
			}
		}

		/**
		 * method that returns additional ValueGenerators that are relevant for the table PERSON
		 *
		 * @return
		 */
		private static Map<String, ValueGenerator<?>> getPersonValueGenerators() {
			Map<String, ValueGenerator<?>> valueGeneratorMap = new HashMap<String, ValueGenerator<?>>();
			valueGeneratorMap.put("dead", ValueGenerators.constant(false));
			return valueGeneratorMap;
		}

		/**
		 * method that returns additional ValueGenerators that are relevant for the table LOCATION_ATTRIBUTE_TYPE
		 *
		 * @return
		 */
		private static Map<String, ValueGenerator<?>> getLocationAttributeTypeValueGenerators() {
			Map<String, ValueGenerator<?>> valueGeneratorMap = new HashMap<String, ValueGenerator<?>>();
			valueGeneratorMap.put("min_occurs", ValueGenerators.constant(0));
			return valueGeneratorMap;
		}

		public Config setPkStartValue(int pkStartValue) {
			this.pkStartValue = pkStartValue;
			return this;
		}
	}

	/**
	 * helper class that is used to wrap the {@link java.sql.Connection} object inside
	 * {@link com.ninja_squad.dbsetup.destination.Destination}
	 */
	private static class DestinationAdapter implements Destination {

		private Connection connection;

		DestinationAdapter(Connection connection) {
			this.connection = connection;
		}

		@Override
		public Connection getConnection() throws SQLException {
			return connection;
		}
	}
}

package org.openmrs.module.operationtheater;

import com.ninja_squad.dbsetup.operation.Operation;

import static com.ninja_squad.dbsetup.Operations.insertInto;
import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static org.openmrs.module.operationtheater.DbUtil.Config;

/**
 * Default inserts that are always needed for tests in this module
 */
public class DbUtilDefaultInserts {

	public static Operation get() {
		return sequenceOf(
				DbUtil.insertInto(Config.LOCATION).columns("name").values("OT 1").values("OT 2").build(),
				DbUtil.insertInto(Config.LOCATION_TAG, "uuid")
						.columns("name", "uuid")
						.values("Operation Theater", OTMetadata.LOCATION_TAG_OPERATION_THEATER_UUID).build(),
				insertInto("location_tag_map")
						.columns("location_tag_id", "location_id")
						.values(100, 100)
						.values(100, 101)
						.build(),
				DbUtil.insertInto(Config.LOCATION_ATTRIBUTE_TYPE, "uuid")
						.columns("name", "datatype", "datatype_config", "max_occurs", "uuid")
						.values("default available time begin",
								"org.openmrs.customdatatype.datatype.RegexValidatedTextDatatype", "[0-9]{2}:[0-9]{2}", 1,
								"4e051aeb-a19d-49e0-820f-51ae591ec41f")
						.values("default available time end",
								"org.openmrs.customdatatype.datatype.RegexValidatedTextDatatype", "[0-9]{2}:[0-9]{2}", 1,
								"a9d9ec55-e992-4d04-aebe-808be50aa87a")
						.build(),
				DbUtil.insertInto(Config.LOCATION_ATTRIBUTE)
						.columns("location_id", "attribute_type_id", "value_reference")
						.values(100, 100, "08:00")
						.values(101, 100, "08:00")
						.values(100, 101, "17:00")
						.values(101, 101, "17:00")
						.build(),
				DbUtil.insertInto(Config.PERSON).columns().values().values().build(),
				DbUtil.insertInto(Config.PATIENT).columns().values().values().build()
		);
	}
}

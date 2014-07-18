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
				DbUtil.insertInto(Config.PERSON).columns().values().values().build(),
				DbUtil.insertInto(Config.PATIENT).columns().values().values().build()
		);
	}
}

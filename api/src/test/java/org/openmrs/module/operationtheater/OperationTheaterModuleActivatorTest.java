package org.openmrs.module.operationtheater;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.idgen.SequentialIdentifierGenerator;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.idgen.validator.LuhnMod30IdentifierValidator;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Tests {@link OperationTheaterModuleActivatorTest}
 */
//@SkipBaseSetup
public class OperationTheaterModuleActivatorTest extends BaseModuleContextSensitiveTest {

	/**
	 * @verifies create emergency procedure and patient placeholder
	 * @see OperationTheaterModuleActivator#started()
	 */
	@Test
	public void started_shouldCreateEmergencyProcedureAndPatientPlaceholder() throws Exception {
		//prepare
		OperationTheaterService otService = Context.getService(OperationTheaterService.class);
		PatientService patientService = Context.getPatientService();

		PatientIdentifierType idType = new PatientIdentifierType();
		idType.setName(OTMetadata.OPENMRS_ID_NAME);
		idType.setDescription("description");
		idType.setValidator("org.openmrs.module.idgen.validator.LuhnMod30IdentifierValidator");
		idType.setCheckDigit(true);
		idType.setLocationBehavior(PatientIdentifierType.LocationBehavior.NOT_USED);
		patientService.savePatientIdentifierType(idType);

		IdentifierSourceService idService = initMockGenerator(patientService);
		OperationTheaterModuleActivator activator = new OperationTheaterModuleActivator();
		Whitebox.setInternalState(activator, "idService", idService);

		//call method under test
		activator.started();

		//verify
		Procedure procedure = otService.getProcedureByUuid(OTMetadata.PLACEHOLDER_PROCEDURE_UUID);
		assertThat(procedure, is(notNullValue()));
		assertThat(procedure.getName(), is("EMERGENCY Placeholder"));
		assertThat(procedure.getDescription(), is("This procedure is used as placeholder for emergencies"));
		assertThat(procedure.getInterventionDuration(), is(50));
		assertThat(procedure.getOtPreparationDuration(), is(10));
		assertThat(procedure.getInpatientStay(), is(1));

		Patient patient = patientService.getPatientByUuid(OTMetadata.PLACEHOLDER_PATIENT_UUID);
		assertThat(patient, is(notNullValue()));
		assertThat(patient.getGivenName(), is("EMERGENCY"));
		assertThat(patient.getFamilyName(), is("PLACEHOLDER PATIENT"));
		assertThat(patient.getGender(), is("M"));
		assertThat(patient.getIdentifiers(), hasSize(1));
	}

	long seed = 0;
	SequentialIdentifierGenerator mockIdGenerator;

	private IdentifierSourceService initMockGenerator(PatientService patientService) {
		PatientIdentifierType openmrsIdType = patientService.getPatientIdentifierTypeByName(OTMetadata.OPENMRS_ID_NAME);
		mockIdGenerator = new SequentialIdentifierGenerator();
		mockIdGenerator.setIdentifierType(openmrsIdType);
		mockIdGenerator.setName("name");
		mockIdGenerator.setUuid("uuid");
		mockIdGenerator.setBaseCharacterSet(new LuhnMod30IdentifierValidator().getBaseCharacters());
		mockIdGenerator.setLength(6);
		mockIdGenerator.setFirstIdentifierBase("10000");

		IdentifierSourceService mockIdService = Mockito.mock(IdentifierSourceService.class);
		Mockito.when(mockIdService.generateIdentifier(Mockito.eq(openmrsIdType), Mockito.eq("EmergencyData"))).thenAnswer(
				new Answer<String>() {

					@Override
					public String answer(InvocationOnMock invocation) throws Throwable {
						return generateIdentifier();
					}
				}
		);
		return mockIdService;
	}

	private String generateIdentifier() {
		seed++;
		return mockIdGenerator.getIdentifierForSeed(seed);
	}
}

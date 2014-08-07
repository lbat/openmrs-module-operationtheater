package org.openmrs.module.operationtheater.uiframework.converter;

import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.api.OperationTheaterService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.when;

/**
 * Tests {@link StringToProcedureConverter}
 */
public class StringToProcedureConverterTest {

	/**
	 * @verifies return null if id is null or blank
	 * @see StringToProcedureConverter#convert(String)
	 */
	@Test
	public void convert_shouldReturnNullIfIdIsNullOrBlank() throws Exception {
		OperationTheaterService serviceMock = Mockito.mock(OperationTheaterService.class);
		StringToProcedureConverter converter = new StringToProcedureConverter();
		converter.setOtService(serviceMock);

		//call method under test and verify
		assertThat(converter.convert(null), is(nullValue()));
		assertThat(converter.convert("   "), is(nullValue()));

		Mockito.verifyZeroInteractions(serviceMock);
	}

	/**
	 * @verifies return result of otService getProcedure if id is digits only
	 * @see StringToProcedureConverter#convert(String)
	 */
	@Test
	public void convert_shouldReturnResultOfOtServiceGetProcedureIfIdIsDigitsOnly() throws Exception {
		OperationTheaterService serviceMock = Mockito.mock(OperationTheaterService.class);
		Integer id = 1;
		Procedure procedure = new Procedure();
		when(serviceMock.getProcedure(id)).thenReturn(procedure);
		StringToProcedureConverter converter = new StringToProcedureConverter();
		converter.setOtService(serviceMock);

		//call method under test and verify
		assertThat(converter.convert(String.valueOf(id)), is(procedure));
	}

	/**
	 * @verifies return result of otService getProcedureByUuid if id is not digits only
	 * @see StringToProcedureConverter#convert(String)
	 */
	@Test
	public void convert_shouldReturnResultOfOtServiceGetProcedureByUuidIfIdIsNotDigitsOnly() throws Exception {
		OperationTheaterService serviceMock = Mockito.mock(OperationTheaterService.class);
		String uuid = "uuid";
		Procedure procedure = new Procedure();
		when(serviceMock.getProcedureByUuid(uuid)).thenReturn(procedure);
		StringToProcedureConverter converter = new StringToProcedureConverter();
		converter.setOtService(serviceMock);

		//call method under test and verify
		assertThat(converter.convert(uuid), is(procedure));
	}
}

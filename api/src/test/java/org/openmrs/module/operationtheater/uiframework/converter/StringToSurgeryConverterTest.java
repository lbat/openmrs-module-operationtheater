package org.openmrs.module.operationtheater.uiframework.converter;

import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.when;

/**
 * Tests {@link StringToSurgeryConverter}
 */
public class StringToSurgeryConverterTest {

	/**
	 * @verifies return null if id is null or blank
	 * @see StringToSurgeryConverter#convert(String)
	 */
	@Test
	public void convert_shouldReturnNullIfIdIsNullOrBlank() throws Exception {
		OperationTheaterService serviceMock = Mockito.mock(OperationTheaterService.class);
		StringToSurgeryConverter converter = new StringToSurgeryConverter();
		converter.setOtService(serviceMock);

		//call method under test and verify
		assertThat(converter.convert(null), is(nullValue()));
		assertThat(converter.convert("   "), is(nullValue()));

		Mockito.verifyZeroInteractions(serviceMock);
	}

	/**
	 * @verifies return result of otService getSurgery if id are digits only
	 * @see StringToSurgeryConverter#convert(String)
	 */
	@Test
	public void convert_shouldReturnResultOfOtServiceGetSurgeryIfIdAreDigitsOnly() throws Exception {
		OperationTheaterService serviceMock = Mockito.mock(OperationTheaterService.class);
		Integer id = 1;
		Surgery surgery = new Surgery();
		when(serviceMock.getSurgery(id)).thenReturn(surgery);
		StringToSurgeryConverter converter = new StringToSurgeryConverter();
		converter.setOtService(serviceMock);

		//call method under test and verify
		assertThat(converter.convert(String.valueOf(id)), is(surgery));
	}

	/**
	 * @verifies return result of otService getSurgeryByUuid if id are not digits only
	 * @see StringToSurgeryConverter#convert(String)
	 */
	@Test
	public void convert_shouldReturnResultOfOtServiceGetSurgeryByUuidIfIdAreNotDigitsOnly() throws Exception {
		OperationTheaterService serviceMock = Mockito.mock(OperationTheaterService.class);
		String uuid = "uuid";
		Surgery surgery = new Surgery();
		when(serviceMock.getSurgeryByUuid(uuid)).thenReturn(surgery);
		StringToSurgeryConverter converter = new StringToSurgeryConverter();
		converter.setOtService(serviceMock);

		//call method under test and verify
		assertThat(converter.convert(uuid), is(surgery));
	}
}

package org.openmrs.module.operationtheater.rest.controller;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.openmrs.module.operationtheater.rest.resources.openmrs1_9.SurgeryResource1_9;
import org.openmrs.module.operationtheater.rest.resources.openmrs1_9.SurgeryResource1_9Test;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.test.Verifies;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests {@link SurgeryResource1_9}
 */
public class SurgeryResource1_9ControllerTest extends MainResourceControllerTest {

	public static final int TOTAL_SURGERIES = 4;

	private OperationTheaterService operationTheaterService;

	@Before
	public void setup() throws Exception {
		operationTheaterService = Context.getService(OperationTheaterService.class);
		executeDataSet("standardOperationTheaterTestDataset.xml");
	}

	@Test
	@Verifies(value = "should get surgery by uuid", method = "getSurgeryByUuid(String)")
	public void shouldGetSurgeryByUuid() throws Exception {

		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));

		Surgery surgery = operationTheaterService.getSurgeryByUuid(getUuid());

		assertNotNull(result);
		assertEquals(getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(true, PropertyUtils.getProperty(result, "voided"));
		//TODO add check for display string
		//		Assert.assertEquals("Hippocrates of Cos, Xanadu: 2007-01-01 00:00:00.2 - 2007-01-01 01:00:00.0",
		//				PropertyUtils.getProperty(result, "display"));
		Assert.assertEquals("22b47970-8f52-11e3-baa8-0800200c9a66", Util.getByPath(result, "patient/uuid"));
	}

	@Test
	@Verifies(value = "should get all surgeries", method = "doGetAll(RequestContext)")
	public void shouldGetAllSurgeries() throws Exception {
		MockHttpServletRequest req = newGetRequest(getURI(), new MainResourceControllerTest.Parameter(
				RestConstants.REQUEST_PROPERTY_FOR_INCLUDE_ALL, "true"));
		SimpleObject result = deserialize(handle(req));

		assertEquals(TOTAL_SURGERIES, Util.getResultsSize(result));
		assertEquals(SurgeryResource1_9Test.SURGERY_UUID,
				PropertyUtils.getProperty(Util.getResultsList(result).get(0), "uuid"));
		assertEquals(getUuid(), PropertyUtils.getProperty(Util.getResultsList(result).get(1), "uuid"));
	}

	@Test
	public void shouldGetFullTimeSlotByUuid() throws Exception {

		MockHttpServletRequest req = newGetRequest(getURI() + "/" + getUuid(), new MainResourceControllerTest.Parameter(
				RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL));
		SimpleObject result = deserialize(handle(req));

		Surgery surgery = operationTheaterService.getSurgeryByUuid(getUuid());

		Assert.assertNotNull(result);
		Assert.assertEquals(getUuid(), PropertyUtils.getProperty(result, "uuid"));
		//TODO add check for display string
		//		Assert.assertEquals("Hippocrates of Cos, Xanadu: 2007-01-01 00:00:00.2 - 2007-01-01 01:00:00.0",
		//				PropertyUtils.getProperty(result, "display"));

		Assert.assertEquals("22b47970-8f52-11e3-baa8-0800200c9a66", Util.getByPath(result, "patient/uuid"));

		Assert.assertEquals(true, PropertyUtils.getProperty(result, "voided"));
		//TODO check auditinfo?
	}

	@Test
	public void shouldVoidATimeSlot() throws Exception {

		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + SurgeryResource1_9Test.SURGERY_UUID);
		req.addParameter("!purge", "");
		req.addParameter("reason", "really ridiculous random reason");
		handle(req);

		Surgery voided = operationTheaterService.getSurgeryByUuid(SurgeryResource1_9Test.SURGERY_UUID);
		Assert.assertTrue(voided.isVoided());
		Assert.assertEquals("really ridiculous random reason", voided.getVoidReason());
	}

	@Override
	public String getURI() {
		return OperationTheaterRestController.OPERATION_THEATER_REST_NAMESPACE + "/surgery";
	}

	@Override
	public String getUuid() {
		return SurgeryResource1_9Test.VOIDED_SURGERY_UUID;
	}

	@Override
	public long getAllCount() {
		return 3;
	}

}

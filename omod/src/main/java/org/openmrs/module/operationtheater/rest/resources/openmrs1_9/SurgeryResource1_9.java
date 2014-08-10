package org.openmrs.module.operationtheater.rest.resources.openmrs1_9;

//import org.openmrs.api.context.Context;
//import org.openmrs.module.operationtheater.Surgery;
//import org.openmrs.module.operationtheater.api.OperationTheaterService;
//import org.openmrs.module.operationtheater.rest.controller.OperationTheaterRestController;
//import org.openmrs.module.webservices.rest.web.RequestContext;
//import org.openmrs.module.webservices.rest.web.RestConstants;
//import org.openmrs.module.webservices.rest.web.annotation.Resource;
//import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
//import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
//import org.openmrs.module.webservices.rest.web.representation.Representation;
//import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
//import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
//import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
//import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
//import org.openmrs.module.webservices.rest.web.response.ResponseException;
//
//@Resource(name = RestConstants.VERSION_1 + OperationTheaterRestController.OPERATION_THEATER_REST_NAMESPACE + "/surgery",
//		supportedClass = Surgery.class, supportedOpenmrsVersions = "1.9.*")
//public class SurgeryResource1_9 extends DataDelegatingCrudResource<Surgery> {
//
//	//FIXME not all attributes are covered anymore
//	@Override
//	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
//		if (rep instanceof DefaultRepresentation) {
//			DelegatingResourceDescription description = new DelegatingResourceDescription();
//			description.addProperty("uuid");
//			description.addProperty("patient");
//			description.addProperty("display", findMethod("getDisplayString"));
//			description.addProperty("voided");
//			description.addSelfLink();
//			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
//			return description;
//		} else if (rep instanceof FullRepresentation) {
//			DelegatingResourceDescription description = new DelegatingResourceDescription();
//			description.addProperty("uuid");
//			description.addProperty("patient");
//			description.addProperty("display", findMethod("getDisplayString"));
//			description.addProperty("voided");
//			description.addProperty("auditInfo", findMethod("getAuditInfo"));
//			description.addSelfLink();
//			return description;
//		}
//		return null;
//	}
//
//	@Override
//	public DelegatingResourceDescription getCreatableProperties() {
//		DelegatingResourceDescription description = new DelegatingResourceDescription();
//		//		description.addRequiredProperty("startDate");
//		//		description.addRequiredProperty("endDate");
//		//		description.addRequiredProperty("appointmentBlock");
//		return description;
//	}
//
//	@Override
//	public DelegatingResourceDescription getUpdatableProperties() {
//		return getCreatableProperties();
//	}
//
//	@Override
//	public Surgery newDelegate() {
//		return new Surgery();
//	}
//
//	@Override
//	public Surgery save(Surgery surgery) {
//		return Context.getService(OperationTheaterService.class).saveSurgery(surgery);
//	}
//
//	@Override
//	public Surgery getByUniqueId(String uuid) {
//		return Context.getService(OperationTheaterService.class).getSurgeryByUuid(uuid);
//	}
//
//	@Override
//	protected void delete(Surgery surgery, String reason, RequestContext context) throws ResponseException {
//		if (surgery.isVoided()) {
//			return;
//		}
//		Context.getService(OperationTheaterService.class).voidSurgery(surgery, reason);
//	}
//
//	@Override
//	public void purge(Surgery Surgery, RequestContext requestContext) throws ResponseException {
//		//		if (Surgery == null) {
//		//			return;
//		//		}
//		//		Context.getService(OperationTheaterService.class).purgeSurgery(Surgery);
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	protected NeedsPaging<Surgery> doGetAll(RequestContext context) {
//		return new NeedsPaging<Surgery>(Context.getService(OperationTheaterService.class).getAllSurgeries(
//				context.getIncludeAll()), context);
//	}
//
//	/**
//	 * Return a list of time slots that fall within the given constraints.
//	 *
//	 * @param appointmentType - Type of the appointment this slot must support
//	 * @param fromDate        - (optional) earliest start date.
//	 * @param toDate          - (optional) latest start date.
//	 * @param provider        - (optional) the time slots's provider.
//	 * @param location        - (optional) the time slots's location. (or predecessor location)
//	 * @param includeFull     - (optional, default false) include time slots that are already fully
//	 *                        booked
//	 */
//	@Override
//	protected PageableResult doSearch(RequestContext context) {
//
//		//		Date startDate = context.getParameter("fromDate") != null ? (Date) ConversionUtil.convert(
//		//				context.getParameter("fromDate"), Date.class) : null;
//		//
//		//		Date endDate = context.getParameter("toDate") != null ? (Date) ConversionUtil.convert(
//		//				context.getParameter("toDate"), Date.class) : null;
//		//
//		//		AppointmentType appointmentType = context.getParameter("appointmentType") != null ? Context.getService(
//		//				OperationTheaterService.class).getAppointmentTypeByUuid(context.getParameter("appointmentType")) : null;
//		//
//		//		Provider provider = context.getParameter("provider") != null ? Context.getProviderService().getProviderByUuid(
//		//				context.getParameter("provider")) : null;
//		//
//		//		Location location = context.getParameter("location") != null ? Context.getLocationService().getLocationByUuid(
//		//				context.getParameter("location")) : null;
//		//
//		//		Boolean includeFull = context.getParameter("includeFull") != null ? (Boolean) ConversionUtil.convert(
//		//				context.getParameter("includeFull"), Boolean.class) : false;
//		//
//		//		Patient patient = context.getParameter("excludeSurgerysPatientAlreadyBookedFor") != null
//		//				? Context.getPatientService().getPatientByUuid(context.getParameter("excludeSurgerysPatientAlreadyBookedFor"))
//		//				: null;
//		//
//		//		if (includeFull) {
//		//			return new NeedsPaging<Surgery>(Context.getService(OperationTheaterService.class)
//		//					.getSurgerysByConstraintsIncludingFull(appointmentType, startDate, endDate, provider, location, patient),
//		//					context);
//		//		} else {
//		//			return new NeedsPaging<Surgery>(Context.getService(OperationTheaterService.class).getSurgerysByConstraints(
//		//					appointmentType, startDate, endDate, provider, location, patient), context);
//		//		}
//		throw new UnsupportedOperationException();
//	}
//
//	public String getDisplayString(Surgery surgery) {
//		//		return surgery.getAppointmentBlock().getProvider() + ", " + surgery.getAppointmentBlock().getLocation() + ": "
//		//				+ surgery.getStartDate() + " - " + surgery.getEndDate();
//		//FIXME
//		return "this is the display string";
//	}
//}

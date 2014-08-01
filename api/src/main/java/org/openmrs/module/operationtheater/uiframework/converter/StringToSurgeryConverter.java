package org.openmrs.module.operationtheater.uiframework.converter;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.module.operationtheater.Surgery;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class StringToSurgeryConverter implements Converter<String, Surgery> {

	private Pattern onlyDigits = Pattern.compile("\\d+");

	@Autowired
	private OperationTheaterService otService;

	public void setOtService(OperationTheaterService patientService) {
		this.otService = patientService;
	}

	/**
	 * @param id
	 * @return
	 * @should return null if id is null or blank
	 * @should return result of otService getSurgery if id are digits only
	 * @should return result of otService getSurgeryByUuid if id are not digits only
	 */
	@Override
	public Surgery convert(String id) {
		if (StringUtils.isBlank(id)) {
			return null;
		} else if (onlyDigits.matcher(id).matches()) {
			return otService.getSurgery(Integer.valueOf(id));
		} else {
			return otService.getSurgeryByUuid(id);
		}
	}
}

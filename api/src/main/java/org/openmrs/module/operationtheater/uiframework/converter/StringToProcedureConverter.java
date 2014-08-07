package org.openmrs.module.operationtheater.uiframework.converter;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.module.operationtheater.Procedure;
import org.openmrs.module.operationtheater.api.OperationTheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class StringToProcedureConverter implements Converter<String, Procedure> {

	private Pattern onlyDigits = Pattern.compile("\\d+");

	@Autowired
	private OperationTheaterService otService;

	public void setOtService(OperationTheaterService otService) {
		this.otService = otService;
	}

	/**
	 * @param id
	 * @return
	 * @should return null if id is null or blank
	 * @should return result of otService getProcedure if id is digits only
	 * @should return result of otService getProcedureByUuid if id is not digits only
	 */
	@Override
	public Procedure convert(String id) {
		if (StringUtils.isBlank(id)) {
			return null;
		} else if (onlyDigits.matcher(id).matches()) {
			return otService.getProcedure(Integer.valueOf(id));
		} else {
			return otService.getProcedureByUuid(id);
		}
	}
}

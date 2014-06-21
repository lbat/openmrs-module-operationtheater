<%
    ui.decorateWith("appui", "standardEmrPage")
//    ui.includeCss("appointmentschedulingui", "appointmentType.css")
    ui.includeJavascript("operationtheater", "patientsSurgeries.js")
%>

${ui.includeFragment("coreapps", "patientHeader", [patient: patient.patient, activeVisit: activeVisit])}

<script type="text/javascript">

    // TODO redo usnig angular?

    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ patient.patient.familyName }, ${ patient.patient.givenName }", link: '${ui.pageLink("coreapps", "clinicianfacing/patient", [patientId: patient.id])}'},
        { label: "${ ui.message("operationtheater.patientsSurgeries.breadcrumbLabel")}" }
    ];

    jq(function () {
        var resultMessage = "${resultMessage}";
        if (resultMessage != "") {
            emr.successMessage(resultMessage);
        }
    });

</script>
<style>
/* Todo refactor to compass */

/* line 1, ../../../compass/sass/appointmentType.scss */
.procedure-label {
    display: inline-block;
}

/* line 5, ../../../compass/sass/appointmentType.scss */
.create-procedure input, .create-procedure textarea {
    min-width: 0;
}

/* line 9, ../../../compass/sass/appointmentType.scss */
#proceduresTable {
    word-break: break-all;
}
</style>


<div class="container">
    <div>
        <div id="manageProceduresTitle" class="procedure-label">
            <h1>
                ${ui.message("operationtheater.patientsSurgeries.title")}
            </h1>
        </div>

        <button class="confirm procedure-label right"
                onclick="location.href = '${ui.pageLink("operationtheater", "surgery")}?patientId=${patientId}'">
            <i class="icon-plus"></i>
            ${ui.message("operationtheater.patientsSurgeries.button.new")}
        </button>

    </div>

    <div id="procedures-list">
        <table id="proceduresTable" empty-value-message='${ui.message("uicommons.dataTable.emptyTable")}'>
            <thead>
            <tr>
                <th style="width: 22%">${ui.format(ui.message("general.dateCreated"))}</th>
                <th style="width: 20%">${ui.message("general.name")}</th>
                <th style="width: 50%">${ui.message("general.description")}</th>
                <th style="width: 8%">${ui.message("general.action")}</th>
            </tr>
            </thead>
            <tbody>
            <% surgeryList.each { surgery -> %>

            <tr>
                <td>${ui.format(surgery.dateCreated)}</td>
                <td>${ui.format(surgery.procedure.name)}</td>
                <td>${ui.format(surgery.procedure.description)}</td>
                <td class="align-center">
                    <span>
                        <i class="editProcedure delete-item icon-pencil"
                           data-procedure-id="${surgery.id}"
                           data-patient-id="${surgery.patient.id}"
                           data-edit-url='${ui.pageLink("operationtheater", "surgery")}'
                           title="${ui.message("coreapps.edit")}"></i>
                        <i class="deleteProcedure delete-item icon-remove"
                           data-procedure-id="${surgery.id}"
                           title="${ui.message("coreapps.delete")}"></i>
                    </span>
                </td>
            </tr>


            <div id="delete-procedure-dialog" class="dialog" style="display: none">
                <div class="dialog-header">
                    <h3>${ui.message("operationtheater.patientsSurgeries.deleteSurgeryDialogTitle")}</h3>
                </div>

                <div class="dialog-content">
                    <input type="hidden" id="encounterId" value="">
                    <ul>
                        <li class="info">
                            <span>${ui.message("operationtheater.patientsSurgeries.deleteSurgeryDialogMessage")}</span>
                        </li>
                    </ul>

                    <button class="confirm right">${ui.message("emr.yes")}
                        <i class="icon-spinner icon-spin icon-2x" style="display: none; margin-left: 10px;"></i></button>
                    <button class="cancel">${ui.message("emr.no")}</button>
                </div>
            </div>
            <% } %>
            </tbody>
        </table>
    </div>
</div>


${ui.includeFragment("uicommons", "widget/dataTable", [object : "#proceduresTable",
                                                       options: [
                                                               bFilter        : false,
                                                               bJQueryUI      : true,
                                                               bLengthChange  : false,
                                                               iDisplayLength : 10,
                                                               sPaginationType: '\"full_numbers\"',
                                                               bSort          : false,
                                                               sDom           : '\'ft<\"fg-toolbar ui-toolbar ui-corner-bl ui-corner-br ui-helper-clearfix datatables-info-and-pg \"ip>\''
                                                       ]
])}



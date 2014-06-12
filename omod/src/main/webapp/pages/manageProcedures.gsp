<%
    ui.decorateWith("appui", "standardEmrPage")
//    ui.includeCss("appointmentschedulingui", "appointmentType.css")
    ui.includeJavascript("operationtheater", "manageProcedures.js")
%>


<script type="text/javascript">

    // TODO redo usnig angular?

    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("operationtheater.procedure.manage")}" }
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
                ${ui.message("operationtheater.procedure.manage.title")}
            </h1>
        </div>

        <button class="confirm procedure-label right"
                onclick="location.href = '${ui.pageLink("operationtheater", "createEditProcedure")}'">
            <i class="icon-plus"></i>
            ${ui.message("operationtheater.procedure.button.new")}
        </button>

    </div>

    <div id="procedures-list">
        <table id="proceduresTable" empty-value-message='${ui.message("uicommons.dataTable.emptyTable")}'>
            <thead>
            <tr>
                <th style="width: 22%">${ui.message("general.name")}</th>
                <th style="width: 22%">${ui.message("general.description")}</th>
                <th style="width: 16%">${ui.message("operationtheater.manageprocedures.interventionDuration")}</th>
                <th style="width: 16%">${ui.message("operationtheater.manageprocedures.otPreparationDuration")}</th>
                <th style="width: 16%">${ui.message("operationtheater.manageprocedures.inpatientStay")}</th>
                <th style="width: 8%">${ui.message("general.action")}</th>
            </tr>
            </thead>
            <tbody>
            <% procedureList.each { procedure -> %>

            <tr>
                <td>${ui.format(procedure.name)}</td>
                <td>${ui.format(procedure.description)}</td>
                <td>${ui.format(procedure.interventionDuration)}</td>
                <td>${ui.format(procedure.otPreparationDuration)}</td>
                <td>${ui.format(procedure.inpatientStay)}</td>
                <td class="align-center">
                    <span>
                        <i class="editProcedure delete-item icon-pencil"
                           data-procedure-id="${procedure.id}"
                           data-edit-url='${ui.pageLink("operationtheater", "createEditProcedure")}'
                           title="${ui.message("coreapps.edit")}"></i>
                        <i class="deleteProcedure delete-item icon-remove"
                           data-procedure-id="${procedure.id}"
                           title="${ui.message("coreapps.delete")}"></i>
                    </span>
                </td>
            </tr>


            <div id="delete-procedure-dialog" class="dialog" style="display: none">
                <div class="dialog-header">
                    <h3>${ui.message("operationtheater.manageprocedures.deleteProcedureDialogTitle")}</h3>
                </div>

                <div class="dialog-content">
                    <input type="hidden" id="encounterId" value="">
                    <ul>
                        <li class="info">
                            <span>${ui.message("operationtheater.manageprocedures.deleteProcedureDialogMessage")}</span>
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



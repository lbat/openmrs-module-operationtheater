<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeJavascript("operationtheater", "surgery_page/surgicalTeam.js")
    ui.includeJavascript("operationtheater", "surgery_page/workflow.js")

    ui.includeJavascript("uicommons", "emr.js")
    ui.includeJavascript("uicommons", "typeahead.js");
    ui.includeJavascript("operationtheater", "bower_components/validation/jquery.validate.js")

    ui.includeCss("referenceapplication", "referenceapplication.css")
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.format(patient.patient.familyName) }, ${ ui.format(patient.patient.givenName) }",
            link: '${ui.pageLink("coreapps", "clinicianfacing/patient", [patientId: patient.id])}'},
        { label: "${ui.message("operationtheater.patientsSurgeries.breadcrumbLabel")}",
            link: '${ui.pageLink("operationtheater", "patientsSurgeries", [patientId: patient.patient.id])}'},
        { label: "${surgery.procedure.name?surgery.procedure.name: ui.message("operationtheater.patientsSurgeries.button.new")}"}
    ]
    var patient = { id: ${ patient.id } };
</script>

<%=ui.includeFragment("appui", "messages", [codes: [
        "coreapps.delete",
        "operationtheater.procedure.notFound",
        "operationtheater.provider.notFound",
        "uicommons.dataTable.emptyTable",
].flatten()
])%>

${ui.includeFragment("coreapps", "patientHeader", [patient: patient.patient, activeVisit: activeVisit])}


<!-- TODO create css with compass -->
<style type='text/css'>
input.error {
    border: 2px solid;
    border-color: #ff6666;
}
</style>


<script type="text/javascript">

    jq().ready(function () {

        var procedureMap = {};
        <% procedureList.each{ procedure ->%>
        procedureMap['${procedure.name}'] = '${procedure.uuid}';
        <% } %>
        var options = { source: Object.keys(procedureMap) };
        jq('#surgeryProcedure-field').typeahead(options);

        jq('#setProcedureButton').click(function () {
            if (!jq('#surgeryProcedure-field').valid()) {
                return;
            }

            var procedureUuid = procedureMap[jq('#surgeryProcedure-field').val()];
            emr.getFragmentActionWithCallback("operationtheater", "surgery", "updateProcedure",
                    {surgery: "${surgery.uuid}", procedure: procedureUuid}
                    , function (data) {
                        emr.successMessage(data.message);
                    }, function (err) {
                        emr.handleError(err);
                    }
            );
        });

        //surgical team
        var providerMap = {};
        <% providerList.each{ provider ->%>
        providerMap['${provider.name}'] = '${provider.uuid}';
        <% } %>
        var providerOptions = { source: Object.keys(providerMap) };

        surgicalTeam.init("${surgery.uuid}", providerMap, providerOptions);
        surgicalTeam.get();

        //workflow
        workflow.init("${surgery.uuid}");

        //validation
        jq.validator.addMethod("procedureCheck", function (value, element) {
            console.log("procedureCheck");
            return jq.inArray(value, options.source) != -1;
        }, emr.message('operationtheater.procedure.notFound'));

        jq.validator.addMethod("providerCheck", function (value, element) {
            console.log("providerCheck");
            return jq.inArray(value, providerOptions.source) != -1;
        }, emr.message('operationtheater.provider.notFound'));

        //TODO set error messages to support internationalization
        window.validator = jq("#surgeryForm").validate({
            rules: {
                "surgeryProcedure": {
                    required: true,
                    procedureCheck: true
                },
                "addProviderTextfield": {
                    providerCheck: true
                }
            },
            errorClass: "error",
            validClass: "",
            errorPlacement: function (error, element) {
                var errorEl = jq(element).next();
                while (errorEl.prop('tagName') != 'SPAN') {
                    errorEl = jq(errorEl).next();
                }
                errorEl.text(error.text());
            },
            highlight: function (element, errorClass, validClass) {
                jq(element).addClass(errorClass);
                var errorEl = jq(element).next();
                while (errorEl.prop('tagName') != 'SPAN') {
                    errorEl = jq(errorEl).next();
                }
                errorEl.addClass(errorClass);
                errorEl.show();
            },
            unhighlight: function (element, errorClass, validClass) {
                jq(element).removeClass(errorClass);
                var errorEl = jq(element).next();
                while (errorEl.prop('tagName') != 'SPAN') {
                    errorEl = jq(errorEl).next();
                }
                errorEl.removeClass(errorClass);
                errorEl.hide();
            }
        });
    });
</script>

<h2>
    ${ui.message("operationtheater.surgery.page.title")}
</h2>

<form class="simple-form-ui" id="surgeryForm" autocomplete="off">
    <fieldset>
        <legend>${ui.message("operationtheater.surgery.page.fieldset.procedure")}</legend>
        <!-- TODO maxlength should be managed centrally in the POJO-->
        ${ui.includeFragment("uicommons", "field/text", [
                label        : ui.message("general.name"),
                formFieldName: "surgeryProcedure",
                id           : "surgeryProcedure",
                maxLength    : 101,
                initialValue : (surgery.procedure.name ?: '')
        ])}
        <a class="button" id="setProcedureButton">${ui.message("general.save")}</a>

    </fieldset>

    <fieldset>
        <legend>${ui.message("operationtheater.surgery.page.fieldset.surgicalTeam")}</legend>

        <div id="surgical-team-list">
            <table id="surgical-team-table" empty-value-message='${ui.message("uicommons.dataTable.emptyTable")}'>
                <thead>
                <tr>
                    <th style="width: 80%">${ui.message("general.name")}</th>
                    <th style="width: 20%">${ui.message("general.action")}</th>
                </tr>
                </thead>
                <tbody>

                </tbody>
            </table>
        </div>

        <p>
            ${ui.includeFragment("uicommons", "field/text", [
                    label        : "Add Provider",
                    formFieldName: "addProviderTextfield",
                    id           : "addProviderTextfield",
                    maxLength    : 101,
                    initialValue : ""
            ])}
            <a class="button" id="addProviderButton">${ui.message("general.add")}</a>
        </p>

    </fieldset>

    <fieldset>
        <legend>${ui.message("operationtheater.surgery.page.fieldset.workflow")}</legend>
        <a class="button" id="startSurgeryButton">${ui.message("operationtheater.surgery.page.button.beginSurgery")}</a>
        <a class="button" id="finishSurgeryButton">${ui.message("operationtheater.surgery.page.button.finishSurgery")}</a>

        <p>
        <table id="timestamp-table">
            <thead>
            <tr>
                <th>${ui.message("operationtheater.surgery.page.tableColumn.event")}</th>
                <th>${ui.message("operationtheater.surgery.page.tableColumn.date")}</th>
            </tr>
            </thead>
            <tbody>

            </tbody>
        </table>
    </p>
    </fieldset>
</form>

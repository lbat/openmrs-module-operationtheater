<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeJavascript("uicommons", "typeahead.js");
    ui.includeJavascript("operationtheater", "bower_components/validation/jquery.validate.js")

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
        //var options = { source: ["Antepartum Ward", "Central Archives", "Clinic Registration", "Community Health", "Dental", "Emergency", "Emergency Reception", "ICU", "Isolation", "Labor and Delivery", "Main Laboratory", "Men's Internal Medicine", "NICU", "Operating Rooms", "Outpatient Clinic", "Pediatrics", "Post-op GYN", "Postpartum Ward", "Pre-op/PACU", "Radiology", "Surgical Ward", "Women's Clinic", "Women's Internal Medicine", "Women's Outpatient Laboratory", "Women's Triage"], items: 3 };
        jq('#surgeryProcedure-field').typeahead(options);

        jq.validator.addMethod("procedureCheck", function (value, element) {
            return jq.inArray(value, options.source) != -1;

        }, '${ui.message('operationtheater.surgeryPage.fieldError.procedureDoesNotExist')}');

        //TODO set error messages to support internationalization
        jq("#surgeryForm").validate({
            rules: {
                "surgeryProcedure": {
                    required: true,
                    procedureCheck: true
                }
            },
            errorClass: "error",
            validClass: "",
            onfocusout: function (element) {
                jq(element).valid();
            },
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

        //submit function
        jq('#surgeryForm').submit(function () {
            jq('#procedureUuid').val(procedureMap[jq('#surgeryProcedure-field').val()])

            // ... continue work
        });

    });
</script>

<h2>
    ${ui.message("operationtheater.procedure.title")}
</h2>

<form class="simple-form-ui" method="post" id="surgeryForm" autocomplete="off">

    <!--<p>
        <input id="typeahead" formField data-provide="typeahead" placeholder="Auto Suggest" type="text" />
    </p>-->

    <!-- TODO maxlength should be managed centrally in the POJO-->
    ${ui.includeFragment("uicommons", "field/text", [
            label        : ui.message("general.name"),
            formFieldName: "",
            id           : "surgeryProcedure",
            maxLength    : 101,
            initialValue : (surgery.procedure.name ?: '')
    ])}

    <input type="hidden" value="${surgery.uuid}" name="surgeryUuid">
    <input type="hidden" value="${surgery.procedure.uuid ?: ''}" name="procedureUuid" id="procedureUuid">

    <div>
        <!--<input type="button" class="cancel" value="${ui.message("emr.cancel")}"
               onclick="javascript:window.location = '${ui.pageLink("operationtheater", "manageProcedures")}'"/>-->
        <input type="submit" class="confirm" id="save-button" value="${ui.message("general.save")}"/>
    </div>

</form>

<%
    //based on procedureschedulingui appointmentType.gsp

    ui.decorateWith("appui", "standardEmrPage")

    ui.includeJavascript("operationtheater", "bower_components/validation/jquery.validate.js")
%>

<!-- TODO create css with compass -->
<style type='text/css'>
input.error {
    border-color: #ff6666;
}
</style>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("operationtheater.procedure.manage")}",
            link: '${ui.pageLink("operationtheater", "manageProcedures")}' },
        { label: "${ ui.message("operationtheater.procedure.title")}" }
    ];
</script>

<script type="text/javascript">

    jq().ready(function () {

        //TODO set error messages to support internationalization
        jq("#procedureForm").validate({
            rules: {
                "name": {
                    required: true,
                    minlength: 3,
                    maxlength: 255
                },
                "description": {
                    required: false,
                    maxlength: 1024
                },
                "interventionDuration": {
                    required: true,
                    digits: true,
                    min: 0,
                    max: ${maxInterventionDuration}
                },
                "otPreparationDuration": {
                    required: true,
                    digits: true,
                    min: 0,
                    max: ${maxOtPreparationDuration}
                },
                "inpatientStay": {
                    required: true,
                    digits: true,
                    min: 0,
                    max: ${maxInpatientStay}
                }
            },
            errorClass: "error",
            validClass: "",
            onfocusout: function (element) {
                jq(element).valid();
            },
            errorPlacement: function (error, element) {
                element.next().text(error.text());
            },
            highlight: function (element, errorClass, validClass) {
                jq(element).addClass(errorClass);
                jq(element).next().addClass(errorClass);
                jq(element).next().show();
            },
            unhighlight: function (element, errorClass, validClass) {
                jq(element).removeClass(errorClass);
                jq(element).next().removeClass(errorClass);
                jq(element).next().hide();
            }
        });
    });
</script>

<h1>
    ${ui.message("operationtheater.procedure.title")}
</h1>

<form class="simple-form-ui" method="post" id="procedureForm">

    ${ui.includeFragment("uicommons", "field/text", [
            label        : ui.message("general.name"),
            formFieldName: "name",
            id           : "name",
            maxLength    : 101,
            initialValue : (procedure.name ?: '')
    ])}

    ${ui.includeFragment("emr", "field/textarea", [
            label        : ui.message("general.description"),
            formFieldName: "description",
            id           : "description",
            initialValue : (procedure.description ?: '')
    ])}

    ${ui.includeFragment("uicommons", "field/text", [
            label        : ui.message("operationtheater.procedure.interventionDuration"),
            formFieldName: "interventionDuration",
            id           : "intervention-duration",
            initialValue : (procedure.interventionDuration ?: '')
    ])}

    ${ui.includeFragment("uicommons", "field/text", [
            label        : ui.message("operationtheater.procedure.otPreparationDuration"),
            formFieldName: "otPreparationDuration",
            id           : "ot-preparation-duration",
            initialValue : (procedure.otPreparationDuration ?: '')
    ])}

    ${ui.includeFragment("uicommons", "field/text", [
            label        : ui.message("operationtheater.procedure.inpatientStay"),
            formFieldName: "inpatientStay",
            id           : "inpatient-stay",
            initialValue : (procedure.inpatientStay ?: '')
    ])}

    <input type="hidden" value="${procedure.uuid}" name="uuid">

    <div>
        <input type="button" class="cancel" value="${ui.message("emr.cancel")}"
               onclick="javascript:window.location = '${ ui.pageLink("operationtheater", "manageProcedures") }'"/>
        <input type="submit" class="confirm" id="save-button" value="${ui.message("operationtheater.procedure.save")}"/>
    </div>

</form>

(function (surgery, $, undefined) {

    surgery.initProcedureButton = function (surgeryUuid, procedureMap, newSurgery_, patientId) {
        var newSurgery;

        jq('#setProcedureButton').click(function () {
            if (!jq('#surgeryProcedure-field').valid()) {
                return;
            }

            newSurgery = newSurgery_;

            var procedureUuid = procedureMap[jq('#surgeryProcedure-field').val()];

            if (newSurgery) {
                emr.getFragmentActionWithCallback("operationtheater", "surgery", "createNewSurgery",
                    {surgery: surgeryUuid, procedure: procedureUuid, patient: patientId}
                    , function (data) {
                        emr.successMessage(data.message);
                        newSurgery = false;
                        //show other fieldsets
                        jq('#surgicalTeamFieldset').show();
                        jq('#workflowFieldset').show();
                        //change button text
                        jq('#setProcedureButton').text(emr.message("general.save"));
                        workflow.getDataFromServer();
                    }, function (err) {
                        emr.handleError(err);
                    }
                );
            } else {
                emr.getFragmentActionWithCallback("operationtheater", "surgery", "updateProcedure",
                    {surgery: "${surgery.uuid}", procedure: procedureUuid}
                    , function (data) {
                        emr.successMessage(data.message);
                    }, function (err) {
                        emr.handleError(err);
                    }
                );
            }
        });
    };

    surgery.setUpValidation = function (options, providerOptions) {
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
    };

})(window.surgery = window.surgery || {});

//Self-Executing Anonymous Function Pattern
//http://appendto.com/2010/10/how-good-c-habits-can-encourage-bad-javascript-habits-part-1/
(function (filterResourcesDialog, $, undefined) {
    //private properties
    var dialog;
    var resources;

    //public methods
    filterResourcesDialog.show = function (calResources) {
        resources = calResources;

        //dynamically add checkboxes
        var html = "";
        for (i in resources) {
            var checked = resources[i].selected ? "checked" : "";
            html += '<p>' +
                '<input class="resources" id="resource-' + resources[i].id + '" name="resource" ' + checked + ' type="checkbox"></input>' +
                '<label>' + resources[i].name + '</label>' +
                '</p>';
        }

        html += '<span id="filter-resources-error" class="field-error error" style=""></span>';

        jq("#filter-resources-form").html(html);

        //validation
        jq('#filter-resources-form').validate({
            ignore: [],
            rules: {
                "resource": {
                    atLeastOneCheckbox: true
                }
            },
            errorClass: "error",
            validClass: "",
            errorPlacement: function (error, element) {
                jq('#filter-resources-error').text(error.text());
            },
            highlight: function (element, errorClass, validClass) {
                jq('#filter-resources-error').show();
            },
            unhighlight: function (element, errorClass, validClass) {
                jq('#filter-resources-error').hide();
            }
        });

        dialog.show();
        return false;
    };

    filterResourcesDialog.create = function () {

        dialog = emr.setupConfirmationDialog({
            selector: '#filter-resources-dialog',
            actions: {
                confirm: function () {
                    if (!jq('#filter-resources-form').valid()) {
                        return;
                    }

                    updateFullCalendar();
                    dialog.close();
                },
                cancel: function () {
                    dialog.close();
                }
            }
        });

        //validation
        jq.validator.addMethod("atLeastOneCheckbox", function (value, elem, params) {
            if (jq(".resources:checkbox:checked").length > 0) {
                return true;
            } else {
                return false;
            }
        }, "Please select at least one operation theater!");
    };

    //private methods
    function updateFullCalendar() {
        for (i in resources) {
            if (jq('#resource-' + resources[i].id).is(':checked') && !resources[i].selected) {
                //add
                jq('#calendar').fullCalendar('addEventResource', resources[i]);
                resources[i].selected = true;
            } else if (!jq('#resource-' + resources[i].id).is(':checked') && resources[i].selected) {
                //remove
                jq('#calendar').fullCalendar('removeEventResource', resources[i].id);
                resources[i].selected = false;
            }
        }
    }

}(window.filterResourcesDialog = window.filterResourcesDialog || {}, jQuery));

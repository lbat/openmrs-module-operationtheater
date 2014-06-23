//Self-Executing Anonymous Function Pattern
//http://appendto.com/2010/10/how-good-c-habits-can-encourage-bad-javascript-habits-part-1/
(function (availableTimesDialog, $, undefined) {
    //Private Property
    var dialog;
    var dateFormatted = "";
    var calEvent;

    //Public Method
    availableTimesDialog.show = function (event) {
        calEvent = event;

        dateFormatted = jq.fullCalendar.formatDate(event.start, 'yyyy-MM-dd');

        jq('#available-times-dialog-ot').text(event.resource.name);
        jq('#available-times-dialog-date').text(dateFormatted);

        var start = event.availableStart;
        var end = event.availableEnd;
        jq('#is-ot-available').prop("checked", start != end);

        var startOfDay = new Date(event.start);
        startOfDay.setHours(0);
        startOfDay.setMinutes(0);
        var tomorrow = new Date(event.start);
        tomorrow.setDate(tomorrow.getDate() + 1);
        var endOfDay = new Date(tomorrow - 60000); //subtract one minute = 60000ms

        jq('#start_time_picker-wrapper').datetimepicker('setStartDate', startOfDay);
        jq('#start_time_picker-wrapper').datetimepicker('setEndDate', endOfDay);

        jq('#end_time_picker-wrapper').datetimepicker('setStartDate', startOfDay);
        jq('#end_time_picker-wrapper').datetimepicker('setEndDate', endOfDay);

        var availableStart = jq.fullCalendar.formatDate(new Date(event.availableStart), 'HH:mm');
        var availableEnd = jq.fullCalendar.formatDate(new Date(event.availableEnd), 'HH:mm');

        console.log(availableEnd);

        jq("#start_time_picker-display").val(availableStart);
        jq("#end_time_picker-display").val(availableEnd);

        jq("#start_time_picker-field").val(event.availableStart);
        jq("#end_time_picker-field").val(event.availableEnd);

        //resets validation errors
        availableTimesDialog.requires("start_time_picker");
        availableTimesDialog.requires("end_time_picker");

        dialog.show();
        return false;
    };

    availableTimesDialog.createDialog = function () {

        dialog = emr.setupConfirmationDialog({
            selector: '#available-times-dialog',
            actions: {
                confirm: function () {
                    if (!availableTimesDialog.validate())
                        return;

                    var params = {};
                    params.locationUuid = calEvent.resource.uuid//"76d562c4-79d7-45ef-b1c4-8e15c2b01c41";
                    params.available = jq('#is-ot-available').is(':checked');
                    params.startTime = dateFormatted + " " + jq("#start_time_picker-display").val() + ":00.0"; //not working on server side if seconds and milliseconds are missing (even with custom pattern)
                    params.endTime = dateFormatted + " " + jq("#end_time_picker-display").val() + ":00.0";
                    emr.getFragmentActionWithCallback('operationtheater', 'scheduling', 'adjustAvailableTimes', params
                        , function (data) {
                            emr.successMessage("Updated available times"); //Todo message.properties
                            dialog.close();
                            jq('#calendar').fullCalendar('refetchEvents');
                        });
                },
                cancel: function () {
                    dialog.close();
                }
            }
        });

        //
        jq('#is-ot-available').change(function () {
            if (jq('#is-ot-available').is(':checked')) {
                //enable
                jq('#start_time_picker-display').attr("disabled", false);
                jq('#end_time_picker-display').attr("disabled", false);
            } else {
                //disable
                jq('#start_time_picker-display').attr("disabled", true);
                jq('#end_time_picker-display').attr("disabled", true);
                availableTimesDialog.clearFieldError(jq('#start_time_picker-display'), jq('#start_time_picker .field-error'));
                availableTimesDialog.clearFieldError(jq('#end_time_picker-display'), jq('#end_time_picker .field-error'));
//            jq('#end_time_picker-wrapper .add-on').attr("disabled",true );
//            jq('#end_time_picker-wrapper .add-on i').attr("disabled",true );
                //FIXME disable small calendar icon too
            }
        });

        //add required field validation handlers
        jq('#start_time_picker-display').attr("onblur", "javascript:availableTimesDialog.requires('start_time_picker')");
        jq('#start_time_picker-display').attr("onChange", "javascript:availableTimesDialog.requires('start_time_picker')");

        jq('#end_time_picker-display').attr("onblur", "javascript:availableTimesDialog.requires('end_time_picker')");
        jq('#end_time_picker-display').attr("onChange", "javascript:availableTimesDialog.requires('end_time_picker')");

    };

    availableTimesDialog.setFieldError = function (el, errorEl, errorMessage) {
        el.className = 'illegalValue';
        errorEl.text(errorMessage);
        errorEl.show();
    };


    availableTimesDialog.clearFieldError = function (el, errorEl) {
        el.className = 'legalValue';
        errorEl.text("");
        errorEl.hide();
    };

    availableTimesDialog.requires = function (elId) {
        var el = jq('#' + elId + "-display");
        var errorEl = jq('#' + elId + ' .field-error');

        availableTimesDialog.clearFieldError(el, errorEl);

        if (jq('#' + elId + "-field").val() == '') {

            availableTimesDialog.setFieldError(el, errorEl, "This field is required");
            return true;
        }
        return false;
    };

    availableTimesDialog.validate = function () {
        var startTimeId = "start_time_picker";
        var endTimeId = "end_time_picker";
        var startTimeEmpty = availableTimesDialog.requires(startTimeId)
        var endTimeEmpty = availableTimesDialog.requires(endTimeId);

        if (!jq('#is-ot-available').is(':checked')) {
            return true;
        }

        if (!startTimeEmpty && !endTimeEmpty) {
            var end = new Date(jq('#' + endTimeId + '-field').val());
            var start = new Date(jq('#' + startTimeId + '-field').val());

            console.log(start);
            console.log(end);

            if (start >= end) {
                var el = jq('#' + endTimeId + "-display");
                var errorEl = jq('#' + endTimeId + ' .field-error');

                var message = "start time must be before the end time";

                availableTimesDialog.setFieldError(el, errorEl, message);
            }
            else {
                return true;
            }
        }

        return false;
    };
}(window.availableTimesDialog = window.availableTimesDialog || {}, jQuery));

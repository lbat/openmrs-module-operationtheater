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

//        var availableStart = jq.fullCalendar.formatDate(new Date(event.availableStart), 'HH:mm');
        var availableEnd = jq.fullCalendar.formatDate(new Date(event.availableEnd), 'HH:mm');
        var availableStart = moment(event.availableStart).format('HH:mm');

        jq("#start_time_picker-display").val(availableStart);
        jq("#end_time_picker-display").val(availableEnd);

        jq("#start_time_picker-field").val(event.availableStart);
        jq("#end_time_picker-field").val(event.availableEnd);

        //resets validation errors
        jq('#available-time-form').valid();

        dialog.show();
        return false;
    };

    availableTimesDialog.create = function () {

        dialog = emr.setupConfirmationDialog({
            selector: '#available-times-dialog',
            actions: {
                confirm: function () {
                    if (!jq('#available-time-form').valid())
                        return;

                    jq('#available-times-dialog' + ' .icon-spin').css('display', 'inline-block').parent().addClass('disabled');

                    var params = {};
                    params.locationUuid = calEvent.resource.uuid//"76d562c4-79d7-45ef-b1c4-8e15c2b01c41";
                    params.available = jq('#is-ot-available').is(':checked');
                    params.startTime = dateFormatted + " " + jq("#start_time_picker-display").val() + ":00.0"; //not working on server side if seconds and milliseconds are missing (even with custom pattern)
                    params.endTime = dateFormatted + " " + jq("#end_time_picker-display").val() + ":00.0";
                    emr.getFragmentActionWithCallback('operationtheater', 'scheduling', 'adjustAvailableTimes', params
                        , function (data) {
                            emr.successMessage(data.message);
                            dialog.close();
                            jq('#available-times-dialog' + ' .icon-spin').css('display', 'none').parent().removeClass('disabled');
                            jq('#calendar').fullCalendar('refetchEvents');
                        }, function (err) {
                            emr.handleError(err); //FIXME field errors are not displayed - only global errors
                        }
                    );
                },
                cancel: function () {
                    dialog.close();
                }
            }
        });

        //checkbox event listener
        jq('#is-ot-available').change(function () {
            jq('#available-time-form').valid();

            if (jq('#is-ot-available').is(':checked')) {
                //enable
                jq('#start_time_picker-display').attr("disabled", false);
                jq('#end_time_picker-display').attr("disabled", false);
            } else {
                //disable
                jq('#start_time_picker-display').attr("disabled", true);
                jq('#end_time_picker-display').attr("disabled", true);
//            jq('#end_time_picker-wrapper .add-on').attr("disabled",true );
//            jq('#end_time_picker-wrapper .add-on i').attr("disabled",true );
                //FIXME disable small calendar icon too
            }
        });

        //validation
        var greaterThanErrorMsg = emrExt.message(emr.message("operationtheater.validation.error.greaterThan"),
            emr.message("operationtheater.scheduling.page.availableTimesDialog.label.startTime"));

        jq.validator.addMethod("greaterThan", function (value, element, params) {

            if (!jq('#is-ot-available').is(':checked'))
                return true;

            if (!/Invalid|NaN/.test(new Date(value))) {
                return new Date(value) > new Date($(params).val());
            }

            return isNaN(value) && isNaN($(params).val())
                || (Number(value) > Number($(params).val()));
        }, greaterThanErrorMsg);

        jq('#available-time-form').validate({
            ignore: [],
            rules: {
                "startTimePicker": {
                    required: true
                },
                "endTimePicker": {
                    required: function () {
                        return jq('#is-ot-available').is(':checked');
                    },
                    greaterThan: "#start_time_picker-field"
                }
            },
            errorClass: "error",
            validClass: "",
            //TODO no onfocusout: problem with datetimepicker
//            onfocusout: function (element) {
//                jq(element).valid();
//            },
            errorPlacement: function (error, element) {
                element.next().text(error.text());
            },
            highlight: function (element, errorClass, validClass) {
                jq(element).prev().children().first().addClass(errorClass);
                var selector = "#" + element.id.slice(0, -6) + "-error";
                jq(selector).addClass(errorClass);
                jq(selector).show();
            },
            unhighlight: function (element, errorClass, validClass) {
                jq(element).prev().children().first().removeClass(errorClass);
                var selector = "#" + element.id.slice(0, -6) + "-error";
                jq(selector).removeClass(errorClass);
                jq(selector).hide();
            }
        });
    };
}(window.availableTimesDialog = window.availableTimesDialog || {}, jQuery));

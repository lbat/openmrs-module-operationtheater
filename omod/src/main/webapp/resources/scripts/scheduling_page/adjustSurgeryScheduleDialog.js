//Self-Executing Anonymous Function Pattern
//http://appendto.com/2010/10/how-good-c-habits-can-encourage-bad-javascript-habits-part-1/
(function (adjustSurgeryScheduleDialog, $, undefined) {
    //Private Property
    var dialog;
    var calEvent;

    //Public Method
    adjustSurgeryScheduleDialog.show = function (event) {
        calEvent = event;

        jq('#adjust-schedule-surgery').text(calEvent.title);

        jq('#adjust-surgery-form-location').val(calEvent.resource.name);

        jq('#adjust_schedule_start_time-wrapper').datetimepicker('setStartDate', new Date());

        var availableStart = moment(event.start).format("DD-MM-YYYY HH:mm");
        jq("#adjust_schedule_start_time-display").val(availableStart);
        jq("#adjust_schedule_start_time-field").val(event.start);

//        jq('#available-times-dialog-ot').text(event.resource.name);
//
//        var start = event.availableStart;
//        var end = event.availableEnd;
//        jq('#is-ot-available').prop("checked", start != end);
//
//        var startOfDay = new Date(event.start);
//        startOfDay.setHours(0);
//        startOfDay.setMinutes(0);
//        var tomorrow = new Date(event.start);
//        tomorrow.setDate(tomorrow.getDate() + 1);
//        var endOfDay = new Date(tomorrow - 60000); //subtract one minute = 60000ms
//
//        jq('#start_time_picker-wrapper').datetimepicker('setStartDate', startOfDay);
//        jq('#start_time_picker-wrapper').datetimepicker('setEndDate', endOfDay);
//
//        jq('#end_time_picker-wrapper').datetimepicker('setStartDate', startOfDay);
//        jq('#end_time_picker-wrapper').datetimepicker('setEndDate', endOfDay);
//
////        var availableStart = jq.fullCalendar.formatDate(new Date(event.availableStart), 'HH:mm');
//        var availableEnd = jq.fullCalendar.formatDate(new Date(event.availableEnd), 'HH:mm');
//        var availableStart = moment(event.availableEnd).format('HH:mm');
//
//        jq("#start_time_picker-display").val(availableStart);
//        jq("#end_time_picker-display").val(availableEnd);
//
//        jq("#start_time_picker-field").val(event.availableStart);
//        jq("#end_time_picker-field").val(event.availableEnd);

        //resets validation errors
        jq('#adjust-schedule-form').valid();

        dialog.show();
        return false;
    };

    adjustSurgeryScheduleDialog.createDialog = function () {

        dialog = emr.setupConfirmationDialog({
            selector: '#adjust-schedule-dialog',
            actions: {
                confirm: function () {
                    if(!jq('#adjust-schedule-form').valid())
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

        //validation
        jq('adjust-schedule-form').validate({
            ignore: [],
            rules: {
                "startTime": {
                    required: true,
                    date: true
                }
            },
            errorClass: "error",
            validClass: "",
            errorPlacement: function (error, element) {
                element.next().text(error.text());
            },
            highlight: function (element, errorClass, validClass) {
                jq(element).prev().children().first().addClass(errorClass);
                var selector = "#"+element.id.slice(0, - 6)+"-error";
                jq(selector).addClass(errorClass);
                jq(selector).show();
            },
            unhighlight: function (element, errorClass, validClass) {
                jq(element).prev().children().first().removeClass(errorClass);
                var selector = "#"+element.id.slice(0, - 6)+"-error";
                jq(selector).removeClass(errorClass);
                jq(selector).hide();
            }
        });
    };
}(window.adjustSurgeryScheduleDialog = window.adjustSurgeryScheduleDialog || {}, jQuery));

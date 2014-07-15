//Self-Executing Anonymous Function Pattern
//http://appendto.com/2010/10/how-good-c-habits-can-encourage-bad-javascript-habits-part-1/
(function (adjustSurgeryScheduleDialog, $, undefined) {
    //Private Property
    var dialog;
    var calEvent;
    var dateFormat = "DD-MM-YYYY HH:mm";
    var operationtheaters;

    //Public Method
    adjustSurgeryScheduleDialog.show = function (event) {
        calEvent = event;

        jq('#adjust-schedule-surgery').text(calEvent.title);
        jq('#adjust-surgery-form-location').val(calEvent.resource.name);
        jq('#is-ot-amvn vailable').prop("checked", calEvent.dateLocked);

        jq('#adjust_schedule_start_time-wrapper').datetimepicker('setStartDate', new Date());

        var availableStart = moment(event.start).format(dateFormat);
        jq("#adjust_schedule_start_time-display").val(availableStart);
        jq("#adjust_schedule_start_time-field").val(event.start);

        //reset validation errors
        jq('#adjust-schedule-form').valid();

        dialog.show();
        return false;
    };

    adjustSurgeryScheduleDialog.createDialog = function (ots) {
        operationtheaters = ots;

        dialog = emr.setupConfirmationDialog({
            selector: '#adjust-schedule-dialog',
            actions: {
                confirm: function () {
                    if(!jq('#adjust-schedule-form').valid())
                        return;

                    jq('#adjust-schedule-dialog' + ' .icon-spin').css('display', 'inline-block').parent().addClass('disabled');

                    var start = moment.utc(jq("#adjust_schedule_start_time-display").val(), dateFormat);

                    var params = {};
                    params.surgeryUuid = calEvent.surgeryUuid;
                    params.scheduledLocationUuid = operationtheaters[jq('#adjust-surgery-form-location').val()];
                    params.lockedDate = jq('#lock-date').is(':checked');
                    params.start = start.toISOString().replace("T", " "); //replacing needed for spring - otherwise time is not parsed
                    emr.getFragmentActionWithCallback('operationtheater', 'scheduling', 'adjustSurgerySchedule', params
                        , function (data) {
                            emr.successMessage(data.message);
                            dialog.close();
                            jq('#adjust-schedule-dialog' + ' .icon-spin').css('display', 'none').parent().removeClass('disabled');
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

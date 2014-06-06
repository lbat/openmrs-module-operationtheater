//TODO refactor

var availableTimesDialog = null;
var dateFormatted = "";
var calEvent;

function showAvailableTimesDialog (event) {
    calEvent = event;

    dateFormatted = jq.fullCalendar.formatDate(calEvent.start, 'yyyy-MM-dd');

    jq('#available-times-dialog-ot').text(calEvent.resource.name);
    jq('#available-times-dialog-date').text(dateFormatted);

    var start = calEvent.availableStart;
    var end = calEvent.availableEnd;
    jq('#is-ot-available').prop("checked", start != end);

    var startOfDay = new Date(calEvent.start);
    startOfDay.setHours(0);
    startOfDay.setMinutes(0);
    var tomorrow = new Date(calEvent.start);
    tomorrow.setDate(tomorrow.getDate()+1);
    var endOfDay = new Date(tomorrow-60000); //subtract one minute = 60000ms

    jq('#start_time_picker-wrapper').datetimepicker('setStartDate', startOfDay);
    jq('#start_time_picker-wrapper').datetimepicker('setEndDate', endOfDay);

    jq('#end_time_picker-wrapper').datetimepicker('setStartDate', startOfDay);
    jq('#end_time_picker-wrapper').datetimepicker('setEndDate', endOfDay);

    var availableStart = jq.fullCalendar.formatDate(new Date(calEvent.availableStart), 'HH:mm');
    var availableEnd = jq.fullCalendar.formatDate(new Date(calEvent.availableEnd), 'HH:mm');

    console.log(availableEnd);

    jq("#start_time_picker-display").val(availableStart);
    jq("#end_time_picker-display").val(availableEnd);

    jq("#start_time_picker-field").val(calEvent.availableStart);
    jq("#end_time_picker-field").val(calEvent.availableEnd);

    //resets validation errors
    requires("start_time_picker");
    requires("end_time_picker");

    availableTimesDialog.show();
    return false;
}

function createAvailableTimesDialog(patientId) {
    availableTimesDialog = emr.setupConfirmationDialog({
        selector: '#available-times-dialog',
        actions: {
            confirm: function() {
                if(!validate())
                    return;

                var params = {};
                params.locationUuid = calEvent.resource.uuid//"76d562c4-79d7-45ef-b1c4-8e15c2b01c41";
                params.available=jq('#is-ot-available').is(':checked');
                params.startTime = dateFormatted + " " + jq("#start_time_picker-display").val()+":00.0"; //not working on server side if seconds and milliseconds are missing (even with custom pattern)
                params.endTime = dateFormatted + " " + jq("#end_time_picker-display").val()+":00.0";
                emr.getFragmentActionWithCallback('operationtheater', 'scheduling', 'adjustAvailableTimes', params
                    , function(data) {
                        emr.successMessage("Updated available times"); //Todo message.properties
                        availableTimesDialog.close();
                        jq('#calendar').fullCalendar( 'refetchEvents' );
                    });
            },
            cancel: function() {
                availableTimesDialog.close();
            }
        }
    });

    //
    jq('#is-ot-available').change( function() {
        if(jq('#is-ot-available').is(':checked')){
            //enable
            emr.successMessage("enable");
            jq('#start_time_picker-display').attr("disabled",false);
            jq('#end_time_picker-display').attr("disabled",false);
        }else{
            //disable
            emr.successMessage("disable");
            jq('#start_time_picker-display').attr("disabled",true);
            jq('#end_time_picker-display').attr("disabled",true );
            clearFieldError(jq('#start_time_picker-display'), jq('#start_time_picker .field-error'));
            clearFieldError(jq('#end_time_picker-display'), jq('#end_time_picker .field-error'));
//            jq('#end_time_picker-wrapper .add-on').attr("disabled",true );
//            jq('#end_time_picker-wrapper .add-on i').attr("disabled",true );
            //FIXME disable small calendar icon too
        }
    });

    //add required field validation handlers
    jq('#start_time_picker-display').attr("onblur","javascript:requires('start_time_picker')");
    jq('#start_time_picker-display').attr("onChange","javascript:requires('start_time_picker')");

    jq('#end_time_picker-display').attr("onblur","javascript:requires('end_time_picker')");
    jq('#end_time_picker-display').attr("onChange","javascript:requires('end_time_picker')");

}

function setFieldError(el, errorEl, errorMessage) {
    el.className = 'illegalValue';
    errorEl.text(errorMessage);
    errorEl.show();
}


function clearFieldError(el, errorEl) {
    el.className = 'legalValue';
    errorEl.text("");
    errorEl.hide();
}

function requires(elId){
    var el = jq('#'+elId+"-display");
    var errorEl = jq('#'+elId+' .field-error');

    clearFieldError(el, errorEl);

    if(jq('#'+elId+"-field").val() == ''){

        setFieldError(el, errorEl, "This field is required");
        return true;
    }
    return false;
}

function validate(){
    var startTimeId = "start_time_picker";
    var endTimeId =  "end_time_picker";
    var startTimeEmpty = requires(startTimeId)
    var endTimeEmpty = requires(endTimeId);

    if(!jq('#is-ot-available').is(':checked')){
        return true;
    }

    if(!startTimeEmpty && !endTimeEmpty){
        var end = new Date(jq('#'+endTimeId+'-field').val());
        var start = new Date(jq('#'+startTimeId+'-field').val());

        console.log(start);
        console.log(end);

        if(start >= end){
            var el = jq('#'+endTimeId+"-display");
            var errorEl = jq('#'+endTimeId+' .field-error');

            var message = "start time must be before the end time";

            setFieldError(el, errorEl, message);
        }
        else{
            return true;
        }
    }

    return false;
}

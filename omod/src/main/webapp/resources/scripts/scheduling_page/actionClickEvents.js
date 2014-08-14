//Self-Executing Anonymous Function Pattern
//http://appendto.com/2010/10/how-good-c-habits-can-encourage-bad-javascript-habits-part-1/
(function (actionClickEvents, $, undefined) {
    //private properties
    var calResources;
    var getSolverStatusURL;
    var executeSchedulerURL;
    var scheduleEmergencyURL;

    //public methods
    actionClickEvents.add = function (resources) {
        calResources = resources;
        getSolverStatusURL = emr.fragmentActionLink("operationtheater", "scheduling", "getSolverStatus", null);
        executeSchedulerURL = emr.fragmentActionLink("operationtheater", "scheduling", "schedule", null);
        scheduleEmergencyURL = emr.fragmentActionLink("operationtheater", "scheduling", "scheduleEmergency", null);

        addEmergencyActionClickEvent();
        addSchedulerActionClickEvent();
        addShowFilterDialogClickEvent();
    };

    //private methods
    function addEmergencyActionClickEvent() {
        jq('#schedule-emergency').click(function () {
            jq.getJSON(scheduleEmergencyURL, null)
                .success(function (data) {
                    var message = "";
                    var clazz = "";
                    var icon = "";
                    if (data.waitingTime === 0) {
                        message = emrExt.message("operationtheater.scheduling.emergency.freeOperationTheaterFound", data.location);
                        clazz = "success";
                        icon = "icon-ok";
                    } else {
                        message = emrExt.message("operationtheater.scheduling.emergency.nextOtisAvailableInMinutes", data.location, data.waitingTime);
                        clazz = "warning";
                        icon = "icon-warning-sign"
                    }
                    jq("#emergency-note-container .note").attr("class", "note " + clazz);
                    jq("#emergency-note-container .medium").attr("class", icon + " medium");
                    jq("#emergency-note-container p").text(message);
                    jq('#emergency-note-container').show();
                    jq('#calendar').fullCalendar('refetchEvents');
                })
                .error(function (xhr, status, err) {
                    emr.handleError(xhr);
                })
        });
    }

    function addSchedulerActionClickEvent() {
        jq('#schedule-action').click(function () {
            jq.getJSON(executeSchedulerURL, null)
                .success(function (data) {
                    emr.successMessage(data.message);
                    jq('#wait-during-calculation').show();
                    pullSchedulingStatus = setInterval(function () {
                        jq.getJSON(getSolverStatusURL, null)
                            .success(function (data) {
                                if (data.message == "running") {
                                    return;
                                }
                                jq('#wait-during-calculation').hide();
                                emr.successMessage(data.message);
                                clearInterval(pullSchedulingStatus);
                                jq('#calendar').fullCalendar('refetchEvents');
                            })
                            .error(function (xhr, status, err) {
                                emr.handleError(xhr);
                                jq('#wait-during-calculation').hide();
                                clearInterval(pullSchedulingStatus);
                                jq('#calendar').fullCalendar('refetchEvents');
                            })
                    }, 1000);
                })
                .error(function (xhr, status, err) {
                    emr.handleError(xhr);
                })
        });
        //execute scheduler action
        var pullSchedulingStatus;
    }

    function addShowFilterDialogClickEvent() {
        //show filter dialog
        jq('#filter-action').click(function () {
            filterResourcesDialog.show(calResources);
        });
    }


}(window.actionClickEvents = window.actionClickEvents || {}, jQuery));

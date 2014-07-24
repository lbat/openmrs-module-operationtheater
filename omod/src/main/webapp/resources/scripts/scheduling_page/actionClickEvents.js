//Self-Executing Anonymous Function Pattern
//http://appendto.com/2010/10/how-good-c-habits-can-encourage-bad-javascript-habits-part-1/
(function (actionClickEvents, $, undefined) {
    //private properties
    var calResources;
    var getSolverStatusURL;
    var executeSchedulerURL;

    //public methods
    actionClickEvents.add = function (execSchedulerURL, solverStatusURL, resources) {
        calResources = resources;
        getSolverStatusURL = solverStatusURL;
        executeSchedulerURL = execSchedulerURL;

        addSchedulerActionClickEvent();
        addShowFilterDialogClickEvent();
    }

    //private methods
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

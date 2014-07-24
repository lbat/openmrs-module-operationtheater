<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeCss("referenceapplication", "referenceapplication.css")

    ui.includeCss("operationtheater", "bower_components/fullcalendar/fullcalendar.css")
    ui.includeJavascript("operationtheater", "bower_components/fullcalendar/fullcalendar.js")
    ui.includeJavascript("operationtheater", "scheduling_page/adjustAvailableTimesDialog.js")
    ui.includeJavascript("operationtheater", "scheduling_page/adjustSurgeryScheduleDialog.js")
    ui.includeJavascript("operationtheater", "scheduling_page/filterOperationTheatersInDailyView.js")
    ui.includeJavascript("operationtheater", "scheduling_page/calendar.js")
    ui.includeJavascript("operationtheater", "scheduling_page/actionClickEvents.js")
    ui.includeJavascript("operationtheater", "bower_components/validation/jquery.validate.js")

    ui.includeJavascript("uicommons", "emr.js")
    ui.includeJavascript("uicommons", "moment.min.js")
    ui.includeJavascript("uicommons", "datetimepicker/bootstrap-datetimepicker.min.js")
%>
<script type="text/javascript">

    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("operationtheater.scheduling.page.label")}"}
    ];

    jq(document).ready(function () {

        var calResources = [];
        var resourceLookUp = {};
        <% resources.eachWithIndex { it, i -> %>
        calResources.push({id: "${i+1}", name: "${ it.name }", uuid: "${ it.uuid }", selected: true});//, color: "black", textColor: "black"});
        resourceLookUp["${it.name}"] = "${it.uuid}";
        <% } %>

        //create dialogs
        availableTimesDialog.createDialog();
        adjustSurgeryScheduleDialog.createDialog(resourceLookUp);
        filterResourcesDialog.createDialog();

        //create calendar
        calendar.create(calResources, resourceLookUp, '${ ui.actionLink("operationtheater", "scheduling", "getEvents") }');

        //add action click events
        var executeSchedulerURL = '${ ui.actionLink("operationtheater", "scheduling", "schedule") }';
        var getSchedulerStatusURL = '${ ui.actionLink("operationtheater", "scheduling", "getSolverStatus") }';
        actionClickEvents.add(executeSchedulerURL, getSchedulerStatusURL, calResources);

        jq(window).resize(function () {
            calendar.resize();
        });
        setTimeout(function () {
            calendar.resize();
        }, 300);
    });


</script>
<style type='text/css'>
input.error {
    border: 2px solid;
    border-color: #ff6666;
}

body {
    max-width: 95%;
}

#calendar {
    width: 100%;
    height: 100%;
    margin: 0 auto;
    margin-top: 20px;
}

</style>

<h1>
    ${ui.message("operationtheater.scheduling.page.heading")}
</h1>

<!-- scheduling note container -->
<div id="wait-during-calculation" class="note-container" style="display: none">
    <div class="note warning">
        <div class="text">
            <i class="icon-spinner icon-spin medium"></i>
            <p>Please wait while the optimal scheduling is calculated</p>
        </div>
    </div>
</div>

<!-- action button with dropdown menu -->
<div class="actions dropdown" style="top:0px;margin-bottom: 10px">
    <span class="dropdown-name"><i class="icon-cog"></i>Actions<i class="icon-sort-down"></i></span>
    <ul>
        <li>
            <a href="#" id="schedule-action"><i class="icon-calendar"></i>Schedule</a>
        </li>

        <li>
            <a href="#" id="filter-action"><i class="icon-filter"></i>Filter</a>
        </li>
    </ul>
</div>

<!-- calendar container -->
<div id='calendar'></div>

<!-- DIALOGS -->

<!-- filter daily resources dialog -->
<div id="filter-resources-dialog" class="dialog simplemodal-data" style="">
    <div class="dialog-header">
        <i class="icon-filter"></i>

        <h3>Filter Operation Theaters</h3>
    </div>

    <div class="dialog-content">
        <p class="dialog-instructions">Please select operation theaters that should be displayed</p>
        <ul>
            <!-- TODO add select all, deselect all link -->
            <form id="filter-resources-form" class="simple-form-ui"></form>
        </ul>

        <button class="confirm right">${ui.message("emr.okay")}</button>
        <button class="cancel">${ui.message("emr.cancel")}</button>
    </div>
</div>

<!-- available times dialog -->
<div id="available-times-dialog" class="dialog simplemodal-data" style="">
    <div class="dialog-header">
        <i class="icon-time"></i>

        <h3>Operation Theater available times</h3>
    </div>

    <div class="dialog-content">
        <p class="dialog-instructions">Please enter and confirm the available times:</p>
        <ul>
            <li class="info">
                <span>Operation Theater</span>
                <h5 id="available-times-dialog-ot">OT 1</h5>
            </li>
            <li class="info">
                <span>Date</span>
                <h5 id="available-times-dialog-date" data-bind="text: text">01.06.2014</h5>
            </li>

            <form id="available-time-form" class="simple-form-ui">
                <p>
                    <input id="is-ot-available" checked="checked" type="checkbox"></input>
                    <label>OT available for this day</label>
                </p>

                <p>
                    <label for="start_time_picker-display">start time:</label>
                    <!--<input id="startTime" name="startTime" placeholder="e.g. 08:00" type="text"></input>-->
                    ${ui.includeFragment("operationtheater", "field/datetimepicker", [
                            id           : 'start_time_picker',
                            label        : '',
                            formFieldName: 'startTimePicker',
                            useTime      : true])}
                </p>

                <p>
                    <label for="end_time_picker-display">end time:</label>
                    <!--<input id="endTime" name="endTime" placeholder="e.g. 17:00" type="text"></input>-->
                    ${ui.includeFragment("operationtheater", "field/datetimepicker", [
                            id           : 'end_time_picker',
                            label        : '',
                            formFieldName: 'endTimePicker',
                            useTime      : true])}
                </p>

            </form>
        </ul>

        <button class="confirm right">${ui.message("emr.save")}
            <i class="icon-spinner icon-spin icon-2x" style="display: none; margin-left: 10px;"></i></button>
        <button class="cancel">${ui.message("emr.cancel")}</button>
    </div>
</div>

<!-- adjust surgery schedule dialog -->
<div id="adjust-schedule-dialog" class="dialog simplemodal-data" style="">
    <div class="dialog-header">
        <i class="icon-time"></i>

        <h3>Adjust surgery schedule</h3>
    </div>

    <div class="dialog-content">
        <p class="dialog-instructions">Please adjust and confirm the schedule for the selected surgery</p>
        <ul>
            <li class="info">
                <span id="">Surgery</span>
                <h5 id="adjust-schedule-surgery"></h5>
            </li>

            <form id="adjust-schedule-form" class="simple-form-ui">

                <p>
                    <label for="adjust-surgery-form-location">Operation Theater:</label>
                    <span class="select-arrow">
                        <select id="adjust-surgery-form-location">
                            <option value="null">not specified</option>
                            <% resources.eachWithIndex { it, i -> %>
                            <option value="${it.name}">${it.name}</option>
                            <% } %>
                        </select>
                    </span>
                </p>

                <p>
                    <label for="adjust_schedule_start_time-display">start time:</label>
                    ${ui.includeFragment("operationtheater", "field/datetimepicker", [
                            id           : 'adjust_schedule_start_time',
                            label        : '',
                            formFieldName: 'startTime',
                            useTime      : true,
                            format: "dd-mm-yyyy hh:ii",
                            startView    : 2])}
                </p>

                <p>
                    <input id="lock-date" type="checkbox"></input>
                    <label>Lock date (scheduler will not change it)</label>
                </p>

            </form>
        </ul>


        <button class="confirm right">${ui.message("emr.save")}
            <i class="icon-spinner icon-spin icon-2x" style="display: none; margin-left: 10px;"></i></button>
        <button class="cancel">${ui.message("emr.cancel")}</button>
    </div>
</div>

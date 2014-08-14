<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeCss("referenceapplication", "referenceapplication.css")

    ui.includeCss("operationtheater", "bower_components/fullcalendar/fullcalendar.css")
    ui.includeJavascript("operationtheater", "bower_components/fullcalendar/fullcalendar.js")
    ui.includeJavascript("operationtheater", "scheduling_page/adjustAvailableTimesDialog.js")
    ui.includeJavascript("operationtheater", "scheduling_page/surgeryDialog.js")
    ui.includeJavascript("operationtheater", "scheduling_page/filterOperationTheatersInDailyView.js")
    ui.includeJavascript("operationtheater", "scheduling_page/calendar.js")
    ui.includeJavascript("operationtheater", "scheduling_page/actionClickEvents.js")
    ui.includeJavascript("operationtheater", "bower_components/validation/jquery.validate.js")
    ui.includeJavascript("operationtheater", "emrExt.js")

    ui.includeJavascript("uicommons", "emr.js")
    ui.includeJavascript("uicommons", "moment.min.js")
    ui.includeJavascript("uicommons", "datetimepicker/bootstrap-datetimepicker.min.js")
%>

<%=ui.includeFragment("appui", "messages", [codes: [
        "operationtheater.validation.error.greaterThan",
        "operationtheater.scheduling.page.availableTimesDialog.label.startTime",
        "operationtheater.scheduling.emergency.freeOperationTheaterFound",
        "operationtheater.scheduling.emergency.nextOtisAvailableInMinutes",
].flatten()
])%>

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
        availableTimesDialog.create();
        surgeryDialog.create(resourceLookUp);
        filterResourcesDialog.create();

        //create calendar
        calendar.create(calResources, resourceLookUp);

        //add action click events
        actionClickEvents.add(calResources);

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

            <p>${ui.message("operationtheater.scheduling.page.waitWhileSchedulingIsCalculated")}</p>
        </div>
    </div>
</div>
<!-- emergency note container -->
<div id="emergency-note-container" class="note-container" style="display: none">
    <div class="note warning">
        <div class="text">
            <i class="icon-warning-sign medium"></i>

            <p>${ui.message("operationtheater.scheduling.page.waitWhileSchedulingIsCalculated")}</p>
        </div>

        <div class="close-icon"><i class="icon-remove" onClick="javascript:jq('#emergency-note-container').hide();"></i>
        </div>
    </div>
</div>

<!-- action button with dropdown menu -->
<div class="actions dropdown" style="top:0px;margin-bottom: 10px">
    <span class="dropdown-name"><i class="icon-cog"></i>Actions<i class="icon-sort-down"></i></span>
    <ul>
        <li>
            <a href="#" id="schedule-emergency"><i
                    class="icon-ambulance"></i>${ui.message("operationtheater.scheduling.page.action.emergency")}</a>
        </li>

        <li>
            <a href="#" id="schedule-action"><i
                    class="icon-calendar"></i>${ui.message("operationtheater.scheduling.page.action.schedule")}</a>
        </li>

        <li>
            <a href="#" id="filter-action"><i
                    class="icon-filter"></i>${ui.message("operationtheater.scheduling.page.action.filter")}</a>
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

        <h3>${ui.message("operationtheater.scheduling.page.filterResourcesDialog.heading")}</h3>
    </div>

    <div class="dialog-content">
        <p class="dialog-instructions">${ui.message("operationtheater.scheduling.page.filterResourcesDialog.instructions")}</p>
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

        <h3>${ui.message("operationtheater.scheduling.page.availableTimesDialog.heading")}</h3>
    </div>

    <div class="dialog-content">
        <p class="dialog-instructions">${ui.message("operationtheater.scheduling.page.availableTimesDialog.instructions")}</p>
        <ul>
            <li class="info">
                <span>${ui.message("operationtheater.scheduling.page.availableTimesDialog.info.location")}</span>
                <h5 id="available-times-dialog-ot">OT 1</h5>
            </li>
            <li class="info">
                <span>${ui.message("operationtheater.scheduling.page.availableTimesDialog.info.date")}</span>
                <h5 id="available-times-dialog-date" data-bind="text: text">01.06.2014</h5>
            </li>

            <form id="available-time-form" class="simple-form-ui">
                <p>
                    <input id="is-ot-available" checked="checked" type="checkbox"></input>
                    <label>${ui.message("operationtheater.scheduling.page.availableTimesDialog.label.available")}</label>
                </p>

                <p>
                    <label for="start_time_picker-display">${ui.message("operationtheater.scheduling.page.availableTimesDialog.label.startTime")}</label>
                    <!--<input id="startTime" name="startTime" placeholder="e.g. 08:00" type="text"></input>-->
                    ${ui.includeFragment("operationtheater", "field/datetimepicker", [
                            id           : 'start_time_picker',
                            label        : '',
                            formFieldName: 'startTimePicker',
                            useTime      : true])}
                </p>

                <p>
                    <label for="end_time_picker-display">${ui.message("operationtheater.scheduling.page.availableTimesDialog.label.endTime")}</label>
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

        <h3>${ui.message("operationtheater.scheduling.page.adjustScheduleDialog.heading")}</h3>
    </div>

    <div class="dialog-content">
        <p class="dialog-instructions">${ui.message("operationtheater.scheduling.page.adjustScheduleDialog.instructions")}</p>
        <ul>
            <li class="info">
                <span id="">${ui.message("operationtheater.scheduling.page.adjustScheduleDialog.info")}</span>
                <a href="javascript:surgeryDialog.openSurgery('${ui.pageLink("operationtheater", "surgery")}');"><h5
                        style="color:#007fff" id="adjust-schedule-surgery"></h5></a>
            </li>

            <form id="adjust-schedule-form" class="simple-form-ui">
                <p>
                    <label for="adjust-surgery-form-location">${ui.message("operationtheater.scheduling.page.adjustScheduleDialog.label.location")}</label>
                    <span class="select-arrow">
                        <select id="adjust-surgery-form-location">
                            <option value="null">${ui.message("operationtheater.notSpecified")}</option>
                            <% resources.eachWithIndex { it, i -> %>
                            <option value="${it.name}">${it.name}</option>
                            <% } %>
                        </select>
                    </span>
                </p>

                <p>
                    <label for="adjust_schedule_start_time-display">${ui.message("operationtheater.scheduling.page.adjustScheduleDialog.label.startTime")}</label>
                    ${ui.includeFragment("operationtheater", "field/datetimepicker", [
                            id           : 'adjust_schedule_start_time',
                            label        : '',
                            formFieldName: 'startTime',
                            useTime      : true,
                            format       : "dd-mm-yyyy hh:ii",
                            startView    : 2])}
                </p>

                <p>
                    <input id="lock-date" type="checkbox"></input>
                    <label>${ui.message("operationtheater.scheduling.page.adjustScheduleDialog.label.lockDate")}</label>
                </p>
            </form>
        </ul>

        <button class="confirm right">${ui.message("emr.save")}
            <i class="icon-spinner icon-spin icon-2x" style="display: none; margin-left: 10px;"></i></button>
        <button class="cancel">${ui.message("emr.cancel")}</button>
    </div>
</div>

<!-- open surgery record -->
<div id="open-surgery-dialog" class="dialog simplemodal-data" style="">
    <div class="dialog-header">
        <i class="icon-link"></i>

        <h3>${ui.message("operationtheater.scheduling.page.openSurgeryDialog.heading")}</h3>
    </div>

    <div class="dialog-content">
        <p class="dialog-instructions">${ui.message("operationtheater.scheduling.page.openSurgeryDialog.question")}</p>

        <button class="confirm right">${ui.message("emr.yes")}
            <button class="cancel">${ui.message("emr.cancel")}</button>
    </div>
</div>

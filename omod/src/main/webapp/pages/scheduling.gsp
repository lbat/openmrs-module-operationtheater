<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeCss("referenceapplication", "referenceapplication.css")

    ui.includeCss("operationtheater", "bower_components/fullcalendar/fullcalendar.css")
    ui.includeJavascript("operationtheater", "bower_components/fullcalendar/fullcalendar.js")
    ui.includeJavascript("operationtheater", "scheduling_page/adjustAvailableTimesDialog.js")
    ui.includeJavascript("operationtheater", "scheduling_page/adjustSurgeryScheduleDialog.js")
    ui.includeJavascript("operationtheater", "scheduling_page/filterOperationTheatersInDailyView.js")
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
        var operationTheaters = {};
        <% resources.eachWithIndex { it, i -> %>
        calResources.push({id: "${i+1}", name: "${ it.name }", uuid: "${ it.uuid }", selected: true});//, color: "black", textColor: "black"});
        operationTheaters["${it.name}"] = "${it.uuid}";
        <% } %>

        //create dialogs
        availableTimesDialog.createDialog();
        adjustSurgeryScheduleDialog.createDialog(operationTheaters);
        filterResourcesDialog.createDialog();

        jq('#calendar').fullCalendar({
            height: 3000,
            header: {
                left: 'prev,next today, resourceDay, agendaWeek, resourceWeek',
                center: 'title',
                right: ''
            },
            defaultView: "resourceDay",
            allDaySlot: false,
            editable: true,
            eventDurationEditable: false,
            eventStartEditable: true,
            eventAfterRender: function (event, element, view) {
                if (event.annotation) {
                    schedule = jq('.highlighting');
                    width = jq('.fc-widget-content').width();
                    width = width + 'px';
                    jq(element).css('width', width);
                    jq(element).css('opacity', '0.4');
                } else if (event.dateLocked) {
                    jq(element).addClass("icon-lock");
                }
            },
            eventClick: function (calEvent, jsEvent, view) {
                if (calEvent.annotation) {
                    availableTimesDialog.show(calEvent);
                } else {
                    //FIXME remove this hack - just for demonstration purpose
                    //window.location.href = '${ui.pageLink("operationtheater", "surgery", [surgeryId:2, patientId:6])}';
                    adjustSurgeryScheduleDialog.show(calEvent);
                }
            },
            resources: calResources,
            events: function (start, end, callback) {
                var params = {};
                params.start = jq.fullCalendar.formatDate(start, 'yyyy-MM-dd');
                params.end = jq.fullCalendar.formatDate(end, 'yyyy-MM-dd');
                params.resources = Object.keys(operationTheaters);
                jq.ajaxSetup({ scriptCharset: "utf-8", contentType: "application/json; charset=utf-8"});
                jq.getJSON('${ ui.actionLink("operationtheater", "scheduling", "getEvents") }', jq.param(params, true))
                        .success(function (data) {
                            callback(data);
                        })
                        .error(function (xhr, status, err) {
                            emr.handleError(xhr);
                        })
            }
        });

        resizeFullCalendar();
        jq(window).resize(function () {
            resizeFullCalendar();
        });

        //add datepicker button
        jq('span:contains(today)').parents('td').append('<span class="fc-header-space"></span>' +
                '<span id="datepicker-button" class="button" href="#" onclick="return false">' +
                '   <i class ="icon-calendar"></i>' +
                '' +
                '</span>' +
                '<span">' +
                '   <input id="datetimepicker" style="visibility: hidden; height: 1px;width: 1px; padding:0px;" size="16" type="text" value="2012-06-15" readonly>' +
                '</span>');


        jq('td.fc-header-right').empty();
        jq('td.fc-header-right').append('<div class="dropdown" style="margin-right:0px; top:0px">' +
                '<span class="dropdown-name">' +
                '    <i class="icon-cog"></i>' +
                '    Actions' +
                '    <i class="icon-sort-down"></i>' +
                '</span>' +
                '<ul>' +
                '    <li>' +
                '        <a href="#" id="schedule-action" ><i class="icon-calendar"></i>Schedule</a>' +
                '    </li>' +
                '    <li>' +
                '        <a href="#" id="filter-action"><i class="icon-filter"></i>Filter</a>' +
                '    </li>' +
                '</ul>' +
                '</div>');

        //execute scheduler
        var pullSchedulingStatus;
        jq('#schedule-action').click(function () {
            jq.getJSON('${ ui.actionLink("operationtheater", "scheduling", "schedule") }', null)
                    .success(function (data) {
                        emr.successMessage(data.message);
                        jq('#wait-during-calculation').show();
                        pullSchedulingStatus = setInterval(function () {
                            jq.getJSON('${ ui.actionLink("operationtheater", "scheduling", "getSolverStatus") }', null)
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

        //show filter dialog
        jq('#filter-action').click(function () {
            filterResourcesDialog.show(calResources);
        });


        //attach datetimepicker to calendar button
        jq(function () {
            var button = jq("#datepicker-button");

            jq("#datetimepicker").datetimepicker({
                format: 'yyyy-mm-dd',
                minView: 2,
                pickerPosition: "bottom-left",
                autoClose: true,
                language: "${org.openmrs.api.context.Context.getLocale()}",
                todayHighlight: true
            });

            jq("#datetimepicker").datetimepicker().on('changeDate', function (ev) {
                console.log(ev.date);
                jq('#datetimepicker').datetimepicker('hide');
                jq("#calendar").fullCalendar('gotoDate', ev.date);
            });

            button.click(function () {
                console.log("dim: " + left + " - " + top);
                var date = jq('#calendar').fullCalendar('getDate');
                var dateStr = moment.utc(date).format("YYYY-mm-DD");
                jq('#datetimepicker').val(dateStr);
                jq('#datetimepicker').datetimepicker('show');

                var datetimepicker = jq('div:visible.datetimepicker');
                var left = button.offset().left + button.outerWidth() / 2 - datetimepicker.width() + 15;
                var top = button.offset().top + button.outerHeight(true);
                datetimepicker.css({top: top, left: left});
            });

            //customize fullcalendar design
//        jq("span.fc-button").addClass("button");

            //remove border from header table
            jq("table.fc-header tr").css("border-collapse", "collapse");
            jq("table.fc-header tr").css("border", "0");
            jq("table.fc-header td").css("border-collapse", "collapse");
            jq("table.fc-header td").css("border", "0");
        });
    });

    //TODO offset not correct
    function resizeFullCalendar() {
        var offset = jq("#calendar").offset();
        var margin = parseInt(jq(document.body).css("margin-top")) + parseInt(jq(document.body).css("margin-bottom"));
        console.log(offset.top);
        console.log(margin);

        var height = jq(window).height() * 0.95 - offset.top;
        jq('#calendar').fullCalendar('option', 'height', height);
    }

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
}

</style>

<h1>
    ${ui.message("operationtheater.scheduling.page.heading")}
</h1>

<div id="wait-during-calculation" class="note-container" style="display: none">
    <div class="note warning">
        <div class="text">
            <i class="icon-spinner icon-spin medium"></i>

            <p>Please wait while the optimal scheduling is calculated</p>

        </div>
    </div>
</div>


<div id='calendar'></div>

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
                    <input id="lock-date" checked="checked" type="checkbox"></input>
                    <label>Lock date (scheduler will not change it)</label>
                </p>

            </form>
        </ul>


        <button class="confirm right">${ui.message("emr.save")}
            <i class="icon-spinner icon-spin icon-2x" style="display: none; margin-left: 10px;"></i></button>
        <button class="cancel">${ui.message("emr.cancel")}</button>
    </div>
</div>

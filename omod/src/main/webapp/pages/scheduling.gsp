<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeCss("referenceapplication", "referenceapplication.css")

    ui.includeCss("operationtheater", "bower_components/fullcalendar/fullcalendar.css")
    ui.includeJavascript("operationtheater", "bower_components/fullcalendar/fullcalendar.js")

    ui.includeJavascript("operationtheater", "schedulingAvailableTimesDialog.js")
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
        var operationTheaters = [];
        <% resources.eachWithIndex { it, i -> %>
        calResources.push({id: "${i+1}", name: "${ it.name }", uuid: "${ it.uuid }"});//, color: "black", textColor: "black"});
        operationTheaters.push("${it.name}")
        <% } %>

        availableTimesDialog.createDialog();

        jq('#calendar').fullCalendar({
            header: {
                left: 'prev,next today',
                center: 'title',
                right: 'This is a button <a href="javascript:void(0)" onClick="addRes();">Add resource</a>'
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
                }
            },
            eventClick: function (calEvent, jsEvent, view) {
                if (calEvent.annotation) {
                    availableTimesDialog.show(calEvent);
                }
            },
            resources: calResources,
            events: function (start, end, callback) {
                var params = {};
                params.start = jq.fullCalendar.formatDate(start, 'yyyy-MM-dd');
                params.end = jq.fullCalendar.formatDate(end, 'yyyy-MM-dd');
                params.resources = operationTheaters;
                jq.ajaxSetup({ scriptCharset: "utf-8", contentType: "application/json; charset=utf-8"});
                jq.getJSON('${ ui.actionLink("operationtheater", "scheduling", "getEvents") }', jq.param(params, true))
                        .success(function (data) {
                            callback(data);
                        })
                        .error(function (xhr, status, err) {
                            emr.handleError(xhr);
                            //emr.errorAlert("AJAX error " + err, null); //TODO message.properties
                        })
            }
//        events: [{"title":"","start":"2014-06-03 05:00","end":"2014-06-03 15:00","resourceId":1,"allDay":false, annotation:true, "resourceName":"OT 1"}]
        });

        //customize fullcalendars design
//        jq("span.fc-button").addClass("button");
        //add datepicker button
        jq('span:contains(today)').parents('td').append('<span class="fc-header-space"></span><span id="datepicker-link" class="button" href="#" onclick="return false"><i class ="icon-calendar"></i></span>');
//        jq('span:contains(today)').parents ('td').append ('<div>'+
//                '<form id="form_datetime">'+
//                '<input width="1px" hidden="true" type="text" value="" style="display: none">'+
//                '<span class="button"><i class="icon-calendar"></i></span>'+
//        '</form>'+
//        '</div>');
//        jq("#form_datetime").datetimepicker({
//            format: "dd MM yyyy - hh:ii",
//            autoclose: true,
//            todayBtn: true,
//            pickerPosition: "bottom-left"
////        showOn: 'button'
//        });

//        jq("#date-picker").datepicker({
//            dateFormat: 'dd-mm-yy',
//            clickInput: true,
//            showOn: 'button',
//            buttonImage: 'icon-calendar',
//            buttonImageOnly: true,
//
//            onSelect: function(dateText, inst) {
//                // I don't know if this conversion works, maybe a string parsing is needed
//                var date = new Date(dateText);
//                jq('#calendar').fullCalendar('gotoDate', date.getFullYear(), date.getMonth(), date.getDate());
//            }
//        });

        jq('td.fc-header-right').empty();
        jq('td.fc-header-right').append('<div class="dropdown" style="margin-right:0px; top:0px">' +
                '<span class="dropdown-name">' +
                '    <i class="icon-cog"></i>' +
                '    Actions' +
                '    <i class="icon-sort-down"></i>' +
                '</span>' +
                '<ul>' +
                '    <li>' +
                '        <a href="#"><i class="icon-filter"></i>Filter</a>' +
                '    </li>' +
                '</ul>' +
                '</div>');


        //remove border from header table
        jq("table.fc-header tr").css("border-collapse", "collapse");
        jq("table.fc-header tr").css("border", "0");
        jq("table.fc-header td").css("border-collapse", "collapse");
        jq("table.fc-header td").css("border", "0");
    });

</script>
<style type='text/css'>

#calendar {
    width: 100%;
    margin: 0 auto;
}

</style>

<h1>
    ${ui.message("operationtheater.scheduling.page.heading")}
</h1>

<div id='calendar'></div>

<div id='loading' style='display:none'>loading...</div>

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
                    ${ui.includeFragment("operationtheater", "datetimepicker", [id: 'start_time_picker', label: '', formFieldName: 'start time picker', useTime: true])}
                </p>

                <p>
                    <label for="end_time_picker-display">end time:</label>
                    <!--<input id="endTime" name="endTime" placeholder="e.g. 17:00" type="text"></input>-->
                    ${ui.includeFragment("operationtheater", "datetimepicker", [id: 'end_time_picker', label: '', formFieldName: 'end time picker', useTime: true])}
                </p>

            </form>
        </ul>

        <button class="confirm right">Confirm</button>
        <button class="cancel">Cancel</button>
    </div>
</div>

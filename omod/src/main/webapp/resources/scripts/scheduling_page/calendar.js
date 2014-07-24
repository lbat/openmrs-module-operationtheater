//Self-Executing Anonymous Function Pattern
//http://appendto.com/2010/10/how-good-c-habits-can-encourage-bad-javascript-habits-part-1/
(function (calendar, $, undefined) {
    //private properties
    var calResources;
    var resourceLookUp;
    var getEventsURL;
    var events;
    var loadFromServer = true;
    var dailyView = true;
    var lastOTInWeeklyView;

    //public methods
    calendar.create = function (resources, lookUp, eventURL) {
        calResources = resources;
        resourceLookUp = lookUp;
        lastOTInWeeklyView = calResources[0];
        getEventsURL = eventURL;

        createFullCalendar();
        customizeDesign();

        addDatePickerButton();
        addWeekViewResourceSelector();
    }

    calendar.resize = function () {
        //TODO offset not correct
        var offset = jq("#calendar").offset();
        var margin = parseInt(jq(document.body).css("margin-top")) + parseInt(jq(document.body).css("margin-bottom"));
        var height = jq(window).height() * 0.95 - offset.top - margin;
        jq('#calendar').fullCalendar('option', 'height', height);
    };

    //private methods
    function createFullCalendar() {
        jq('#calendar').fullCalendar({
            height: 3000,
            header: {
                left: 'prev,next today',
                center: 'title',
                right: 'resourceDay, agendaWeek'
            },
            defaultView: "resourceDay",
            allDaySlot: false,
            editable: true,
            eventDurationEditable: false,
            eventStartEditable: true,
            viewDisplay: function (view, element) { //viewRender in later versions
                if (view.name == "resourceDay" && !dailyView) {
                    dailyView = true;
                    jq('#select-weekly-resource-form').hide();
                    loadFromServer = false;
                    jq('#calendar').fullCalendar('refetchEvents');
                    calendar.resize();
                } else if (view.name != "resourceDay" && dailyView) {
                    dailyView = false;
                    jq('#select-weekly-resource-form').show();
                    jq('#calendar').fullCalendar('refetchEvents');
                    calendar.resize();
                }
            },
            eventAfterRender: function (event, element, view) {
                if (event.annotation && view.name == "resourceDay") {
                    schedule = jq('.highlighting');
                    width = jq('.fc-widget-content').width();
                    width = width + 'px';
                    jq(element).css('width', width);
                }
                if (event.annotation) {
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
                if (!loadFromServer && dailyView) {
                    loadFromServer = true;
                    callback(events);
                    return;
                }

                var params = {};
                params.start = jq.fullCalendar.formatDate(start, 'yyyy-MM-dd');
                params.end = jq.fullCalendar.formatDate(end, 'yyyy-MM-dd');
                params.resources = Object.keys(resourceLookUp);
                jq.ajaxSetup({ scriptCharset: "utf-8", contentType: "application/json; charset=utf-8"});
                jq.getJSON(getEventsURL, jq.param(params, true))
                    .success(function (data) {
                        events = data;
                        if (!dailyView) {
                            var filteredEvents = jq.grep(events, function (e) {
                                return e.resourceId == lastOTInWeeklyView.id
                            });
                            callback(filteredEvents);
                        } else {
                            callback(events);
                        }
                    })
                    .error(function (xhr, status, err) {
                        emr.handleError(xhr);
                    })
            }
        });
    }

    function customizeDesign() {
        //customize fullcalendar design
//        jq("span.fc-button").addClass("button");

        //remove border from header table
        jq("table.fc-header tr").css("border-collapse", "collapse");
        jq("table.fc-header tr").css("border", "0");
        jq("table.fc-header td").css("border-collapse", "collapse");
        jq("table.fc-header td").css("border", "0");
    }

    function addDatePickerButton() {
        //add datepicker button
        jq('span:contains(today)').parents('td').append('<span class="fc-header-space"></span>' +
            '<span id="datepicker-button" class="button" href="#" onclick="return false">' +
            '   <i class ="icon-calendar"></i>' +
            '</span>' +
            '<span">' +
            '   <input id="datetimepicker" style="visibility: hidden; height: 1px;width: 1px; padding:0px;" size="16" type="text" value="2012-06-15" readonly>' +
            '</span>');

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
        });
    }

    function addWeekViewResourceSelector() {
        //add week view resource selector
        var options = "";
        for (i in calResources) {
            options += '<option value="' + calResources[i].name + '">' + calResources[i].name + '</option>';
        }
        jq('.fc-header-title').parents('td').append('<form id="select-weekly-resource-form" style="margin-bottom: 10px; display: none"><span class="select-arrow">' +
            '       <select id="select-weekly-resource">' +
            options +
            '   </select>' +
            '</span></form>');

        //add change listener to week view resource selector
        jq('#select-weekly-resource').change(function () {
            lastOTInWeeklyView = jq.grep(calResources, function (e) {
                return e.name === jq('#select-weekly-resource').val()
            })[0];
            loadFromServer = false;
            jq('#calendar').fullCalendar("refetchEvents");
            jq('#calendar').fullCalendar('removeEvents', function (event) {
                return event.resource.id != lastOTInWeeklyView.id;
            });
        });
    }

}(window.calendar = window.calendar || {}, jQuery));

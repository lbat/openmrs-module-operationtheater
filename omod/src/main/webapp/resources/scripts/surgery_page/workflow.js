(function (workflow, $, undefined) {

    var surgeryUuid;

    workflow.init = function (surgeryUuid_) {
        surgeryUuid = surgeryUuid_;

        $('#startSurgeryButton').click(function () {
            if (!jq('#startSurgeryButton').hasClass("disabled")) {
                workflow.startSurgery();
            }
        });
        $('#finishSurgeryButton').click(function () {
            if (!jq('#finishSurgeryButton').hasClass("disabled")) {
                workflow.finishSurgery();
            }
        });

        getDataFromServer();
    };

    workflow.startSurgery = function () {
        emr.getFragmentActionWithCallback("operationtheater", "surgery", "startSurgery", {surgery: surgeryUuid}
            , function (data) {
                emr.successMessage(data.message);
                getDataFromServer();
            }, function (err) {
                emr.handleError(err); //TODO server errors (StatusCode 500) are not handled - only if response was created from FailureResult
            }
        );
    };

    workflow.finishSurgery = function () {
        emr.getFragmentActionWithCallback("operationtheater", "surgery", "finishSurgery", {surgery: surgeryUuid}
            , function (data) {
                emr.successMessage(data.message);
                getDataFromServer();
            }, function (err) {
                emr.handleError(err); //TODO server errors (StatusCode 500) are not handled - only if response was created from FailureResult
            }
        );
    };

    workflow.getDataFromServer = function () {
        emr.getFragmentActionWithCallback("operationtheater", "surgery", "getSurgeryTimes", {surgery: surgeryUuid}
            , function (data) {
                insertDataIntoTable(data);
            }, function (err) {
                emr.handleError(err); //TODO server errors (StatusCode 500) are not handled - only if response was created from FailureResult
            }
        );
    }

    function insertDataIntoTable(data) {
        var html = "";
        for (i in data) {
            html +=
                '<tr>' +
                '   <td>' + data[i].displayName + '</td>' +
                '   <td>' + data[i].dateTimeStr + '</td>' +
                '</tr>';
            if (data[i].type === "STARTED") {
                jq('#startSurgeryButton').addClass('disabled');
            }
            if (data[i].type === "FINISHED") {
                jq('#finishSurgeryButton').addClass('disabled');
            }
        }
        if (html === "") {
            html = '<tr><td>' + emr.message('uicommons.dataTable.emptyTable') + '</td><td></td></tr>'
        }

        jq("#timestamp-table tbody").html(html);
    }

}(window.workflow = window.workflow || {}, jQuery));

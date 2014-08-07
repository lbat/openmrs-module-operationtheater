//Self-Executing Anonymous Function Pattern
//http://appendto.com/2010/10/how-good-c-habits-can-encourage-bad-javascript-habits-part-1/
(function (surgicalTeam, $, undefined) {

    var surgeryUuid;

    surgicalTeam.init = function (surgeryUuid_, providerMap, providerOptions) {
        surgeryUuid = surgeryUuid_;

        jq('#addProviderTextfield-field').typeahead(providerOptions);

        jq('#addProviderButton').click(function () {
            surgicalTeam.add(providerMap[jq('#addProviderTextfield-field').val()]);
        });
    };

    surgicalTeam.get = function () {
        emr.getFragmentActionWithCallback("operationtheater", "surgery", "getSurgicalTeam", {surgery: surgeryUuid}
            , function (data) {
                insertDataIntoTable(data);
            }, function (err) {
                emr.handleError(err); //TODO server errors (StatusCode 500) are not handled - only if response was created from FailureResult
            }
        );
    };

    surgicalTeam.add = function (providerUuid) {
        if (!jq('#addProviderTextfield-field').valid()) {
            return;
        }

        emr.getFragmentActionWithCallback("operationtheater", "surgery", "addProviderToSurgicalTeam",
            {surgery: surgeryUuid, provider: providerUuid}
            , function (data) {
                //Todo remove from typeahead options
                emr.successMessage(data.message);
                jq('#addProviderTextfield-field').val('');
                surgicalTeam.get();
            }, function (err) {
                emr.handleError(err);
            }
        );
    };

    surgicalTeam.remove = function (providerUuid) {
        emr.getFragmentActionWithCallback("operationtheater", "surgery", "removeProviderFromSurgicalTeam",
            {surgery: surgeryUuid, provider: providerUuid}
            , function (data) {
                //Todo add to typeahead options
                emr.successMessage(data.message);
                surgicalTeam.get();
            }, function (err) {
                emr.handleError(err);
            }
        );
    };

    function insertDataIntoTable(data) {
        var html = "";
        for (i in data) {
            html +=
                '<tr>' +
                '   <td>' + data[i].name + '</td>' +
                '   <td class="align-center">' +
                '       <span>' +
                '           <i class="delete-item icon-remove"' +
                '               onClick="javascript:surgicalTeam.remove(\'' + data[i].uuid + '\');"' +
                '               title="' + emr.message("coreapps.delete") + '"></i>' +
                '       </span>' +
                '   </td>' +
                '</tr>';
        }
        jq("#surgical-team-table tbody").html(html);
    }

}(window.surgicalTeam = window.surgicalTeam || {}, jQuery));

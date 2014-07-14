// TODO redo using angular?

$(function () {
    $(document).on('click', '.deleteElement', function (event) {
        var id = $(event.target).attr("data-id");
        createDeleteDialog(id, $(this));
        showDeleteDialog();
    });

    $(document).on('click', '.editElement', function (event) {
        emr.navigateTo({
            provider: 'operationtheater',
            page: 'surgery',
            query: { surgeryId: $(event.target).attr("data-id"), patientId: $(event.target).attr("data-patient-id")  }
        });
    });

    addDefaultRowWhenTableEmpty();
});


function createDeleteDialog(id, deleteElement) {
    deleteDialog = emr.setupConfirmationDialog({
        selector: '#delete-dialog',
        actions: {
            confirm: function () {
                jq('#delete-dialog' + ' .icon-spin').css('display', 'inline-block').parent().addClass('disabled');
                deleteWithCallback(id, deleteElement);
                deleteDialog.close();
                jq('#delete-dialog' + ' .icon-spin').css('display', 'none').parent().removeClass('disabled');
            },
            cancel: function () {
                deleteDialog.close();
            }
        }
    });
}


function reloadPage(patientId) {
    emr.navigateTo({
        provider: 'operationtheater',
        page: 'patientsSurgeries',
        query: {deleted: true, patientId: patientId}
    });
}

function showDeleteDialog() {
    deleteDialog.show();
    return false;
}

function deleteWithCallback(id, deleteElement) {
    var patientId = $(deleteElement).attr("data-patient-id");
    emr.getFragmentActionWithCallback('operationtheater', 'patientsSurgeries', 'voidSurgery'
        , { surgeryId: id}
        , function (data) {
            reloadPage(patientId);
        }
        , function (err) {
            emr.handleError(err);
        }
    );
}

function verifyIfTableEmpty() {
    return $('#surgeriesTable tr').length == 1 ? true : false;
}

function addDefaultRowWhenTableEmpty() {

    if (verifyIfTableEmpty()) {
        var defaultMessage = $('#surgeriesTable').attr("empty-value-message");
        $('#surgeriesTable').append('<tr><td>' + defaultMessage + '</td><td></td><td></td><td></td></tr>');
    }
}





// TODO redo using angular?

$(function () {
    $(document).on('click', '.deleteProcedure', function (event) {
        var procedureId = $(event.target).attr("data-procedure-id");
        createDeleteProcedureDialog(procedureId, $(this));
        showDeleteProcedureDialog();
    });

    $(document).on('click', '.editProcedure', function (event) {
        emr.navigateTo({
            provider: 'operationtheater',
            page: 'createEditProcedure',
            query: { procedureId: $(event.target).attr("data-procedure-id") }
        });
    });

    addDefaultRowWhenTableEmpty();
});


function createDeleteProcedureDialog(procedureId, deleteElement) {
    deleteProcedureDialog = emr.setupConfirmationDialog({
        selector: '#delete-procedure-dialog',
        actions: {
            confirm: function () {
                jq('#delete-procedure-dialog' + ' .icon-spin').css('display', 'inline-block').parent().addClass('disabled');
                deleteProcedureWithCallback(procedureId, deleteElement);
                deleteProcedureDialog.close();
                jq('#delete-procedure-dialog' + ' .icon-spin').css('display', 'none').parent().removeClass('disabled');
            },
            cancel: function () {
                deleteProcedureDialog.close();
            }
        }
    });
}


function reloadPage() {
    emr.navigateTo({
        provider: 'operationtheater',
        page: 'manageProcedures',
        query: {deleted: true}
    });
}

function showDeleteProcedureDialog() {
    deleteProcedureDialog.show();
    return false;
}

function deleteProcedureWithCallback(procedureId, deleteElement) {
    emr.getFragmentActionWithCallback('operationtheater', 'manageProcedures', 'retireProcedure'
        , { procedureId: procedureId}
        , function (data) {
            reloadPage(data.message);
        }
        , function (err) {
            emr.handleError(err);
        }
    );
}

function verifyIfTableEmpty() {
    return $('#proceduresTable tr').length == 1 ? true : false;
}

function addDefaultRowWhenTableEmpty() {

    if (verifyIfTableEmpty()) {
        var defaultMessage = $('#proceduresTable').attr("empty-value-message");
        $('#proceduresTable').append('<tr><td>' + defaultMessage + '</td><td></td><td></td><td></td><td></td><td></td></tr>');
    }
}





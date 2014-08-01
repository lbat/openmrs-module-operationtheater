//Self-Executing Anonymous Function Pattern
//http://appendto.com/2010/10/how-good-c-habits-can-encourage-bad-javascript-habits-part-1/
(function (emrExt, $, undefined) {

    //TODO insert into uicommons emr.js and make pull request
    emrExt.message = function (code, args) {
        //https://stackoverflow.com/questions/610406/javascript-equivalent-to-printf-string-format
        if (!String.format) {
            String.format = function (format) {
                var args = Array.prototype.slice.call(arguments, 1);
                return format.replace(/{(\d+)}/g, function (match, number) {
                    return typeof args[number] != 'undefined' ? args[number] : match;
                });
            };
        }

        var message = emr.message(code, "");
        arguments[0] = message;
        return String.format.apply(null, arguments);
    };

}(window.emrExt = window.emrExt || {}, jQuery));


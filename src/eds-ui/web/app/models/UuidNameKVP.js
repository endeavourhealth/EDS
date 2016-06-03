var app;
(function (app) {
    var models;
    (function (models) {
        'use strict';
        var UuidNameKVP = (function () {
            function UuidNameKVP() {
            }
            UuidNameKVP.toAssociativeArray = function (items) {
                var associativeArray = {};
                for (var i = 0; i < items.length; i++) {
                    associativeArray[items[i].uuid] = items[i].name;
                }
                return associativeArray;
            };
            UuidNameKVP.fromAssociativeArray = function (associativeArray) {
                var array = [];
                for (var key in associativeArray) {
                    if (associativeArray.hasOwnProperty(key)) {
                        array.push({ uuid: key, name: associativeArray[key] });
                    }
                }
                return array;
            };
            return UuidNameKVP;
        })();
        models.UuidNameKVP = UuidNameKVP;
    })(models = app.models || (app.models = {}));
})(app || (app = {}));
//# sourceMappingURL=UuidNameKVP.js.map
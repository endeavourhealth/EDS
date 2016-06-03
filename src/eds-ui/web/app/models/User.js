/// <reference path="UserInRole.ts" />
var app;
(function (app) {
    var models;
    (function (models) {
        'use strict';
        var User = (function () {
            function User() {
            }
            return User;
        })();
        models.User = User;
    })(models = app.models || (app.models = {}));
})(app || (app = {}));
//# sourceMappingURL=User.js.map
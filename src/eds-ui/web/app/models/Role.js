var app;
(function (app) {
    var models;
    (function (models) {
        'use strict';
        (function (Role) {
            Role[Role["USER"] = 1] = "USER";
            Role[Role["ADMIN"] = 2] = "ADMIN";
            Role[Role["SUPER"] = 4] = "SUPER";
        })(models.Role || (models.Role = {}));
        var Role = models.Role;
    })(models = app.models || (app.models = {}));
})(app || (app = {}));
//# sourceMappingURL=Role.js.map
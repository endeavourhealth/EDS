/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var models;
    (function (models) {
        (function (FolderType) {
            FolderType[FolderType["Unknonwn"] = 0] = "Unknonwn";
            FolderType[FolderType["Library"] = 1] = "Library";
            FolderType[FolderType["Report"] = 2] = "Report";
        })(models.FolderType || (models.FolderType = {}));
        var FolderType = models.FolderType;
    })(models = app.models || (app.models = {}));
})(app || (app = {}));
//# sourceMappingURL=FolderType.js.map
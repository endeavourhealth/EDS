var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var models;
    (function (models) {
        'use strict';
        var FolderNode = (function (_super) {
            __extends(FolderNode, _super);
            function FolderNode() {
                _super.apply(this, arguments);
            }
            return FolderNode;
        })(models.Folder);
        models.FolderNode = FolderNode;
    })(models = app.models || (app.models = {}));
})(app || (app = {}));
//# sourceMappingURL=FolderNode.js.map
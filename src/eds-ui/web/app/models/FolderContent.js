var app;
(function (app) {
    var models;
    (function (models) {
        var FolderItem = (function () {
            function FolderItem() {
                this.children = [];
            }
            return FolderItem;
        })();
        models.FolderItem = FolderItem;
    })(models = app.models || (app.models = {}));
})(app || (app = {}));
//# sourceMappingURL=FolderContent.js.map
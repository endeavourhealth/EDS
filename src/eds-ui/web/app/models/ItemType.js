/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var models;
    (function (models) {
        (function (ItemType) {
            ItemType[ItemType["ReportFolder"] = 0] = "ReportFolder";
            ItemType[ItemType["Report"] = 1] = "Report";
            ItemType[ItemType["Query"] = 2] = "Query";
            ItemType[ItemType["Test"] = 3] = "Test";
            ItemType[ItemType["Resource"] = 4] = "Resource";
            ItemType[ItemType["CodeSet"] = 5] = "CodeSet";
            ItemType[ItemType["DataSet"] = 6] = "DataSet";
            ItemType[ItemType["LibraryFolder"] = 7] = "LibraryFolder";
            ItemType[ItemType["Protocol"] = 8] = "Protocol";
            ItemType[ItemType["System"] = 9] = "System"; // 9
        })(models.ItemType || (models.ItemType = {}));
        var ItemType = models.ItemType;
        function itemTypeIdToString() {
            return function (input) {
                switch (input) {
                    case ItemType.ReportFolder:
                        return 'Report folder';
                    case ItemType.Report:
                        return 'Report';
                    case ItemType.Query:
                        return 'Cohort';
                    case ItemType.Test:
                        return 'Test';
                    case ItemType.Resource:
                        return 'Resource';
                    case ItemType.CodeSet:
                        return 'Code set';
                    case ItemType.DataSet:
                        return 'Data set';
                    case ItemType.LibraryFolder:
                        return 'Library folder';
                    case ItemType.Protocol:
                        return 'Data protocol';
                    case ItemType.System:
                        return 'System';
                }
            };
        }
        models.itemTypeIdToString = itemTypeIdToString;
        function itemTypeIdToIcon() {
            return function (input) {
                switch (input) {
                    case ItemType.ReportFolder:
                        return 'fa-folder-open';
                    case ItemType.Report:
                        return 'fa-file';
                    case ItemType.Query:
                        return 'fa-user';
                    case ItemType.Test:
                        return 'fa-random';
                    case ItemType.Resource:
                        return 'fa-database';
                    case ItemType.CodeSet:
                        return 'fa-tags';
                    case ItemType.DataSet:
                        return 'fa-list-alt';
                    case ItemType.LibraryFolder:
                        return 'fa-folder-open';
                    case ItemType.Protocol:
                        return 'fa-share-alt';
                    case ItemType.System:
                        return 'fa-laptop';
                }
            };
        }
        models.itemTypeIdToIcon = itemTypeIdToIcon;
        angular
            .module('app.models')
            .filter('itemTypeIdToString', itemTypeIdToString)
            .filter('itemTypeIdToIcon', itemTypeIdToIcon);
    })(models = app.models || (app.models = {}));
})(app || (app = {}));
//# sourceMappingURL=ItemType.js.map
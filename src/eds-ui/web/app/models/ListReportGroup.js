var app;
(function (app) {
    var models;
    (function (models) {
        'use strict';
        var ListReportGroup = (function () {
            function ListReportGroup() {
            }
            return ListReportGroup;
        })();
        models.ListReportGroup = ListReportGroup;
        function listReportGroupToIcon() {
            return function (listReportGroup) {
                if (listReportGroup.summary != null) {
                    return 'fa-summary';
                }
                if (listReportGroup.fieldBased != null) {
                    return 'fa-fieldBased';
                }
            };
        }
        models.listReportGroupToIcon = listReportGroupToIcon;
        function listReportGroupToTypeName() {
            return function (listReportGroup) {
                if (listReportGroup.summary != null) {
                    return 'Summary';
                }
                if (listReportGroup.fieldBased != null) {
                    return 'Field based';
                }
            };
        }
        models.listReportGroupToTypeName = listReportGroupToTypeName;
        function listReportGroupToDescription() {
            return function (listReportGroup) {
                if (listReportGroup.summary != null) {
                    return 'Summary description';
                }
                if (listReportGroup.fieldBased != null) {
                    return ' (' + listReportGroup.fieldBased.fieldOutput.length + ' fields)';
                }
            };
        }
        models.listReportGroupToDescription = listReportGroupToDescription;
        angular
            .module('app.models')
            .filter('listReportGroupToIcon', listReportGroupToIcon)
            .filter('listReportGroupToTypeName', listReportGroupToTypeName)
            .filter('listReportGroupToDescription', listReportGroupToDescription);
    })(models = app.models || (app.models = {}));
})(app || (app = {}));
//# sourceMappingURL=ListReportGroup.js.map
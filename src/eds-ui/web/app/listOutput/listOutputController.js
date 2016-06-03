/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var listOutput;
    (function (listOutput) {
        var TestEditorController = app.dialogs.TestEditorController;
        var LibraryItemModuleBase = app.library.LibraryItemModuleBase;
        'use strict';
        var ListOutputController = (function (_super) {
            __extends(ListOutputController, _super);
            function ListOutputController(libraryService, logger, $modal, adminService, $window, $stateParams) {
                _super.call(this, libraryService, adminService, logger, $window, $stateParams);
                this.libraryService = libraryService;
                this.logger = logger;
                this.$modal = $modal;
                this.adminService = adminService;
                this.$window = $window;
                this.$stateParams = $stateParams;
                this.loadEntityMap();
            }
            ListOutputController.prototype.loadEntityMap = function () {
                var vm = this;
                vm.libraryService.getEntityMap().then(function (result) {
                    vm.entityMap = result;
                })
                    .catch(function (data) {
                    vm.logger.error('Error loading entity map', data, 'Error');
                });
            };
            ListOutputController.prototype.selectDataSource = function (datasourceContainer) {
                var vm = this;
                var test = null;
                if (datasourceContainer.dataSource) {
                    test = { dataSource: datasourceContainer.dataSource };
                }
                TestEditorController.open(this.$modal, test, true)
                    .result.then(function (dataSourceContainer) {
                    datasourceContainer.dataSource = dataSourceContainer.dataSource;
                    if (vm.selectedListReportGroup.heading === '') {
                        vm.selectedListReportGroup.heading = vm.getDatasourceDisplayName();
                    }
                    vm.loadDataSourceAvailableFieldList();
                    vm.adminService.setPendingChanges();
                });
            };
            ListOutputController.prototype.loadDataSourceAvailableFieldList = function () {
                // Find entity in entitymap
                this.dataSourceAvailableFields = [];
                var entityName = this.selectedListReportGroup.fieldBased.dataSource.entity;
                var matchingEntities = $.grep(this.entityMap.entity, function (e) { return e.logicalName === entityName; });
                if (matchingEntities.length === 1) {
                    this.dataSourceAvailableFields = $.grep(matchingEntities[0].field, function (e) { return e.availability.indexOf('output') > -1; });
                }
            };
            ListOutputController.prototype.getFieldDisplayName = function (logicalName) {
                var matchingFields = $.grep(this.dataSourceAvailableFields, function (e) { return e.logicalName === logicalName; });
                if (matchingFields.length === 1) {
                    return matchingFields[0].displayName;
                }
                return '<Select...>';
            };
            ListOutputController.prototype.getDatasourceDisplayName = function () {
                if (this.selectedListReportGroup
                    && this.selectedListReportGroup.fieldBased
                    && this.selectedListReportGroup.fieldBased.dataSource) {
                    var logicalName = this.selectedListReportGroup.fieldBased.dataSource.entity;
                    var matchingEntities = $.grep(this.entityMap.entity, function (e) { return e.logicalName === logicalName; });
                    if (matchingEntities.length === 1) {
                        return matchingEntities[0].displayName;
                    }
                }
                return '<Unknown>';
            };
            ListOutputController.prototype.addListGroup = function () {
                this.selectedListReportGroup = {
                    heading: '',
                    fieldBased: {
                        dataSource: null,
                        fieldOutput: []
                    }
                };
                this.libraryItem.listReport.group.push(this.selectedListReportGroup);
            };
            ListOutputController.prototype.removeListGroup = function (scope) {
                this.libraryItem.listReport.group.splice(scope.$index, 1);
                if (this.selectedListReportGroup === scope.item) {
                    this.selectedListReportGroup = null;
                }
            };
            ListOutputController.prototype.addFieldOutput = function () {
                this.selectedFieldOutput = {
                    heading: '',
                    field: ''
                };
                this.selectedListReportGroup.fieldBased.fieldOutput.push(this.selectedFieldOutput);
            };
            ListOutputController.prototype.removeFieldOutput = function (scope) {
                this.selectedListReportGroup.fieldBased.fieldOutput.splice(scope.$index, 1);
                if (this.selectedFieldOutput === scope.item) {
                    this.selectedFieldOutput = null;
                }
            };
            ListOutputController.prototype.setItemField = function (item, field) {
                item.field = field.logicalName;
                if (item.heading === '') {
                    item.heading = field.displayName;
                }
            };
            ListOutputController.prototype.create = function (folderUuid) {
                _super.prototype.create.call(this, folderUuid);
                this.libraryItem.listReport = {
                    group: []
                };
            };
            ListOutputController.$inject = ['LibraryService', 'LoggerService',
                '$uibModal', 'AdminService', '$window', '$stateParams'];
            return ListOutputController;
        })(LibraryItemModuleBase);
        listOutput.ListOutputController = ListOutputController;
        angular
            .module('app.listOutput')
            .controller('ListOutputController', ListOutputController);
    })(listOutput = app.listOutput || (app.listOutput = {}));
})(app || (app = {}));
//# sourceMappingURL=listOutputController.js.map
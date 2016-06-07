/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var dialogs;
    (function (dialogs) {
        'use strict';
        var QueueReportController = (function (_super) {
            __extends(QueueReportController, _super);
            function QueueReportController($uibModalInstance, logger, adminService, organisationSetService, $modal, reportUuid, reportName) {
                _super.call(this, $uibModalInstance);
                this.$uibModalInstance = $uibModalInstance;
                this.logger = logger;
                this.adminService = adminService;
                this.organisationSetService = organisationSetService;
                this.$modal = $modal;
                this.reportUuid = reportUuid;
                this.reportName = reportName;
                this.patientTypeDisplay = {
                    regular: 'Regular patients',
                    nonRegular: 'Non-regular patients',
                    all: 'All patients'
                };
                this.patientStatusDisplay = {
                    active: 'Active patients',
                    all: 'Active and non-active patients'
                };
                var requestParameters = {
                    reportUuid: reportUuid,
                    baselineDate: null,
                    patientType: 'regular',
                    patientStatus: 'active',
                    organisation: []
                };
                this.resultData = requestParameters;
            }
            QueueReportController.open = function ($modal, reportUuid, reportName) {
                var options = {
                    templateUrl: 'app/dialogs/queueReport/queueReport.html',
                    controller: 'QueueReportController',
                    controllerAs: 'queueReport',
                    // size:'lg',
                    backdrop: 'static',
                    resolve: {
                        reportUuid: function () { return reportUuid; },
                        reportName: function () { return reportName; }
                    }
                };
                var dialog = $modal.open(options);
                return dialog;
            };
            QueueReportController.prototype.getOrganisationListDisplayText = function () {
                if (this.resultData.organisation && this.resultData.organisation.length > 0) {
                    return this.resultData.organisation.length + ' Organisation(s)';
                }
                else {
                    return 'All Organisations';
                }
            };
            QueueReportController.prototype.pickOrganisationList = function () {
                var vm = this;
                dialogs.OrganisationPickerController.open(vm.$modal, this.resultData.organisation, null)
                    .result.then(function (organisationSet) {
                    vm.resultData.organisation = organisationSet.organisations;
                });
            };
            QueueReportController.prototype.clearOrganisationList = function () {
                this.resultData.organisation = [];
            };
            QueueReportController.prototype.ok = function () {
                if (this.baselineDate) {
                    this.resultData.baselineDate = this.baselineDate.valueOf();
                }
                else {
                    this.resultData.baselineDate = null;
                }
                _super.prototype.ok.call(this);
            };
            QueueReportController.$inject = ['$uibModalInstance', 'LoggerService', 'AdminService', 'OrganisationSetService', '$uibModal',
                'reportUuid', 'reportName'];
            return QueueReportController;
        })(dialogs.BaseDialogController);
        dialogs.QueueReportController = QueueReportController;
        angular
            .module('app.dialogs')
            .controller('QueueReportController', QueueReportController);
    })(dialogs = app.dialogs || (app.dialogs = {}));
})(app || (app = {}));
//# sourceMappingURL=queueReport.controller.js.map
/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var protocol;
    (function (protocol) {
        var LibraryItemModuleBase = app.library.LibraryItemModuleBase;
        'use strict';
        var ProtocolController = (function (_super) {
            __extends(ProtocolController, _super);
            function ProtocolController(libraryService, serviceService, logger, $modal, adminService, $window, $stateParams) {
                _super.call(this, libraryService, adminService, logger, $window, $stateParams);
                this.libraryService = libraryService;
                this.serviceService = serviceService;
                this.logger = logger;
                this.$modal = $modal;
                this.adminService = adminService;
                this.$window = $window;
                this.$stateParams = $stateParams;
                this.enabled = ["TRUE", "FALSE"];
                this.consent = ["OPT-IN", "OPT-OUT"];
                this.type = ["PUBLISHER", "SUBSCRIBER"];
                this.loadServices();
                this.loadSystems();
                this.loadCohorts();
                this.loadDatasets();
            }
            ProtocolController.prototype.create = function (folderUuid) {
                this.protocol = {
                    enabled: 'TRUE',
                    patientConsent: 'OPT-IN',
                    cohort: '0',
                    dataSet: '0',
                    serviceContract: []
                };
                this.libraryItem = {
                    uuid: null,
                    name: 'New data protocol',
                    description: '',
                    folderUuid: folderUuid,
                    protocol: this.protocol
                };
            };
            ProtocolController.prototype.addContract = function () {
                this.selectedContract = {
                    type: '',
                    service: null,
                    system: null,
                    technicalInterface: null,
                    active: 'TRUE'
                };
                this.libraryItem.protocol.serviceContract.push(this.selectedContract);
            };
            ProtocolController.prototype.removeContract = function (scope) {
                this.libraryItem.protocol.serviceContract.splice(scope.$index, 1);
                if (this.selectedContract === scope.item) {
                    this.selectedContract = null;
                }
            };
            ProtocolController.prototype.setService = function () {
                var serviceName = $("#service>option:selected").html();
                this.selectedContract.service.name = serviceName;
            };
            ProtocolController.prototype.setSystem = function () {
                var systemName = $("#system>option:selected").html();
                this.selectedContract.system.name = systemName;
            };
            ProtocolController.prototype.setTechnicalInterface = function () {
                var technicalInterfaceName = $("#technicalInterface>option:selected").html();
                this.selectedContract.technicalInterface.name = technicalInterfaceName;
            };
            ProtocolController.prototype.loadServices = function () {
                var vm = this;
                vm.serviceService.getAll()
                    .then(function (result) {
                    vm.services = result;
                })
                    .catch(function (error) {
                    vm.logger.error('Failed to load services', error, 'Load services');
                });
            };
            ProtocolController.prototype.loadCohorts = function () {
                var vm = this;
                vm.libraryService.getCohorts()
                    .then(function (result) {
                    vm.cohorts = result;
                })
                    .catch(function (error) {
                    vm.logger.error('Failed to load cohorts', error, 'Load cohorts');
                });
            };
            ProtocolController.prototype.loadDatasets = function () {
                var vm = this;
                vm.libraryService.getDatasets()
                    .then(function (result) {
                    vm.dataSets = result;
                })
                    .catch(function (error) {
                    vm.logger.error('Failed to load dataSets', error, 'Load dataSets');
                });
                /*vm.libraryService.getProtocols("edf5ac83-1491-4631-97ff-5c7a283c73b1")
                    .then(function(result) {
                        vm.protocols = result;
                    })
                    .catch(function (error) {
                        vm.logger.error('Failed to load protocols', error, 'Load protocols');
                    });*/
            };
            ProtocolController.prototype.loadSystems = function () {
                var vm = this;
                vm.libraryService.getSystems()
                    .then(function (result) {
                    vm.systems = result;
                    vm.technicalInterfaces = [];
                    console.log(vm.systems[0].technicalInterface.length);
                    console.log(vm.systems[0].technicalInterface[0].name);
                    for (var i = 0; i < vm.systems.length; ++i) {
                        for (var j = 0; j < vm.systems[i].technicalInterface.length; ++j) {
                            var technicalInterface = {
                                uuid: vm.systems[i].technicalInterface[j].uuid,
                                name: vm.systems[i].technicalInterface[j].name,
                                messageType: vm.systems[i].technicalInterface[j].messageType,
                                messageFormat: vm.systems[i].technicalInterface[j].messageFormat,
                                messageFormatVersion: vm.systems[i].technicalInterface[j].messageFormatVersion
                            };
                            vm.technicalInterfaces.push(technicalInterface);
                        }
                    }
                })
                    .catch(function (error) {
                    vm.logger.error('Failed to load systems', error, 'Load systems');
                });
            };
            ProtocolController.$inject = ['LibraryService', 'ServiceService', 'LoggerService',
                '$uibModal', 'AdminService', '$window', '$stateParams'];
            return ProtocolController;
        })(LibraryItemModuleBase);
        protocol.ProtocolController = ProtocolController;
        angular
            .module('app.protocol')
            .controller('ProtocolController', ProtocolController);
    })(protocol = app.protocol || (app.protocol = {}));
})(app || (app = {}));
//# sourceMappingURL=protocol.controller.js.map
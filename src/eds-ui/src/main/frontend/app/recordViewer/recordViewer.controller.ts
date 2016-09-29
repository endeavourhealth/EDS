/// <reference path="../../typings/index.d.ts" />
/// <reference path="../core/patientIdentity.service.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.recordViewer {
	import IRecordViewerService = app.core.IRecordViewerService;
    import PatientFindController = app.dialogs.PatientFindController;
    import UIPatient = app.models.UIPatient;
    import Encounter = app.models.Encounter;

	'use strict';

	export class RecordViewerController {
        patient: UIPatient;
        encounters: Encounter[];
        firstTabActive: boolean = true;

		static $inject = ['$uibModal', 'RecordViewerService'];

		constructor(private $modal: IModalService,
                    protected recordViewerService: IRecordViewerService) {

            this.showPatientFind();
		}

        showPatientFind() {
            var vm = this;

            PatientFindController
                .open(vm.$modal)
                .result
                .then(
                    function (result: UIPatient) {
                        vm.setPatient(result);
                    });
        }

        setPatient(patient: UIPatient) {
            this.clearPatient();
            this.patient = patient;
        }

        clearPatient() {
            this.firstTabActive = true;
            this.patient = null;
            this.encounters = null;
        }

        loadConsultations() {
            var vm = this;
            vm.encounters = null;

            vm.recordViewerService.getEncounters(vm.patient.serviceId, vm.patient.systemId, vm.patient.patientId)
                .then(
                    function (data: Encounter[]) {
                    vm.encounters = data;
                });
        }
    }

	angular
		.module('app.recordViewer')
		.controller('RecordViewerController', RecordViewerController);
}

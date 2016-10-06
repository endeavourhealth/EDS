import IModalService = angular.ui.bootstrap.IModalService;

import {UIPatient} from "./models/UIPatient";
import {UIEncounter} from "./models/UIEncounter";
import {PatientFindController} from "../dialogs/patientFind/patientFind.controller";
import {UICondition} from "./models/UICondition";
import {IRecordViewerService} from "./recordViewer.service";

export class RecordViewerController {
    patient: UIPatient;
    encounters: UIEncounter[];
    conditions: UICondition[];
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
        this.conditions = null;
    }

    loadConsultations() {
        var vm = this;
        vm.encounters = null;

        vm.recordViewerService.getEncounters(vm.patient.serviceId, vm.patient.systemId, vm.patient.patientId)
            .then(
                function (data: UIEncounter[]) {
                vm.encounters = data;
            });
    }

    loadConditions() {
        var vm = this;
        vm.conditions = null;

        vm.recordViewerService.getConditions(vm.patient.serviceId, vm.patient.systemId, vm.patient.patientId)
            .then(
                function (data: UICondition[]) {
                    vm.conditions = data;
                });
    }
}

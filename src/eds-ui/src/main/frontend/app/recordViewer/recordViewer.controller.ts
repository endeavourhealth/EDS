import IModalService = angular.ui.bootstrap.IModalService;

import {UIPatient} from "./models/resources/admin/UIPatient";
import {UIEncounter} from "./models/resources/clinical/UIEncounter";
import {PatientFindController} from "../dialogs/patientFind/patientFind.controller";
import {UICondition} from "./models/resources/clinical/UICondition";
import {IRecordViewerService} from "./recordViewer.service";
import {List} from "linqts/dist/linq";
import {UIProblem} from "./models/resources/clinical/UIProblem";
import {linq} from "../blocks/linq";

export class RecordViewerController {
    patient: UIPatient;
    encounters: UIEncounter[];
    problems: UIProblem[];
    activeTab: number = 0;

static $inject = ['$uibModal', 'RecordViewerService'];

constructor(private $modal: IModalService,
                protected recordViewerService: IRecordViewerService) {

        this.showPatientFind();
}

    showPatientFind() {
        var ctrl = this;
        PatientFindController
            .open(ctrl.$modal)
            .result
            .then((result: UIPatient) => ctrl.setPatient(result));
    }

    setPatient(patient: UIPatient) {
        this.clearPatient();
        this.patient = patient;
    }

    clearPatient() {
        this.activeTab = 0;
        this.patient = null;
        this.encounters = null;
        this.problems = null;
    }

    getActiveProblems(): UIProblem[] {
        return linq(this.problems)
            .Where(t => (!t.hasAbated))
            .ToArray();
    }

    getPastProblems(): UIProblem[] {
        return linq(this.problems)
            .Where(t => t.hasAbated)
            .ToArray();
    }

    loadConsultations() {
        this.encounters = null;

        var ctrl = this;
        ctrl
            .recordViewerService
            .getEncounters(ctrl.patient.serviceId, ctrl.patient.systemId, ctrl.patient.patientId)
            .then((result: UIEncounter[]) => ctrl.encounters = result);
    }

    loadProblems() {
        this.problems = null;

        var ctrl = this;
        ctrl
            .recordViewerService
            .getProblems(ctrl.patient.serviceId, ctrl.patient.systemId, ctrl.patient.patientId)
            .then((result: UIProblem[]) =>
                ctrl.problems = linq(result)
                    .OrderByDescending(t => t.effectiveDate.date)
                    .ToArray());
    }
}

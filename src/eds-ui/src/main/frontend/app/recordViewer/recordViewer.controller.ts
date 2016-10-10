import IModalService = angular.ui.bootstrap.IModalService;

import {UIPatient} from "./models/resources/admin/UIPatient";
import {UIEncounter} from "./models/resources/clinical/UIEncounter";
import {PatientFindController} from "../dialogs/patientFind/patientFind.controller";
import {IRecordViewerService} from "./recordViewer.service";
import {UIProblem} from "./models/resources/clinical/UIProblem";
import {linq} from "../blocks/linq";
import {UIPatientRecord} from "./models/UIPatientRecord";

export class RecordViewerController {
    patient: UIPatientRecord;
    activeTab: number = 0;

    static $inject = ['$uibModal', 'RecordViewerService'];

    constructor(private $modal: IModalService, protected recordViewerService: IRecordViewerService) {
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
        this.patient = new UIPatientRecord(patient);
    }

    clearPatient() {
        this.activeTab = 0;
        this.patient = null;
    }

    getActiveProblems(): UIProblem[] {
        if ((this.patient == null) || (this.patient.problems == null))
            return null;

        return linq(this.patient.problems)
            .Where(t => (!t.hasAbated))
            .ToArray();
    }

    getPastProblems(): UIProblem[] {
        if ((this.patient == null) || (this.patient.problems == null))
            return null;

        return linq(this.patient.problems)
            .Where(t => t.hasAbated)
            .ToArray();
    }

    loadConsultations() {
        if (this.patient.encounters != null)
            return;

        var ctrl = this;
        ctrl
            .recordViewerService
            .getEncounters(ctrl.patient.patient.patientId)
            .then((result: UIEncounter[]) => ctrl.patient.encounters = result);
    }

    loadProblems() {
        if (this.patient.problems != null)
            return;

        var ctrl = this;
        ctrl
            .recordViewerService
            .getProblems(ctrl.patient.patient.patientId)
            .then((result: UIProblem[]) =>
                ctrl.patient.problems = linq(result)
                    .OrderByDescending(t => t.effectiveDate.date)
                    .ToArray());
    }
}

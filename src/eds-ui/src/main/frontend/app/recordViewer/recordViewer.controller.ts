import IModalService = angular.ui.bootstrap.IModalService;

import {UIPatient} from "./models/resources/admin/UIPatient";
import {UIEncounter} from "./models/resources/clinical/UIEncounter";
import {PatientFindController} from "../dialogs/patientFind/patientFind.controller";
import {IRecordViewerService} from "./recordViewer.service";
import {UIProblem} from "./models/resources/clinical/UIProblem";
import {linq} from "../blocks/linq";
import {UIPatientRecord} from "./models/UIPatientRecord";

export class RecordViewerController {

    public patient: UIPatientRecord;
    public activeTab: number = 0;
    private activeProblemSelected: UIProblem = null;

    static $inject = ['$uibModal', 'RecordViewerService'];

    constructor(private $modal: IModalService, protected recordViewerService: IRecordViewerService) {
        this.showPatientFind();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // patient find
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public showPatientFind(): void {
        var ctrl = this;
        PatientFindController
            .open(ctrl.$modal)
            .result
            .then((result: UIPatient) => ctrl.setPatient(result));
    }

    public setPatient(patient: UIPatient): void {
        this.clearPatient();
        this.patient = new UIPatientRecord(patient);
    }

    public clearPatient(): void {
        this.activeProblemSelected = null;
        this.activeTab = 0;
        this.patient = null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // consultations
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public loadConsultations(): void {
        if (this.patient.encounters != null)
            return;

        var ctrl = this;
        ctrl
            .recordViewerService
            .getEncounters(ctrl.patient.patient.patientId)
            .then((result: UIEncounter[]) => ctrl.patient.encounters = result);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // problems
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public isActiveProblemSelected(activeProblem: UIProblem): boolean {
        return (this.activeProblemSelected == activeProblem);
    }

    public setActiveProblemSelected(activeProblem: UIProblem): void {
        this.activeProblemSelected = activeProblem;
    }

    public getActiveProblemSelected(): UIProblem {
        return this.activeProblemSelected;
    }

    public loadProblems(): void {
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

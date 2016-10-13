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
    private selectedProblem: UIProblem = null;

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
        this.selectedProblem = null;
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
    public isSelectedProblem(problem: UIProblem): boolean {
        return (this.selectedProblem == problem);
    }

    public setSelectedProblem(problem: UIProblem): void {
        this.selectedProblem = problem;
    }

    public getSelectedProblem(): UIProblem {
        return this.selectedProblem;
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

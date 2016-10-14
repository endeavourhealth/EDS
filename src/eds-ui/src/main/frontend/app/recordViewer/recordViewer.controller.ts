import IModalService = angular.ui.bootstrap.IModalService;

import {UIPatient} from "./models/resources/admin/UIPatient";
import {UIEncounter} from "./models/resources/clinical/UIEncounter";
import {PatientFindController} from "../dialogs/patientFind/patientFind.controller";
import {IRecordViewerService} from "./recordViewer.service";
import {UIProblem} from "./models/resources/clinical/UIProblem";
import {linq} from "../blocks/linq";
import {UIPatientRecord} from "./models/UIPatientRecord";
import IDocumentService = angular.IDocumentService;
import {UIDiary} from "./models/resources/clinical/UIDiary";

export class RecordViewerController {

    public patient: UIPatientRecord;
    public activeTab: number = 0;
    private selectedProblem: UIProblem = null;
    private problemDetailsPanelMarginTop: number = 0;

    static $inject = ['$document', '$uibModal', 'RecordViewerService'];

    constructor(private $document: IDocumentService, private $modal: IModalService, protected recordViewerService: IRecordViewerService) {
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

    public setSelectedProblem(event: Event, problem: UIProblem): void {
        this.selectedProblem = problem;
        this.setProblemDetailsPanelMarginTop(event);
    }

    private setProblemDetailsPanelMarginTop(event: Event): void {
        var columnHeader = this.$document.find('#problems-list-column');

        if (columnHeader.length > 0) {
            var columnHeaderTop = (columnHeader[0] as Element).getBoundingClientRect().top;
            var tableRowTop = (event.target as Element).getBoundingClientRect().top;
            this.problemDetailsPanelMarginTop = tableRowTop - columnHeaderTop;
        }
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // diary
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public loadDiary(): void {
        if (this.patient.diary != null)
            return;

        var ctrl = this;
        ctrl
            .recordViewerService
            .getDiary(ctrl.patient.patient.patientId)
            .then((result: UIDiary[]) =>
                ctrl.patient.diary = linq(result)
                    .OrderByDescending(t => t.effectiveDate.date)
                    .ToArray());
    }
}

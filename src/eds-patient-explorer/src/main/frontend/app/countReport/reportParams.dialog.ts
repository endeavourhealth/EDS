import moment = require("moment");
import {Component, Input, OnInit} from "@angular/core";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CodePickerDialog} from "../coding/codePicker.dialog";
import {CodeSetValue} from "../coding/models/CodeSetValue";
import {CodingService} from "../coding/coding.service";
import {Concept} from "../coding/models/Concept";
import {CountReportService} from "./countReport.service";
import {Practitioner} from "../practitioner/models/Practitioner";
import {PractitionerPickerDialog} from "../practitioner/practitionerPicker.dialog";
import {CountReport} from "./models/CountReport";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./reportParams.html')
})
export class ReportParamsDialog implements OnInit {
    @Input() countReport : CountReport;

    encounterTypes : Concept[];

    runDate : Date;
    effectiveDate : Date;
    originalCode : string;
    valueMax : number;
    valueMin : number;
    snomedCode : CodeSetValue;
    rootCode : CodeSetValue = null;
    authType : number;
    practitioner : Practitioner;
    dmdCode : number;
    encounterType : number;

    public static open(modalService: NgbModal, countReport: CountReport) {
        const modalRef = modalService.open(ReportParamsDialog, {backdrop: "static", size: 'lg'});
        modalRef.componentInstance.countReport = countReport;
        return modalRef;
    }

    constructor(protected modalService : NgbModal,
                protected activeModal: NgbActiveModal,
                protected codingService : CodingService,
                protected countReportService : CountReportService) {
        this.loadEncounterTypes();
    }

    ngOnInit(): void {
        // work out prompts from query text
        this.runDate = new Date();

        if (!this.countReport)
            return;

        // Check query for remaining prompts
        if (this.countReport.query.indexOf(':EffectiveDate') >= 0) this.effectiveDate = null;
        if (this.countReport.query.indexOf(':SnomedCode') >= 0) this.snomedCode = null;
        if (this.countReport.query.indexOf(':OriginalCode') >= 0) this.originalCode = null;
        if (this.countReport.query.indexOf(':ValueMin') >= 0) this.valueMin = null;
        if (this.countReport.query.indexOf(':ValueMax') >= 0) this.valueMax = null;
        if (this.countReport.query.indexOf(':AuthType') >= 0) this.authType = null;
        if (this.countReport.query.indexOf(':Practitioner') >= 0) this.practitioner = null;
        // DM&D
        if (this.countReport.query.indexOf(':EncounterType') >= 0) this.encounterType = null;

        if (this.countReport.tables.indexOf('referral_request') >= 0) this.rootCode = {code : '3457005', term : 'Patient referral'} as CodeSetValue;
    }

    loadEncounterTypes() {
        let vm = this;
        vm.countReportService.getEncounterTypeCodes()
          .subscribe(
            (result) => vm.encounterTypes = result
          );
    }

    selectSnomed() {
        var vm = this;
        CodePickerDialog.open(vm.modalService, [], true, this.rootCode)
          .result.then(
          (result) => {
              vm.snomedCode = result[0];
              vm.snomedCode.term = 'Loading...';
              vm.codingService.getPreferredTerm(vm.snomedCode.code)
                .subscribe(
                  (term) => vm.snomedCode.term = term.preferredTerm
                );
          }
        )
    }

    clearSnomed() {
        this.snomedCode = null;
    }

    selectPractitioner() {
        var vm = this;
        PractitionerPickerDialog.open(vm.modalService)
          .result.then(
          (result) => vm.practitioner = result
        );
    }

    clearPractitioner() {
        this.practitioner = null;
    }

    setEffectiveDate($event) {
        if ($event)
            this.effectiveDate = $event;
    }

    hide(item : any) {
        return item === undefined;
    }

    ok() {
        let params : any = {};

        params.RunDate = "'" + moment(this.runDate).format('DD/MM/YYYY') + "'";
        params.EffectiveDate = (this.effectiveDate) ? "'" + moment(this.effectiveDate).format('DD/MM/YYYY') + "'" : 'null';
        params.SnomedCode = (this.snomedCode) ? this.snomedCode.code : 'null';
        params.OriginalCode = (this.originalCode) ? "'" + this.originalCode + "'" : 'null';
        params.ValueMin = (this.valueMin) ? this.valueMin : 'null';
        params.ValueMax = (this.valueMax) ? this.valueMax  : 'null';
        params.AuthType = (this.authType) ? this.authType : 'null';
        params.Practitioner = (this.practitioner) ? this.practitioner.id : 'null';
        // DM & D
        params.EncounterType = (this.encounterType) ? this.encounterType : 'null';
        // Referral type

        this.activeModal.close(params);
        console.log('OK Pressed');
    }

    cancel() {
        this.activeModal.dismiss('cancel');
        console.log('Cancel Pressed');
    }
}

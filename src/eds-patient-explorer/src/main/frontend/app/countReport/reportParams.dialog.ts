import {Component, Input, OnInit} from "@angular/core";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {ReportParams} from "./models/ReportParams";
import moment = require("moment");
import {CodePickerDialog} from "../coding/codePicker.dialog";
import {CodeSetValue} from "../coding/models/CodeSetValue";
import {CodingService} from "../coding/coding.service";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./reportParams.html')
})
export class ReportParamsDialog implements OnInit {
    @Input() query;
    runDate : Date;
    effectiveDate : Date;
    originalCode : string;
    valueMax : number;
    valueMin : number;
    snomedCode : CodeSetValue;
    authType : string;
    dmdCode : number;

    public static open(modalService: NgbModal, query: string) {
        const modalRef = modalService.open(ReportParamsDialog, {backdrop: "static", size: 'lg'});
        modalRef.componentInstance.query = query;
        return modalRef;
    }

    constructor(protected modalService : NgbModal, protected activeModal: NgbActiveModal, protected codingService : CodingService ) {
    }

    ngOnInit(): void {
        // work out prompts from query text
        this.runDate = new Date();

        if (!this.query)
            return;

        // Check query for remaining prompts
        if (this.query.indexOf(':EffectiveDate') >= 0) this.effectiveDate = null;
        if (this.query.indexOf(':SnomedCode') >= 0) this.snomedCode = null;
        if (this.query.indexOf(':OriginalCode') >= 0) this.originalCode = null;
        if (this.query.indexOf(':ValueMin') >= 0) this.valueMin = null;
        if (this.query.indexOf(':ValueMax') >= 0) this.valueMax = null;
        if (this.query.indexOf(':AuthType') >= 0) this.authType = null;
    }

    selectSnomed() {
        var vm = this;
        CodePickerDialog.open(vm.modalService, [], true)
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
        params.AuthType = (this.authType) ? "'" + this.authType +"'" : 'null';

        this.activeModal.close(params);
        console.log('OK Pressed');
    }

    cancel() {
        this.activeModal.dismiss('cancel');
        console.log('Cancel Pressed');
    }
}

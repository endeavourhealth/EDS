import {Component, Input} from "@angular/core";
import {LoggerService} from "eds-common-js";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {DsaPurpose} from "./models/DsaPurpose";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./purposeAdd.html')
})
export class PurposeAddDialog {
    public static open(modalService: NgbModal, purposes : DsaPurpose[], type : string) {
        const modalRef = modalService.open(PurposeAddDialog, { backdrop : "static"});
        modalRef.componentInstance.resultData = jQuery.extend(true, [], purposes);
        modalRef.componentInstance.type = type;

        return modalRef;
    }

    @Input() resultData : DsaPurpose[];
    @Input() type : string;
    title : string = '';
    detail : string = '';


    constructor(public activeModal: NgbActiveModal,
                private log:LoggerService) {}


    Add() {
        var newPurpose : DsaPurpose = new DsaPurpose();
        newPurpose.title = this.title;
        newPurpose.detail = this.detail;
        this.resultData.push(newPurpose);
        this.activeModal.close(this.resultData);
    }

    AddAnother() {
        var newPurpose : DsaPurpose = new DsaPurpose();
        newPurpose.title = this.title;
        newPurpose.detail = this.detail;
        this.resultData.push(newPurpose);
        this.title = '';
        this.detail = '';
    }

    cancel() {
        this.activeModal.dismiss('cancel');
    }
}

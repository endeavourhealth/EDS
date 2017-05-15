import {Component, Input} from "@angular/core";
import {Dsa} from "./models/Dsa";
import {DsaService} from "./dsa.service";
import {LoggerService} from "eds-common-js";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {DsaPurpose} from "./models/DsaPurpose";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./purposeAdd.html')
})
export class PurposeAddDialog {
    public static open(modalService: NgbModal, purposes : DsaPurpose[]) {
        const modalRef = modalService.open(PurposeAddDialog, { backdrop : "static"});
        modalRef.componentInstance.resultData = jQuery.extend(true, [], purposes);

        return modalRef;
    }

    @Input() resultData : DsaPurpose[];
    title : string = '';
    detail : string = '';
    newPurpose : DsaPurpose = new DsaPurpose();

    constructor(public activeModal: NgbActiveModal,
                private log:LoggerService) {}


    Add() {
        this.newPurpose.title = this.title;
        this.newPurpose.detail = this.detail;
        this.resultData.push(this.newPurpose);
        this.activeModal.close(this.resultData);
        console.log('OK Pressed');
    }

    cancel() {
        this.activeModal.dismiss('cancel');
        console.log('Cancel Pressed');
    }
}

import {Component, Input} from "@angular/core";
import {LoggerService} from "eds-common-js";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {Purpose} from "./models/Purpose";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./purposeAdd.html')
})
export class PurposeAddDialog {
    public static open(modalService: NgbModal, purposes : Purpose[], type : string) {
        const modalRef = modalService.open(PurposeAddDialog, { backdrop : "static"});
        modalRef.componentInstance.resultData = jQuery.extend(true, [], purposes);
        modalRef.componentInstance.type = type;

        return modalRef;
    }

    @Input() resultData : Purpose[];
    @Input() type : string;
    title : string = '';
    detail : string = '';


    constructor(public activeModal: NgbActiveModal,
                private log:LoggerService) {}


    Add() {
        var newPurpose : Purpose = new Purpose();
        newPurpose.title = this.title;
        newPurpose.detail = this.detail;
        this.resultData.push(newPurpose);
        this.activeModal.close(this.resultData);
    }

    AddAnother() {
        var newPurpose : Purpose = new Purpose();
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

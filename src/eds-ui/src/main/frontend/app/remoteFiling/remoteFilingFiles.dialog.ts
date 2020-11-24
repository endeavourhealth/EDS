import {Component, Input} from "@angular/core";
import {LoggerService, MessageBoxDialog} from "eds-common-js";
import {NgbModal, NgbActiveModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";

import {SubscriberZipFileUUID} from "./models/SubscriberZipFileUUID";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./remoteFilingFilesDialog.html')
})
export class RemoteFilingFilesDialog  {

    @Input() files: SubscriberZipFileUUID[];
    @Input() subscriberId: number;

    constructor(private $modal: NgbModal,
                public activeModal: NgbActiveModal) {
    }


    public static open(modalService: NgbModal, files: SubscriberZipFileUUID[], subscriberId: number) {

        const modalRef = modalService.open(RemoteFilingFilesDialog, { backdrop : "static", size : "lg"} as NgbModalOptions);
        modalRef.componentInstance.files = files;
        modalRef.componentInstance.subscriberId = subscriberId;

        return modalRef;
    }


    close() {
        this.activeModal.dismiss();
    }

    messageDialog(title: string, message: string) {
        MessageBoxDialog.open(
            this.$modal,
            title,
            message,
            'OK',
            '');
    }
}

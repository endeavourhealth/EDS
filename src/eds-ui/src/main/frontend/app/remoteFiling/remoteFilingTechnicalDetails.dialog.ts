import {Component, Input} from "@angular/core";
import {LoggerService, MessageBoxDialog} from "eds-common-js";
import {NgbModal, NgbActiveModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";

import {SubscriberZipFileUUID} from "./models/SubscriberZipFileUUID";
import {RemoteFilingService} from "./remoteFiling.service";
import {DateTimeFormatter} from "../utility/DateTimeFormatter";
import {RemoteFiler} from "./models/RemoteFiler";
import {RemoteFilerError} from "./models/RemoteFilerError";
import {linq} from "eds-common-js/dist/index";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./remoteFilingTechnicalDetailsDialog.html')
})
export class RemoteFilingTechnicalDetailsDialog  {

    //SD-338 - need to import the static formatting functions so they can be used by the HTML template
    formatYYYYMMDDHHMMSS = DateTimeFormatter.formatYYYYMMDDHHMMSS;

    @Input() filer: RemoteFiler;

    constructor(private $modal: NgbModal,
                protected log : LoggerService,
                public activeModal: NgbActiveModal,
                private remoteFilingService: RemoteFilingService) {
    }

    public static open(modalService: NgbModal, filer: RemoteFiler) {

        const modalRef = modalService.open(RemoteFilingTechnicalDetailsDialog, { backdrop : "static", size : "lg"} as NgbModalOptions);
        modalRef.componentInstance.filer = filer;

        return modalRef;
    }

    close() {
        this.activeModal.dismiss();
    }

}
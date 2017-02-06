import {Component, Input} from "@angular/core";
import {LoggerService} from "../common/logger.service";
import {ExchangeAuditService} from "./exchangeAudit.service";
import {NgbModal, NgbActiveModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {TransformErrorDetail} from "./TransformErrorDetail";
import {Subscription} from "rxjs/Subscription";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./transformErrorsDialog.html')
})
export class TransformErrorsDialog  {

    @Input() transformAudit: TransformErrorDetail;
    @Input() serviceId: string;
    @Input() systemId: string;
    lines: string[];
    busyLoadingLines: Subscription;

    constructor(public activeModal: NgbActiveModal,
                private log:LoggerService,
                private exchangeAuditService : ExchangeAuditService) {

    }


    public static open(modalService: NgbModal, serviceId: string, systemId: string, transformAudit: TransformErrorDetail) {

        const modalRef = modalService.open(TransformErrorsDialog, { backdrop : "static", size : "lg"} as NgbModalOptions);
        //const modalRef = modalService.open(TransformErrorsDialog, { backdrop : "static", size : "lg"});

        modalRef.componentInstance.transformAudit = transformAudit;
        modalRef.componentInstance.systemId = systemId;
        modalRef.componentInstance.serviceId = serviceId;

        //start the loading of the lines
        modalRef.componentInstance.loadLines();

        return modalRef;
    }

    private loadLines() {

        var exchangeId = this.transformAudit.exchangeId;
        var version = this.transformAudit.version;
        this.exchangeAuditService.getTransformErrorLines(this.serviceId, this.systemId, exchangeId, version).subscribe(
            (result) => {
                this.lines = result;

                //clear down to say we're not busy
              //  this.busyLoadingLines = null;
            },
            (error) => {
                this.log.error('Failed to retrieve error lines', error, 'View Error')

                //clear down to say we're not busy
                this.busyLoadingLines = null;
            }
        )
    }

    close() {
        this.activeModal.dismiss();
        //console.log('Cancel Pressed');
    }
}

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
    template: require('./remoteFilingFilesDialog.html')
})
export class RemoteFilingFilesDialog  {

    //SD-338 - need to import the static formatting functions so they can be used by the HTML template
    formatYYYYMMDDHHMMSS = DateTimeFormatter.formatYYYYMMDDHHMMSS;

    @Input() filer: RemoteFiler;
    @Input() errors: RemoteFilerError[];
    showLimitWarning: boolean;

    /*@Input() files: SubscriberZipFileUUID[];
    @Input() subscriberId: number;
    @Input() view: string;
    @Input() totalSubscriberFiles: number;

    pageNumber = 1;*/

    constructor(private $modal: NgbModal,
                protected log : LoggerService,
                public activeModal: NgbActiveModal,
                private remoteFilingService: RemoteFilingService) {
    }

    public static open(modalService: NgbModal, filer: RemoteFiler, errors: RemoteFilerError[]) {

        const modalRef = modalService.open(RemoteFilingFilesDialog, { backdrop : "static", size : "lg"} as NgbModalOptions);
        modalRef.componentInstance.filer = filer;
        modalRef.componentInstance.errors = errors;
        modalRef.componentInstance.formatForDisplay();

        /*modalRef.componentInstance.files = files;
        modalRef.componentInstance.subscriberId = subscriberId;
        modalRef.componentInstance.view = view;
        modalRef.componentInstance.totalSubscriberFiles = totalSubscriberFiles;*/

        return modalRef;
    }

    close() {
        this.activeModal.dismiss();
    }

    formatForDisplay() {
        var vm = this;

        //the server only returns the latest 101 errors for each service, to avoid returning
        //to much data. However, for simplicity, just display the 50 most recent in total.
        if (vm.errors.length > 50) {
            var sorted = linq(vm.errors).OrderBy(s => s.dtInserted)
                .Reverse()
                .ToArray();

            vm.errors = [];
            for (var i=0; i<50; i++) {
                vm.errors.push(sorted[i]);
            }

            vm.showLimitWarning = true;
        }
    }

    downloadMessage(error: RemoteFilerError) {
        var vm = this;
        var queuedMessageId = error.queuedMessageId;
        vm.remoteFilingService.getQueuedMessage(queuedMessageId)
            .subscribe(
                (result) => {
                    vm.downloadQueuedMessageData(queuedMessageId, result);
                },
                (error) => {
                    vm.log.error('Failed to get queued message data');
                }
            );
    }

    downloadQueuedMessageData(messageUUID: string, data: string) {
        const link = document.createElement('a');
        link.setAttribute('target', '_blank');
        link.setAttribute('href', 'data:application/zip;base64,'+data);
        link.setAttribute('download', messageUUID + '.zip');
        document.body.appendChild(link);
        link.click();
        link.remove();
    }

    /*downloadZipData(messageUUID: string, data: string) {
        const link = document.createElement('a');
        link.setAttribute('target', '_blank');
        link.setAttribute('href', 'data:application/zip;base64,'+data);
        link.setAttribute('download', messageUUID + '.zip');
        document.body.appendChild(link);
        link.click();
        link.remove();
     }

    messageDialogLarge(title: string, message: string) {
        MessageBoxDialog.openLarge(
            this.$modal,
            title,
            message,
            'OK',
            '');
    }

    pageChanged($event) {
        const vm = this;
        vm.pageNumber = $event;
        vm.getPagedFiles();
    }

    getPagedFiles() {
        const vm = this;

        vm.remoteFilingService.getSubscriberPagedFiles(vm.subscriberId, vm.pageNumber, 50)
            .subscribe(
                (result) => {
                    vm.files = result;
                },
                (error) => vm.log.error('Failed to load files', error, 'Load files')
            )
    }

    isFullyHistoryView() {
        const vm = this;
        if (vm.view.endsWith('filing history (all)')) {
            return true;
        }
        return false;
    }*/
}
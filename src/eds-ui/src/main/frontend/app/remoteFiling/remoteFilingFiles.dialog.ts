import {Component, Input} from "@angular/core";
import {LoggerService, MessageBoxDialog} from "eds-common-js";
import {NgbModal, NgbActiveModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";

import {SubscriberZipFileUUID} from "./models/SubscriberZipFileUUID";
import {RemoteFilingService} from "./remoteFiling.service";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./remoteFilingFilesDialog.html')
})
export class RemoteFilingFilesDialog  {

    @Input() files: SubscriberZipFileUUID[];
    @Input() subscriberId: number;
    @Input() view: string;
    @Input() totalSubscriberFiles: number;

    pageNumber = 1;

    constructor(private $modal: NgbModal,
                protected log : LoggerService,
                public activeModal: NgbActiveModal,
                private remoteFilingService: RemoteFilingService) {
    }

    public static open(modalService: NgbModal, files: SubscriberZipFileUUID[], subscriberId: number, view: string, totalSubscriberFiles: number) {

        const modalRef = modalService.open(RemoteFilingFilesDialog, { backdrop : "static", size : "lg"} as NgbModalOptions);
        modalRef.componentInstance.files = files;
        modalRef.componentInstance.subscriberId = subscriberId;
        modalRef.componentInstance.view = view;
        modalRef.componentInstance.totalSubscriberFiles = totalSubscriberFiles;

        return modalRef;
    }

    close() {
        this.activeModal.dismiss();
    }

    downloadZipData(messageUUID: string, data: string) {

        const vm = this;

        console.log('downloadZipData method called');

        let zip = require('jszip');

        console.log('call zip.loadAsync');
        zip.loadAsync(data, {base64: true})
            .then(function (blob) {
                // will be called, even if content is corrupted
                    console.log('loadAsync completed');
                    let FileSaver = require('file-saver');
                    FileSaver.saveAs(blob,  messageUUID + ".zip");
                    console.log('saveAs completed');
            },
                (error) => {
                    vm.log.error('Failed to create zip file', error, 'Zip file')
                    console.log('error calling loadAsync')
                }
            )
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
    }
}

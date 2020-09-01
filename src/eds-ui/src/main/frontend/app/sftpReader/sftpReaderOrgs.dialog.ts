import {Component, Input} from "@angular/core";
import {LoggerService} from "eds-common-js";
import {NgbModal, NgbActiveModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {Subscription} from "rxjs/Subscription";
import {SftpReaderService} from "./sftpReader.service";
import {SftpReaderChannelBatch} from "./models/SftpReaderChannelBatch";
import {SftpReaderConfiguration} from "./models/SftpReaderConfiguration";
import {OdsSearchDialog} from "../services/odsSearch.dialog";
import {SftpReaderBatchContents} from "./models/SftpReaderBatchContents";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./sftpReaderOrgsDialog.html')
})
export class SftpReaderOrgsDialog  {

    @Input() configurationId: string;
    @Input() batchId: number;
    @Input() contents: SftpReaderBatchContents[];

    searchFromStr: string;
    searchToStr: string;
    searching: boolean;
    results: SftpReaderChannelBatch[];
    //resultStr: string;

    constructor(private $modal: NgbModal,
                public activeModal: NgbActiveModal,
                private log: LoggerService,
                private sftpReaderService: SftpReaderService) {

    }


    public static open(modalService: NgbModal, configurationId: string, batchId: number, contents: SftpReaderBatchContents[]) {

        const modalRef = modalService.open(SftpReaderOrgsDialog, { backdrop : "static", size : "lg"} as NgbModalOptions);
        modalRef.componentInstance.configurationId = configurationId;
        modalRef.componentInstance.batchId = batchId;
        modalRef.componentInstance.contents = contents;

        return modalRef;
    }


    close() {
        this.activeModal.dismiss();
    }

    selectAllErrors() {
        var vm = this;

        for (var i=0; i<vm.contents.length; i++) {
            var batchSplit = vm.contents[i] as SftpReaderBatchContents;
            if (!batchSplit.notified) { //in error
                batchSplit.selected = true;
            }
        }
    }

    ignoreSelectedErrors() {
        var vm = this;

        var selected = [];
        for (var i=0; i<vm.contents.length; i++) {
            var batchSplit = vm.contents[i] as SftpReaderBatchContents;
            if (batchSplit.selected) {
                selected.push(batchSplit);
            }
        }

        if (selected.length == 0) {
            vm.log.warning("No errors selected");
            return;
        }

        var reason = prompt('Enter reason');
        if (reason == null) {
            return;
        }
        reason = 'Manually ignored: ' + reason; //add prefix so it's clear where it came from

        for (var j=0; j<selected.length; j++) {
            var batchSplit = selected[j] as SftpReaderBatchContents;
            vm.ignoreBatchSplit(batchSplit, reason);
        }
    }

    ignoreBatchSplit(batchSplit: SftpReaderBatchContents, reason: string) {
        var vm = this;

        vm.sftpReaderService.ignoreBatchSplit(vm.batchId, batchSplit.batchSplitId, vm.configurationId, reason).subscribe(
            (result) => {
                batchSplit.error = null;
                batchSplit.result = reason;
                batchSplit.notified = true;
            },
            (error) => {
                vm.log.error('Failed to ignore batch split', error, 'SFTP Reader Error');
            }
        )
    }

    /*search() {
        var vm = this;

        var dFrom;
        if (vm.searchFromStr) {
            dFrom = new Date(vm.searchFromStr);
        } else {
            dFrom = new Date('1900-01-01');
        }


        var dTo;
        if (vm.searchToStr) {
            dTo = new Date(vm.searchToStr);
        } else {
            dTo = new Date('9999-12-31');
        }


        vm.searching = true;

        var configurationId = vm.configuration.configurationId;
        vm.sftpReaderService.getSftpReaderHistory(configurationId, dFrom, dTo).subscribe(
            (result) => {
                vm.searching = false;
                vm.results = result;
            },
            (error) => {
                vm.searching = false;
                vm.log.error('Error searching');
            }
        );
    }*/
}

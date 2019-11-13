import {Component, Input} from "@angular/core";
import {LoggerService} from "eds-common-js";
import {NgbModal, NgbActiveModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {Subscription} from "rxjs/Subscription";
import {SftpReaderService} from "./sftpReader.service";
import {SftpReaderChannelStatus} from "./SftpReaderChannelStatus";
import {SftpReaderChannelBatch} from "./SftpReaderChannelBatch";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./sftpReaderHistoryDialog.html')
})
export class SftpReaderHistoryDialog  {

    @Input() status : SftpReaderChannelStatus;

    searchFromStr: string;
    searchToStr: string;
    searching: boolean;
    results: SftpReaderChannelBatch[];
    //resultStr: string;

    constructor(public activeModal: NgbActiveModal,
                private log: LoggerService,
                private sftpReaderService: SftpReaderService) {

    }


    public static open(modalService: NgbModal, status: SftpReaderChannelStatus) {

        const modalRef = modalService.open(SftpReaderHistoryDialog, { backdrop : "static", size : "lg"} as NgbModalOptions);
        modalRef.componentInstance.status = status;

        //default search from last 7 days
        var d = new Date();
        d.setDate(d.getDate() - 7);

        var s = d.getFullYear() + '-' + (d.getMonth()+1) + '-';
        if (d.getDate() < 10) {
            s += '0';
        }
        s += d.getDate();

        modalRef.componentInstance.searchFromStr = s;
        modalRef.componentInstance.searchToStr = '';

        return modalRef;
    }


    close() {
        this.activeModal.dismiss();
        //console.log('Cancel Pressed');
    }

    search() {
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

        vm.sftpReaderService.getSftpReaderHistory(vm.status.id, dFrom, dTo).subscribe(
            (result) => {
                vm.searching = false;
                vm.results = result;

                /*if (result) {
                    vm.resultStr = JSON.stringify(result, null, 2);
                } else {
                    vm.resultStr = 'no match found';
                }*/
            },
            (error) => {
                vm.searching = false;
                vm.log.error('Error searching');
            }
        );
    }
}

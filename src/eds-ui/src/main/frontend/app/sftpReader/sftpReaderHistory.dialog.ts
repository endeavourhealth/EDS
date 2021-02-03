import {Component, Input} from "@angular/core";
import {LoggerService} from "eds-common-js";
import {NgbModal, NgbActiveModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {Subscription} from "rxjs/Subscription";
import {SftpReaderService} from "./sftpReader.service";
import {SftpReaderChannelBatch} from "./models/SftpReaderChannelBatch";
import {SftpReaderConfiguration} from "./models/SftpReaderConfiguration";
import {OdsSearchDialog} from "../services/odsSearch.dialog";
import {SftpReaderOrgsDialog} from "./sftpReaderOrgs.dialog";
import {SftpReaderBatchContents} from "./models/SftpReaderBatchContents";
import {DateTimeFormatter} from "../utility/DateTimeFormatter";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./sftpReaderHistoryDialog.html')
})
export class SftpReaderHistoryDialog  {

    //SD-338 - need to import the static formatting functions so they can be used by the HTML template
    formatYYYYMMDDHHMM = DateTimeFormatter.formatYYYYMMDDHHMM;

    @Input() configuration: SftpReaderConfiguration;

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


    public static open(modalService: NgbModal, configuration: SftpReaderConfiguration) {

        const modalRef = modalService.open(SftpReaderHistoryDialog, { backdrop : "static", size : "lg"} as NgbModalOptions);
        modalRef.componentInstance.configuration = configuration;

        //default search from last 7 days
        var d = new Date();
        d.setDate(d.getDate() - 7);

        var s = d.getFullYear() + '-';
        if (d.getMonth()+1 < 10) {
            s += '0';
        }
        s += (d.getMonth()+1) + '-';
        if (d.getDate() < 10) {
            s += '0';
        }
        s += d.getDate();

        //console.log('setting to [' + s + ']');
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
            //console.log('From ' + vm.searchFromStr);
            dFrom = new Date(vm.searchFromStr);
        } else {
            //console.log('From dawn of time');
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
    }

    filterOrgs(arr: SftpReaderBatchContents[], wantOk: boolean): SftpReaderBatchContents[] {
        var ret = [];

        var i;
        for (i=0; i<arr.length; i++) {
            var c = arr[i];
            if (c.notified == wantOk) {
                ret.push(c);
            }
        }
        return ret;
    }

    viewOrgs(batch: SftpReaderChannelBatch) {
        var vm = this;
        console.log('viewing all orgs in batch');
        console.log(batch);
        vm.viewOrgsImpl(batch, batch.batchContents);
    }

    viewOrgsOk(batch: SftpReaderChannelBatch) {
        var vm = this;
        var orgsOk = vm.filterOrgs(batch.batchContents, true);
        vm.viewOrgsImpl(batch, orgsOk);
    }

    viewOrgsError(batch: SftpReaderChannelBatch) {
        var vm = this;
        var orgsError = vm.filterOrgs(batch.batchContents, false);
        vm.viewOrgsImpl(batch, orgsError);
    }

    private viewOrgsImpl(batch: SftpReaderChannelBatch, orgs: SftpReaderBatchContents[]) {
        var vm = this;
        var configurationId = vm.configuration.configurationId;
        var batchId = batch.id;
        SftpReaderOrgsDialog.open(vm.$modal, configurationId, batchId, orgs);
    }
}

import {linq, LoggerService} from "eds-common-js";
import {StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {Subscription} from "rxjs/Subscription";
import {ServiceService} from "../services/service.service";
import {SftpReaderService} from "./sftpReader.service";
import {SftpReaderChannelStatus} from "./SftpReaderChannelStatus";
import {SftpReaderBatchContents} from "./SftpReaderBatchContents";

@Component({
    template : require('./sftpReader.html')
})
export class SftpReaderComponent {

    //resultStr: string;
    statuses: SftpReaderChannelStatus[];

    constructor(protected sftpReaderService:SftpReaderService,
                protected logger:LoggerService,
                protected $state:StateService) {


    }

    ngOnInit() {
        this.refreshStatus();
    }

    refreshStatus() {
        var vm = this;

        vm.sftpReaderService.getSftpReaderStatus().subscribe(
            (result) => {

                vm.logger.success('Successfully got HL7 status', 'HL7 Status');
                vm.statuses = result;

                //vm.resultStr = JSON.stringify(result, null, 2);

                console.log('received HL7 status');
                console.log(result);
            },
            (error) => {
                vm.logger.error('Failed get HL7 Receiver status', error, 'HL7 Receiver');
            }
        )
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
}
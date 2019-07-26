import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {linq, LoggerService} from "eds-common-js";
import {StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {Subscription} from "rxjs/Subscription";
import {ServiceService} from "../services/service.service";
import {SftpReaderService} from "./sftpReader.service";
import {SftpReaderChannelStatus} from "./SftpReaderChannelStatus";
import {SftpReaderBatchContents} from "./SftpReaderBatchContents";
import {OdsSearchDialog} from "../services/odsSearch.dialog";
import {SftpReaderInstance} from "./SftpReaderInstance";

@Component({
    template : require('./sftpReader.html')
})
export class SftpReaderComponent {

    //resultStr: string;
    instanceNames: SftpReaderInstance[];
    filterInstanceName: string;
    statuses: SftpReaderChannelStatus[];
    resultStr: string;
    showRawJson: boolean;
    refreshingStatus: boolean;

    constructor(private $modal : NgbModal,
                protected sftpReaderService:SftpReaderService,
                protected logger:LoggerService,
                protected $state:StateService) {


    }

    ngOnInit() {
        var vm = this;
        vm.filterInstanceName = 'active';
        vm.refreshInstances();
        vm.refreshStatus();
    }

    refreshInstances() {
        var vm = this;
        vm.sftpReaderService.getSftpReaderInstances().subscribe(
            (result) => {
                vm.instanceNames = result;
            },
            (error) => {
                vm.logger.error('Failed get SFTP Reader instances', error, 'SFTP Reader');
            }
        )
    }

    refreshStatus() {
        var vm = this;
        vm.refreshingStatus = true;
        console.log('vm.refreshingStatus = ' + vm.refreshingStatus);

        vm.sftpReaderService.getSftpReaderStatus(vm.filterInstanceName).subscribe(
            (result) => {
                vm.refreshingStatus = false;
                console.log('vm.refreshingStatus = ' + vm.refreshingStatus);

                vm.logger.success('Successfully got HL7 status', 'HL7 Status');
                vm.statuses = result;

                vm.resultStr = JSON.stringify(result, null, 2);

                console.log('received HL7 status');
                console.log(result);
            },
            (error) => {
                vm.refreshingStatus = false;
                console.log('vm.refreshingStatus = ' + vm.refreshingStatus);

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

    getPanelClass(status: SftpReaderChannelStatus): string {
        if (status.instanceName) {
            return "panel panel-primary";
        } else {
            return "panel panel-info";
        }
    }

    odsSearch() {
        var vm = this;
        OdsSearchDialog.open(vm.$modal);

    }
}
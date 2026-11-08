import {linq, LoggerService} from "eds-common-js";
import {StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {Subscription} from "rxjs/Subscription";
import {ServiceService} from "../services/service.service";
import {DateTimeFormatter} from "../utility/DateTimeFormatter";
import {Hl7v2ApiStatus} from "./Hl7v2ApiStatus";
import {Hl7v2ApiService} from "./hl7v2Api.service";
import {QueueReaderStatusService} from "../queueReaderStatus/queueReaderStatus.service";
import {QueueReaderStatus} from "../queueReaderStatus/queueReaderStatus";

@Component({
    template : require('./hl7v2Api.html')
})
export class Hl7v2ApiComponent {

    //SD-338 - need to import the static formatting functions so they can be used by the HTML template
    formatYYYYMMDDHHMMSS = DateTimeFormatter.formatYYYYMMDDHHMMSS;
    formatYYYYMMDDHHMM = DateTimeFormatter.formatYYYYMMDDHHMM;
    formatHHMMSS = DateTimeFormatter.formatHHMMSS;
    getDateDiffSimpleFromNow = DateTimeFormatter.getDateDiffSimpleFromNow;

    //queue and message status
    refreshingStatus: boolean;
    statusLastRefreshed: Date;
    status: Hl7v2ApiStatus[];

    //application status
    refreshingApplicationStatus: boolean;
    applicationStatusLastRefreshed: Date;
    applicationStatus: QueueReaderStatus;

    constructor(private hl7ReceiverService: Hl7v2ApiService,
                private queueReaderStatusService: QueueReaderStatusService,
                private logger: LoggerService,
                private $state: StateService) {


    }

    ngOnInit() {
        this.refreshStatus();
    }

    refreshStatus() {
        var vm = this;
        vm.refreshingStatus = true;
        vm.statusLastRefreshed = new Date();
        vm.applicationStatusLastRefreshed = new Date();

        vm.hl7ReceiverService.getHl7ReceiverStatus().subscribe(
            (result) => {
                vm.logger.success('Successfully got HL7v2 API status', 'HL7v2 API Status');
                vm.status = result;
                vm.refreshingStatus = false;
            },
            (error) => {
                vm.logger.error('Failed get HL7v2 API status', error, 'HL7v2 API Status');
                vm.refreshingStatus = false;
            }
        );

        vm.queueReaderStatusService.getStatus('hl7v2_msg_sender').subscribe(
            (result) => {
                vm.applicationStatus = result[0];
                vm.refreshingApplicationStatus = false;

            },
            (error) => {
                vm.logger.error('Failed get application status', error);
                vm.refreshingApplicationStatus = false;
            }
        );
    }

    /**
     * if the last message received was >5 mins ago, then something is wrong
     */
    isLastMessageReceivedTooOld(channel: Hl7v2ApiStatus): boolean {
        var vm = this;

        //go back five minutes from when the status was refreshed
        var warningTime = vm.statusLastRefreshed.getTime() - (1000 * 60 * 5);

        return channel.lastReceivedTimestamp < warningTime;
    }

    /**
     * if the oldest message in the queue is >5 mins old, then something is wrong
     */
    isQueueudMessageTooOld(channel: Hl7v2ApiStatus): boolean {
        var vm = this;

        //if processing something from >1 day ago, that's wrong
        var warningTime = vm.statusLastRefreshed.getTime() - (1000 * 60 * 5);

        return channel.queueOldestTimestamp < warningTime;
    }

    isApplicationDead(status: QueueReaderStatus): boolean {
        var vm = this;
        //console.log('getting status');
        //console.log(status);
        var statusTime = status.timestmp;
        var warningTime = vm.applicationStatusLastRefreshed.getTime() - (1000 * 60 * 2);

        return statusTime < warningTime;
    }

    getApplicationAgeDesc(status: QueueReaderStatus): string {
        var vm = this;
        return DateTimeFormatter.getDateDiffDesc(new Date(status.timestmp), vm.applicationStatusLastRefreshed, 2);
    }

}
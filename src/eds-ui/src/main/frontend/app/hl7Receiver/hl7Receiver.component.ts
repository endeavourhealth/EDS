import {linq, LoggerService} from "eds-common-js";
import {StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {Subscription} from "rxjs/Subscription";
import {ServiceService} from "../services/service.service";
import {Hl7ReceiverService} from "./hl7Receiver.service";
import {Hl7ReceiverChannelStatus} from "./Hl7ReceiverChannelStatus";

@Component({
    template : require('./hl7Receiver.html')
})
export class Hl7ReceiverComponent {

    refreshingStatus: boolean;
    statusLastRefreshed: Date;
    channels: Hl7ReceiverChannelStatus[];
    resultStr: string;
    showRawJson: boolean;

    constructor(protected hl7ReceiverService:Hl7ReceiverService,
                protected logger:LoggerService,
                protected $state:StateService) {


    }

    ngOnInit() {
        this.refreshStatus();
    }

    refreshStatus() {
        var vm = this;
        vm.refreshingStatus = true;
        vm.statusLastRefreshed = new Date();

        vm.hl7ReceiverService.getHl7ReceiverStatus().subscribe(
            (result) => {
                vm.logger.success('Successfully got HL7 status', 'HL7 Status');
                vm.channels = result;
                vm.resultStr = JSON.stringify(result, null, 2);

                vm.refreshingStatus = false;
            },
            (error) => {
                vm.logger.error('Failed get HL7 Receiver status', error, 'HL7 Receiver');
                vm.refreshingStatus = false;
            }
        )
    }

    pauseChannel(channelId: number, pause: boolean) {
        var vm = this;
        vm.hl7ReceiverService.pauseChannel(channelId, pause).subscribe(
            (result) => {
                vm.refreshStatus();

                console.log('paused/unpaused OK');
            },
            (error) => {
                vm.logger.error('Failed to pause/unpause', error, 'HL7 Receiver');
            }
        )
    }

    isLastMessageReceivedTooOld(channel: Hl7ReceiverChannelStatus): boolean {
        var vm = this;

        //go back five minutes from when the status was refreshed
        var warningTime = vm.statusLastRefreshed.getTime() - (1000 * 60 * 5);

        return channel.lastMessageReceived < warningTime;
    }
}
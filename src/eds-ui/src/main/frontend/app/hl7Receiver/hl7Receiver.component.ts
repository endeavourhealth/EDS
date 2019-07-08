import {linq, LoggerService} from "eds-common-js";
import {StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {Subscription} from "rxjs/Subscription";
import {ServiceService} from "../services/service.service";
import {Hl7ReceiverService} from "./hl7Receiver.service";

@Component({
    template : require('./hl7Receiver.html')
})
export class Hl7ReceiverComponent {

    resultStr: string;

    constructor(protected hl7ReceiverService:Hl7ReceiverService,
                protected logger:LoggerService,
                protected $state:StateService) {


    }

    ngOnInit() {
        this.refreshStatus();
    }

    refreshStatus() {
        var vm = this;

        vm.hl7ReceiverService.getHl7ReceiverStatus().subscribe(
            (result) => {
                vm.logger.success('Successfully posted to exchange', 'Post to Exchange');
                console.log('received HL7 status');
                console.log(result);

                vm.resultStr = JSON.stringify(result, null, 2);
                //vm.refreshSummariesKeepingSelection(summary, result);
            },
            (error) => {
                vm.logger.error('Failed get HL7 Receiver status', error, 'HL7 Receiver');
                vm.resultStr = 'failed';
            }
        )
    }
}
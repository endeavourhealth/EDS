import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {linq, LoggerService} from "eds-common-js";
import {StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {Subscription} from "rxjs/Subscription";
import {ServiceService} from "../services/service.service";
import {SubscribersService} from "./subscribers.service";
import {OdsSearchDialog} from "../services/odsSearch.dialog";
import {ServiceListComponent} from "../services/serviceList.component";
import {QueueReaderStatusService} from "../queueReaderStatus/queueReaderStatus.service";
import {QueueReaderStatus} from "../queueReaderStatus/queueReaderStatus";
import {SubscriberConfiguration} from "./models/SubscriberConfiguration";
import {Transition} from "ui-router-ng2/ng2";

@Component({
    template : require('./subscriberDetail.html')
})
export class SubscriberDetailComponent {

    subscriberName: string;
    statusLastRefreshed: Date;
    status: SubscriberConfiguration;

    //subscriber status

    /*configurations: SubscribersConfiguration[];
     refreshingStatusMap: {};
     statusMap: {};
     statusesLastRefreshed: Date;*/


    constructor(private $modal:NgbModal,
                protected subscribersService:SubscribersService,
                private queueReaderStatusService:QueueReaderStatusService,
                protected logger:LoggerService,
                private transition : Transition,
                protected $state:StateService) {

        this.subscriberName = transition.params()['subscriberName'];
    }

    ngOnInit() {
        var vm = this;

        vm.refreshScreen();
    }

    refreshScreen() {
        var vm = this;
        //vm.refreshSubscribers(true);
    }

    refreshSubscribers() {
        var vm = this;
        vm.statusLastRefreshed = new Date();

        vm.subscribersService.getSubscriberDetails(vm.subscriberName).subscribe(
            (result) => {
                vm.status = result;
            },
            (error) => {
                vm.logger.error('Failed get subscriber details', error);
            }
        )
    }

    close() {
        var vm = this;
        vm.$state.go(vm.transition.from());
    }
}
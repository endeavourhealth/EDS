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
import {PublisherService} from "./models/PublisherService";
import {Service} from "../services/models/Service";

@Component({
    template : require('./subscriberDetail.html')
})
export class SubscriberDetailComponent {

    subscriberName: string;
    statusLastRefreshed: Date;
    status: SubscriberConfiguration;

    //subscriber status

    dateDiffCache: {}; //cache of date diff calculations

    /*configurations: SubscribersConfiguration[];
     refreshingStatusMap: {};
     statusMap: {};
     statusesLastRefreshed: Date;*/


    constructor(private $modal:NgbModal,
                protected subscribersService:SubscribersService,
                private serviceService : ServiceService,
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
        vm.dateDiffCache = {};
        vm.refreshSubscribers();
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

    getPublishersToShow(): PublisherService[] {
        var vm = this;
        if (!vm.status) {
            return [];
        }

        //TODO - add any filtering here

        var joined = JSON.stringify(vm.status, null, 2);
        console.log(joined);
        console.log('Returning ' + vm.status.publisherServices.length + ' publishers');
        return vm.status.publisherServices;
    }

    getDateDiff(fromMs: number): string {

        var vm = this;

        if (!from) {
            return 'n/a';
        }

        var ret = vm.dateDiffCache[fromMs];
        if (ret) {
            return ret;
        }

        var from = new Date();
        from.setTime(fromMs);

        var now = new Date();

        ret = ServiceListComponent.getDateDiffDesc(from, now, 2);
        vm.dateDiffCache[fromMs] = ret;

        return ret;
    }

    editPublisher(serviceUuid: string) {
        var vm = this;
        ServiceListComponent.editService(serviceUuid, vm.$state);
    }

    viewPublisherExchanges(serviceUuid: string) {
        var vm = this;
        vm.serviceService.get(serviceUuid).subscribe(
            (result) => {
                var service = result as Service;
                ServiceListComponent.viewExchanges(service, vm.$state, vm.$modal);
            },
            (error) => {
                vm.logger.error('Failed get service details', error);
            }
        )

    }
}
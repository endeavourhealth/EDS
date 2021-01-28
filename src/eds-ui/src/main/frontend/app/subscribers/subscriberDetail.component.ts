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
    filteredServices: PublisherService[];
    cachedSystemNames: string[];

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
                vm.cacheSystemNames();
                vm.applyFiltering();
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

    cacheSystemNames() {
        var vm = this;

        //we only need to do this once, so return if already done
        if (vm.cachedSystemNames) {
            return;
        }

        var vm = this;
        if (!vm.status) {
            return;
        }

        var list = [];

        var arrayLength = vm.status.publisherServices.length;
        for (var i = 0; i < arrayLength; i++) {
            var publisher = vm.status.publisherServices[i] as PublisherService;

            for (var j=0; j<publisher.systemStatus.length; j++) {
                var systemStatus = publisher.systemStatus[j];
                var systemName = systemStatus.name;

                if (list.indexOf(systemName) == -1) {
                    list.push(systemName);
                }
            }
        }

        list = linq(list).OrderBy(s => s.toLowerCase()).ToArray();
        vm.cachedSystemNames = list;
    }

    getDateDiff(fromMs: number): string {

        var vm = this;

        if (!fromMs) {
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

    getNonNullOdsCode(publisher: PublisherService): string {
        if (publisher && publisher.odsCode) {
            return publisher.odsCode;
        } else {
            return '';
        }
    }

    getLastDataCutoff(publisher: PublisherService): number {

        var ret;

        //if our publisher has multiple systems, always use the OLDER date
        if (publisher && publisher.systemStatus) {

            for (var i=0; i<publisher.systemStatus.length; i++) {
                var systemStatus = publisher.systemStatus[i];
                var cutoff = systemStatus.lastReceivedExtractCutoff;
                if (cutoff
                    && (!ret || cutoff < ret)) {
                    ret = cutoff;
                }
            }
        }

        if (!ret) {
            ret = 0;
        }
        return ret;
    }

    /**
     * filters the list of publishers accoring to options selected and caches
     */
    applyFiltering() {
        var vm = this;

        var vm = this;
        if (!vm.status) {
            return;
        }

        var filtered = [];

        //work out if the name/ID search text is valid regex and force it to lower case if so
        var validNameFilterRegex;
        if (vm.subscribersService.publisherNameFilter) {
            try {
                new RegExp(vm.subscribersService.publisherNameFilter);
                validNameFilterRegex = vm.subscribersService.publisherNameFilter.toLowerCase().trim();
            } catch (e) {
                //do nothing
            }
        }


        var arrayLength = vm.status.publisherServices.length;
        for (var i = 0; i < arrayLength; i++) {
            var publisher = vm.status.publisherServices[i] as PublisherService;

            //only apply the name filter if it's valid regex
            if (validNameFilterRegex) {
                var name = publisher.name;
                var alias = publisher.alias;
                var id = publisher.odsCode;
                var uuid = publisher.uuid;

                var include = false;
                if (name && name.toLowerCase().match(validNameFilterRegex)) {
                    include = true;

                } if (alias && alias.toLowerCase().match(validNameFilterRegex)) {
                    include = true;

                } else if (id && id.toLowerCase().match(validNameFilterRegex)) {
                    include = true;

                } else if (uuid && uuid.toLowerCase() == validNameFilterRegex) { //don't compare this as regex
                    include = true;

                }

                if (!include) {
                    continue;
                }
            }

            filtered.push(publisher);
        }

        //always sort by name first
        filtered = linq(filtered).OrderBy(s => s.name.toLowerCase()).ToArray();

        if (vm.subscribersService.sortFilter == 'NameAsc') {
            //no extra sorting

        } else if (vm.subscribersService.sortFilter == 'NameDesc') {
            //already sorted by name asc, so just rverse
            filtered = filtered.reverse();

        } else if (vm.subscribersService.sortFilter == 'IDAsc') {
            filtered = linq(filtered).OrderBy(s => vm.getNonNullOdsCode(s)).ToArray();

        } else if (vm.subscribersService.sortFilter == 'IDDesc') {
            filtered = linq(filtered).OrderBy(s => vm.getNonNullOdsCode(s)).ToArray();
            filtered = filtered.reverse();

        } else if (vm.subscribersService.sortFilter == 'LastDataAsc') {
            filtered = linq(filtered).OrderBy(s => vm.getLastDataCutoff(s)).ToArray();
            filtered = filtered.reverse();

        } else if (vm.subscribersService.sortFilter == 'LastDataDesc') {
            filtered = linq(filtered).OrderBy(s => vm.getLastDataCutoff(s)).ToArray();

        } else {
            console.log('unknown sort mode ' + vm.subscribersService.sortFilter);
        }

        vm.filteredServices = filtered;
    }


}
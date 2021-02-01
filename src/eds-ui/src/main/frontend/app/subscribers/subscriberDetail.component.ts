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
import {PublisherSystem} from "./models/PublisherSystem";

@Component({
    template : require('./subscriberDetail.html')
})
export class SubscriberDetailComponent {

    subscriberName: string;
    statusLastRefreshed: Date;
    status: SubscriberConfiguration;
    filteredServices: PublisherService[];
    cachedSystemNames: string[];
    refreshingStatus: boolean;
    selectedPublisher: PublisherService;
    dayDuration: number;

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
        vm.dayDuration = 1000 * 60 * 60 * 24;
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
        vm.refreshingStatus = true;
        vm.selectedPublisher = null;

        vm.subscribersService.getSubscriberDetails(vm.subscriberName).subscribe(
            (result) => {
                vm.status = result;
                vm.cacheSystemNames();
                vm.calculateWarnings();
                vm.applyFiltering();
                vm.refreshingStatus = false;
            },
            (error) => {
                vm.logger.error('Failed get subscriber details', error);
                vm.refreshingStatus = false;
            }
        )
    }

    /**
     * goes through the publisher status objects and works out any warnings
     */
    calculateWarnings() {
        var vm = this;
        if (!vm.status) {
            return;
        }

        var arrayLength = vm.status.publisherServices.length;
        for (var i = 0; i < arrayLength; i++) {
            var publisher = vm.status.publisherServices[i] as PublisherService;

            for (var j=0; j<publisher.systemStatus.length; j++) {
                var systemStatus = publisher.systemStatus[j] as PublisherSystem;

                var inboundBehindDays = 0;
                var inboundBehindWarning = null;
                var outboundBehindDays = 0;
                var outboundBehindWarning = null;


                if (systemStatus.lastReceivedExtractCutoff) {

                    //inbound warning if in inbound error
                    if (systemStatus.lastProcessedInExtractCutoff) {

                        //work out if we're behind in inbound processing
                        var msBehind = systemStatus.lastReceivedExtractCutoff - systemStatus.lastProcessedInExtractCutoff;
                        var behindDesc = ServiceListComponent.getDateDiffDescMs(systemStatus.lastProcessedInExtractCutoff, systemStatus.lastReceivedExtractCutoff, 2);
                        inboundBehindDays = msBehind / vm.dayDuration;
                        inboundBehindWarning = 'Inbound processing ' + behindDesc + ' behind';

                    } else {
                        //if never finished any inbound processing
                        inboundBehindDays = 999; //just some arbitrary max value
                        inboundBehindWarning = 'No inbound processing complete';
                    }

                    //outbound warning if 2+ days behind
                    if (systemStatus.lastProcessedOutExtractCutoff) {

                        //check if behind in outbound processing
                        var msBehind = systemStatus.lastReceivedExtractCutoff - systemStatus.lastProcessedOutExtractCutoff;
                        var behindDesc = ServiceListComponent.getDateDiffDescMs(systemStatus.lastProcessedOutExtractCutoff, systemStatus.lastReceivedExtractCutoff, 2);
                        outboundBehindDays = msBehind / vm.dayDuration;
                        outboundBehindWarning = 'Outbound processing ' + behindDesc + ' behind';

                    } else {
                        outboundBehindDays = 999; //just some arbitrary max value
                        outboundBehindWarning = 'No inbound processing complete';
                    }
                }

                systemStatus.inboundBehindDays = inboundBehindDays;
                systemStatus.inboundBehindWarning = inboundBehindWarning;
                systemStatus.outboundBehindDays = outboundBehindDays;
                systemStatus.outboundBehindWarning = outboundBehindWarning;
            }


        }
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

        //NOTE: despite repeated attempts, the CLOSE button on the next page doesn't work, so
        //to avoid errors being thrown, we close the current view (i.e. return to previous page)
        //and then invoke the new page from that.

        //first close this view
        vm.$state.go(vm.transition.from());

        //then invoke the new one from the new view
        setTimeout(()=>{
            ServiceListComponent.editService(serviceUuid, vm.$state);
        }, 2000);
    }

    viewPublisherExchanges(serviceUuid: string) {

        var vm = this;

        //NOTE: despite repeated attempts, the CLOSE button on the next page doesn't work, so
        //to avoid errors being thrown, we close the current view (i.e. return to previous page)
        //and then invoke the new page from that.

        //first close this view
        vm.$state.go(vm.transition.from());

        //then invoke the new one from the new view
        setTimeout(()=>{
            vm.serviceService.get(serviceUuid).subscribe(
                (result) => {
                    var service = result as Service;
                    ServiceListComponent.viewExchanges(service, vm.$state, vm.$modal);
                },
                (error) => {
                    vm.logger.error('Failed get service details', error);
                }
            )

        }, 2000);
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
            //console.log('Doing ' + publisher.odsCode);

            if (vm.subscribersService.statusFilter) {
                //console.log('vm.subscribersService.statusFilter = ' + vm.subscribersService.statusFilter);
                var include = false;

                for (var j=0; j<publisher.systemStatus.length; j++) {
                    var systemStatus = publisher.systemStatus[j];

                    if (vm.subscribersService.statusFilter == 'up-to-date') {
                        /*console.log('systemStatus.processingInError = ' + systemStatus.processingInError);
                        console.log('systemStatus.inboundBehindDays = ' + systemStatus.inboundBehindDays);
                        console.log('systemStatus.outboundBehindDays = ' + systemStatus.outboundBehindDays);*/

                        if (!systemStatus.processingInError
                            && systemStatus.inboundBehindDays == 0
                            && systemStatus.outboundBehindDays == 0) {
                            include = true;
                        }

                    } else if (vm.subscribersService.statusFilter == 'inbound-error') {

                        if (systemStatus.processingInError) {
                            include = true;
                        }

                    } else if (vm.subscribersService.statusFilter == 'any-behind') {

                        if (systemStatus.inboundBehindDays > 0
                            || systemStatus.outboundBehindDays > 0) {
                            include = true;
                        }

                    } else if (vm.subscribersService.statusFilter == 'severe-behind') {

                        if (systemStatus.inboundBehindDays > 1
                            || systemStatus.outboundBehindDays > 1) {
                            include = true;
                        }

                    } else if (vm.subscribersService.statusFilter == 'any-issue') {

                        if (systemStatus.processingInError
                            || systemStatus.inboundBehindDays > 1
                            || systemStatus.outboundBehindDays > 1) {
                            include = true;
                        }

                    } else {
                        console.log('Unknown status filter [' + vm.subscribersService.statusFilter + ']');
                    }
                }

                //console.log('include = ' + include);
                if (!include) {
                    continue;
                }
            }

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

            if (vm.subscribersService.systemNameFilter) {
                var include = false;

                for (var j=0; j<publisher.systemStatus.length; j++) {
                    var systemStatus = publisher.systemStatus[j];
                    if (systemStatus.name == vm.subscribersService.systemNameFilter) {
                        include = true;
                        break;
                    }
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

    getTagNames(publisher: PublisherService): string[] {
        var vm = this;
        var ret = [];

        var tags = publisher.tags;
        if (!tags) {
            return ret;
        }

        var allTagNames = vm.serviceService.getTagNamesFromCache();
        for (var i=0; i<allTagNames.length; i++) {
            var tagName = allTagNames[i];
            if (tags.hasOwnProperty(tagName)) {
                ret.push(tagName);
            }
        }

        return ret;
    }

    getTagValueDesc(tagName: string, publisher: PublisherService): string {
        var tags = publisher.tags;
        if (!tags) {
            return '';
        }

        var ret = tags[tagName];
        if (!ret) {
            ret = '';
        }
        return ret;
    }

    /**
     * returns the panel "class" definition for the system, depending on it's status
     */
    getPanelClass(systemStatus: PublisherSystem): string {
        var vm = this;

        //if nothing has been received, then it's a weird case
        if (!systemStatus.lastReceivedExtractCutoff) {
            return 'panel panel-info';
        }

        //any transform error or if we've never processed
        if (systemStatus.processingInError
            || !systemStatus.lastProcessedInExtractCutoff
            || !systemStatus.lastProcessedOutExtractCutoff) {
            return 'panel panel-danger';
        }

        //if we're WAY behind then make it red
        if (systemStatus.inboundBehindDays > 1 || systemStatus.outboundBehindDays > 1) {
            return 'panel panel-danger';
        }

        //if we're less than a day behind, make it amber
        if (systemStatus.inboundBehindDays > 0 || systemStatus.outboundBehindDays > 0) {
            return 'panel panel-warning';
        }

        return 'panel panel-success';
    }


    /**
     * saves current list to CSV
     */
    saveToCsv() {

        var vm = this;

        //create CSV content in a String
        var lines = [];
        var line;

        line = '\"Publisher Name\",' +
            '\"ODS Code\",' +
            '\"System\",' +
            '\"Inbound Error\",' +
            '\"Last Extract Received\",' +
            '\"Last Extract Cutoff\",' +
            '\"Complete Inbound Extract Cutoff\",' +
            '\"Complete Outbound Extract Cutoff\"';
        lines.push(line);

        for (var i=0; i<vm.filteredServices.length; i++) {
            var publisher = vm.filteredServices[i] as PublisherService;

            for (var j=0; j<publisher.systemStatus.length; j++) {
                var system = publisher.systemStatus[j];


                var cols = [];

                cols.push(publisher.name);
                cols.push(publisher.odsCode);
                cols.push(system.name);

                if (system.processingInError) {
                    cols.push(system.processingInErrorMessage);
                } else {
                    cols.push('');
                }

                if (system.lastReceivedExtract) {
                    var d = new Date();

                    d.setTime(system.lastReceivedExtract);
                    cols.push(ServiceListComponent.formatDate(d));

                    d.setTime(system.lastReceivedExtractCutoff);
                    cols.push(ServiceListComponent.formatDate(d));

                    if (system.lastProcessedInExtractCutoff) {
                        d.setTime(system.lastProcessedInExtractCutoff);
                        cols.push(ServiceListComponent.formatDate(d));

                    } else {
                        cols.push('None');
                    }

                    if (system.lastProcessedOutExtractCutoff) {
                        d.setTime(system.lastProcessedOutExtractCutoff);
                        cols.push(ServiceListComponent.formatDate(d));

                    } else {
                        cols.push('None');
                    }

                } else {

                    cols.push('Never');
                    cols.push('');
                }


                line = '\"' + cols.join('\",\"') + '\"';
                lines.push(line);
            }

        }

        var csvStr = lines.join('\r\n');

        const filename = vm.subscriberName + '_status.csv';
        const blob = new Blob([csvStr], { type: 'text/plain' });

        let url = window.URL.createObjectURL(blob);
        let a = document.createElement('a');
        document.body.appendChild(a);
        a.setAttribute('style', 'display: none');
        a.href = url;
        a.download = filename;
        a.click();
        window.URL.revokeObjectURL(url);
        a.remove();
    }



}
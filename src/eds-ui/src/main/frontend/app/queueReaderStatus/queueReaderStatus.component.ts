import {linq, LoggerService} from "eds-common-js";
import {StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {Subscription} from "rxjs/Subscription";
import {ServiceService} from "../services/service.service";
import {QueueReaderStatusService} from "./queueReaderStatus.service";
import {QueueReaderStatus} from "./queueReaderStatus";
import {RabbitService} from "../queueing/rabbit.service";
import {Routing} from "../queueing/Routing";
import {ServiceListComponent} from "../services/serviceList.component";
import {RabbitNode} from "../queueing/models/RabbitNode";
import {Service} from "../services/models/Service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
    template : require('./queueReaderStatus.html')
})
export class QueueReaderStatusComponent {

    //properties derived from Rabbit config
    routingExchangeNames: string[]; //distinct exchange names (EdsInbound, EdsProtocol etc.)
    routingKeys: string[]; //distinct set of all routing keys (A, B, C etc.)
    routingQueueNames: string[]; //set of all queue names (EdsInbound-A, EdsInbound-B etc.)
    routingQueueDescs: {}; //map of routing queue names to the description of what the queue is for
    rabbitNodes: RabbitNode[];

    //queue reader status
    statusLastRefreshed: Date;
    queueReaderStatusList: QueueReaderStatus[];
    queueReaderStatusMapByQueue: {};
    queueReaderStatusMapByHostName: {};
    queueReaderCpuMapByHostName: {};
    queueReaderPhysicalMemoryMapByHostName: {};
    queueReaderMemoryGraphMapByHostName: {};
    refreshingQueueReaderStatus: boolean;
    hostNames: string[];

    //rabbit status
    rabbitQueueStatus: {};
    refreshingRabbitQueueStatus: boolean;

    presetColours: string[];
    hostNameColourMap: {};

    constructor(private $modal : NgbModal,
                private serviceService : ServiceService,
                private queueReaderStatusService:QueueReaderStatusService,
                private rabbitService: RabbitService,
                private logger: LoggerService,
                private $state: StateService) {


    }

    ngOnInit() {
        var vm = this;
        vm.presetColours = ['#e6b0aa', '#d7bde2', '#a9cce3', '#a3e4d7', '#a9dfbf', '#f9e79f', '#f5cba7', '#e5e7e9', '#ccd1d1', '#abb2b9', '#f5b7b1', '#d2b4de', '#aed6f1', '#a2d9ce', '#abebc6', '#fad7a0 ', '#edbb99', '#d5dbdb', '#aeb6bf', '#f7f9f9'];
        vm.cacheRabbitConfig();
        vm.cacheRabbitNodes();


    }

    cacheRabbitNodes() {
        var vm = this;
        vm.rabbitNodes = null;
        vm.rabbitService.getRabbitNodes()
            .subscribe(
                data => {
                    vm.rabbitNodes = data;
                    vm.refreshRabbitQueues();
                },
                (error) => vm.logger.error('Failed to load Rabbit nodes', error, 'Load Rabbit nodes'));
    }

    refreshRabbitQueues() {
        var vm = this;
        vm.refreshingRabbitQueueStatus = true;

        var firstRabbitNode = vm.rabbitNodes[0];
        vm.rabbitService.getRabbitQueues(firstRabbitNode.address)
            .subscribe(
                result => {

                    vm.rabbitQueueStatus = {};

                    var len = result.length;
                    for (var i=0; i<len; i++) {
                        var queueStatus = result[i];
                        var name = queueStatus.name;
                        vm.rabbitQueueStatus[name] = queueStatus;
                    }

                    vm.refreshingRabbitQueueStatus = false;
                },
                (error) => {
                    vm.logger.error('Failed to load Rabbit queues', error, 'Load Rabbit queues')
                    vm.refreshingRabbitQueueStatus = false;
                }
            );
    }

    refreshStatus() {
        var vm = this;

        vm.refreshQueueReaders();
        vm.refreshRabbitQueues();
    }


    private cacheRabbitConfig() {
        var vm = this;
        vm.rabbitService.getRoutings()
            .subscribe(
                (result) => {
                    vm.populateRoutingMap(result);
                    vm.refreshQueueReaders();
                },
                (error) => vm.logger.error('Failed to load Rabbit routing', error, 'Load route groups')
            );
    }

    private populateRoutingMap(routings: Routing[]) {
        var vm = this;

        vm.routingExchangeNames = [];
        vm.routingKeys = [];
        vm.routingQueueNames = [];
        vm.routingQueueDescs = {};

        //get a distinct list of exchange names and routing keys from the array
        var len = routings.length;
        for (var i=0; i<len; i++) {
            var r = routings[i];

            var exchangeName = r.exchangeName;
            if ($.inArray(exchangeName, vm.routingExchangeNames) == -1) {
                vm.routingExchangeNames.push(exchangeName);
            }

            var routingKey = r.routeKey;
            if ($.inArray(routingKey, vm.routingKeys) == -1) {
                vm.routingKeys.push(routingKey);
            }

            var combined = vm.combineIntoQueueName(exchangeName, routingKey);
            vm.routingQueueNames.push(combined);

            var desc = r.description;
            vm.routingQueueDescs[combined] = desc;
        }

        //sort the routing key array so they appear as expected (but don't sort the exchange names)
        vm.routingKeys = linq(vm.routingKeys).OrderBy(s => s.toLowerCase()).ToArray();
    }

    refreshQueueReaders() {
        var vm = this;
        vm.refreshingQueueReaderStatus = true;
        vm.statusLastRefreshed = new Date();

        vm.queueReaderStatusService.getStatus('queuereader').subscribe(
            (result) => {

                vm.processQueueReaderResults(result);
                vm.refreshingQueueReaderStatus = false;

            },
            (error) => {
                vm.logger.error('Failed get Queue Reader status', error, 'Queue Readers');
                vm.refreshingQueueReaderStatus = false;
            }
        );
    }

    processQueueReaderResults(results: QueueReaderStatus[]): void {

        var vm = this;

        //clear any caches
        vm.queueReaderStatusMapByQueue = {};
        vm.queueReaderStatusMapByHostName = {};
        vm.queueReaderCpuMapByHostName = {};
        vm.queueReaderPhysicalMemoryMapByHostName = {};

        //filter the results to remove any with >1 instance numbers that aren't interesting

        //work out the maximum "interesting" >1 instance number for each instance name
        //console.log('Finding max num per app');

        var maxInterestingInstanceNumberPerApp = {};
        for (var i=0; i<results.length; i++) {
            var result = results[i];
            //console.log('Doing ' + result.applicationInstanceName + ' num ' + result.applicationInstanceNumber);
            if (result.applicationInstanceNumber > 1) {

                var instanceNum = result.applicationInstanceNumber;
                var currentNum = maxInterestingInstanceNumberPerApp[result.applicationInstanceName];
                //console.log('    current num = ' + currentNum);
                //console.log('    is too old = ' + vm.isStatusDead(result));
                if ((!currentNum || instanceNum > currentNum)
                     && !vm.isStatusDead(result)) {

                    maxInterestingInstanceNumberPerApp[result.applicationInstanceName] = instanceNum;
                    //console.log('max num for ' + result.applicationInstanceName + ' now ' + currentNum);
                }
            }
        }

        //then filter our results to only include >1 instance nums when they're interesting
        vm.queueReaderStatusList = [];

        //console.log('Filtering results');

        for (var i=0; i<results.length; i++) {
            var result = results[i];

            //if the instance num is >1 but it's higher than any that's currently running, then ignore it
            if (result.applicationInstanceNumber > 1) {
                var instanceNum = result.applicationInstanceNumber;
                var maxInteresting = maxInterestingInstanceNumberPerApp[result.applicationInstanceName];
                //console.log('Result for ' + result.applicationInstanceName + ' has num ' + instanceNum + ' and max interesting ' + maxInteresting) ;
                if (!maxInteresting
                    || instanceNum > maxInteresting) {
                    //console.log('Skipping');
                    continue;
                }
            }
            vm.queueReaderStatusList.push(result);

            //console.log(result);
        }

        //sort results by instance name (and then number)
        vm.queueReaderStatusList = linq(vm.queueReaderStatusList)
            .OrderBy(s => s.applicationInstanceName)
            .ThenBy(s => s.applicationInstanceNumber)
            .ToArray();

        //find the most recent CPU usage for each host
        var statusByTimestamp = linq(vm.queueReaderStatusList).OrderBy(s => s.timestmp).ToArray();
        for (var i=statusByTimestamp.length-1; i>=0; i--) {
            var status = statusByTimestamp[i] as QueueReaderStatus;
            var hostName = status.hostName;

            var cpu = status.cpuLoad;
            if (cpu) {
                if (!vm.queueReaderCpuMapByHostName[hostName]) {
                    vm.queueReaderCpuMapByHostName[hostName] = cpu + '%';
                }
            }

            var physicalMemoryDesc = status.physicalMemoryDesc;
            if (physicalMemoryDesc) {
                if (!vm.queueReaderPhysicalMemoryMapByHostName[hostName]) {
                    vm.queueReaderPhysicalMemoryMapByHostName[hostName] = physicalMemoryDesc;
                }
            }
        }

        /*var statusByName = linq(vm.queueReaderStatusList)
            .OrderBy(s => s.applicationInstanceName).ToArray();*/

        for (var i=0; i<vm.queueReaderStatusList.length; i++) {
            var status = vm.queueReaderStatusList[i] as QueueReaderStatus;
            var queueName = status.queueName;
            var hostName = status.hostName;

            var statusesForQueue = vm.queueReaderStatusMapByQueue[queueName];
            if (!statusesForQueue) {
                statusesForQueue = [];
                vm.queueReaderStatusMapByQueue[queueName] = statusesForQueue;
            }
            statusesForQueue.push(status);

            var statusesForHostName = vm.queueReaderStatusMapByHostName[hostName];
            if (!statusesForHostName) {
                statusesForHostName = [];
                vm.queueReaderStatusMapByHostName[hostName] = statusesForHostName;
            }
            statusesForHostName.push(status);
        }

        //need to work out distinct server names
        vm.calculateDistinctHostNames();

        //work out a quick visual indicator for memory usage
        vm.calculateMemoryGraphs();

        //generate unique colours for each host name
        vm.calculateColours();
    }

    calculateDistinctHostNames(): void {
        var vm = this;

        //get list of distinct host names
        vm.hostNames = [];

        var len = vm.queueReaderStatusList.length;
        for (var i=0; i<len; i++) {
            var s = vm.queueReaderStatusList[i];
            var hostName = s.hostName;
            if ($.inArray(hostName, vm.hostNames) == -1) {
                vm.hostNames.push(hostName);
            }
        }

        //sort list
        vm.hostNames = linq(vm.hostNames).OrderBy(s => s.toLowerCase()).ToArray();

    }

    calculateMemoryGraphs(): void {
        var vm = this;

        vm.queueReaderMemoryGraphMapByHostName = {};

        var memorySizeMap = {};
        var memoryUsedMap = {};

        for (var i=0; i<vm.queueReaderStatusList.length; i++) {
            var s = vm.queueReaderStatusList[i];
            var hostName = s.hostName;

            //get the physical memory size
            var memorySize = s.physicalMemoryMb;
            if (!memorySizeMap[hostName]) {
                memorySizeMap[hostName] = memorySize;
            }

            //and if the app is running, add up the max heap allocated
            if (!vm.isStatusDead(s)) {
                var memoryUsed = s.maxHeapMb;
                var used = memoryUsedMap[hostName];
                if (!used) {
                    used = 0;
                }
                used += memoryUsed;
                memoryUsedMap[hostName] = used;
            }
        }

        for (var i=0; i<vm.hostNames.length; i++) {
            var hostName = vm.hostNames[i];
            var memorySize = memorySizeMap[hostName] as number;
            var memoryUsed = memoryUsedMap[hostName] as number;

            var graph = '';

            //this won't be set until all QRs are re-deployed
            if (memorySize) {

                if (!memoryUsed) {
                    memoryUsed = 0;
                }
                var max = Math.round(memorySize / 1024); //convert MB into GB
                var current = Math.round((memoryUsed / memorySize) * max);

                //show the allocation
                for (var j = 0; j < current && j < max; j++) {
                    graph += 'O';
                }

                //if over-allocated, show the extra differently
                for (var j = max; j < current; j++) {
                    graph += 'X';
                }

                //pad out the string to the max
                while (graph.length < max) {
                    graph += "_";
                }
            }

            vm.queueReaderMemoryGraphMapByHostName[hostName] = graph;
        }
    }

    calculateColours():void {
        var vm = this;

        //assign a colour to each server name
        vm.hostNameColourMap = {};

        var len = vm.hostNames.length;
        for (var i=0; i<len; i++) {
            var hostName = vm.hostNames[i];

            var colour = null;
            if (i >= vm.presetColours.length) {
                //just handle the case if we have more servers than preset colours
                colour = '#101010';
            } else {
                colour = vm.presetColours[i];
            }

            vm.hostNameColourMap[hostName] = colour;
        }
    }


    isValidQueue(exchangeName: string, routingKey: string): boolean {
        var vm = this;
        var combined = vm.combineIntoQueueName(exchangeName, routingKey);
        return $.inArray(combined, vm.routingQueueNames) > -1;
    }

    private combineIntoQueueName(exchangeName: string, routingKey: string): string {
        return exchangeName + '-' + routingKey;
    }

    /*getHostName(exchangeName: string, routingKey: string): string {
        var vm = this;
        var queueName = vm.combineIntoQueueName(exchangeName, routingKey);

        var status = vm.latestStatusMap[queueName];
        if (!status) {
            return 'UNKNOWN HOST';
        }

        var ret = status.hostName;
        if (status.maxHeapDesc) {
            var percent = Math.round(100 * (status.currentHeapMb / status.maxHeapMb));
            ret += ' [' + percent + '% of ' + status.maxHeapDesc + ']';
        }
        return ret;
    }*/

    getStatusMemoryDesc(status: QueueReaderStatus): string {
        if (status.maxHeapDesc) {
            /*var percent = Math.round(100 * (status.currentHeapMb / status.maxHeapMb));
            return ' [' + percent + '% of ' + status.maxHeapDesc + ']';*/
            return ' [' + status.maxHeapDesc + ']';
        } else {
            return '';
        }
    }

    isStatusDead(status: QueueReaderStatus): boolean {
        var vm = this;

        var statusTime = status.timestmp;
        var warningTime = vm.statusLastRefreshed.getTime() - (1000 * 60 * 2);

        return statusTime < warningTime;
    }

    getStatusAgeDesc(status: QueueReaderStatus): string {
        var vm = this;
        return ServiceListComponent.getDateDiffDesc(new Date(status.timestmp), vm.statusLastRefreshed, 2);
    }

    getExecutionTime(status: QueueReaderStatus): string {
        var vm = this;
        if (!status.isBusySince) {
            return '';
        } else {
            return ServiceListComponent.getDateDiffDesc(new Date(status.isBusySince), new Date(status.timestmp), 1);
        }
    }


    getCellColour(status: QueueReaderStatus): any {
        var vm = this;
        var hostName = status.hostName;
        return vm.getCellColourForHostName(hostName);
    }

    getCellColourForHostName(hostName: string): any {
        var vm = this;
        var colour = vm.hostNameColourMap[hostName];
        return {'background-color': colour};
    }

    getStatusesForExchangeAndKey(exchangeName: string, routingKey: string): QueueReaderStatus[] {
        var vm = this;
        var queueName = vm.combineIntoQueueName(exchangeName, routingKey);
        var statuses = vm.queueReaderStatusMapByQueue[queueName];

        var ret = [];
        if (statuses) {
            for (var i=0; i<statuses.length; i++) {
                var status = statuses[i];
                if (vm.shouldShowStatus(status)) {
                    ret.push(status);
                }
            }
        }

        return ret;
    }

    getQueueSize(exchangeName: string, routingKey: string): number {
        var vm = this;
        var queueName = vm.combineIntoQueueName(exchangeName, routingKey);
        return vm.getQueueSizeForQueueName(queueName);
    }

    getQueueSizeForQueueName(queueName: string): number {
        var vm = this;
        if (!vm.rabbitQueueStatus) {
            return null;
        }
        var queueStatus = vm.rabbitQueueStatus[queueName];
        if (queueStatus) {
            return queueStatus.messages_ready;
        } else {
            return null;
        }
    }

    getStatusesForHostName(hostName: string): QueueReaderStatus[] {
        var vm = this;
        var statuses = vm.queueReaderStatusMapByHostName[hostName];

        var ret = [];
        if (statuses) {
            for (var i=0; i<statuses.length; i++) {
                var status = statuses[i];
                if (vm.shouldShowStatus(status)) {
                    ret.push(status);
                }
            }
        }

        return ret;
    }

    getCpuUsage(hostName: string): string {
        var vm = this;
        return vm.queueReaderCpuMapByHostName[hostName];
    }

    getMemoryUsage(hostName: string): string {
        var vm = this;
        return vm.queueReaderPhysicalMemoryMapByHostName[hostName];
    }

    getMemoryUsageGraph(hostName: string): string {
        var vm = this;
        return vm.queueReaderMemoryGraphMapByHostName[hostName];
    }

    shouldShowStatus(status: QueueReaderStatus): boolean {
        var vm = this;

        //if filter option is on, return true
        if (vm.queueReaderStatusService.showMissingQueueReadersOnEmptyQueues) {
            return true;
        }

        //if queue isn't empty, return true
        var queueSize = vm.getQueueSizeForQueueName(status.queueName);
        if (queueSize //may be null if still refreshing queues
            && (queueSize > 0
                || (queueSize == 0 && status.isBusy))) { //queue size doesn't include the one being processed now, so allow if busy
            return true;
        }

        //if status is NOT in warning state return true
        if (!vm.isStatusDead(status)) {
            return true;
        }

        return false;
    }

    needsRestart(status: QueueReaderStatus): boolean {
        if (!status.dtJar
            || !status.dtStarted) {
            return false;
        }

        //it needs a restart if the jar date is more recent than the start date
        return status.dtJar > status.dtStarted;
    }

    getNeedsRestartDesc(status: QueueReaderStatus): string {

        var vm = this;

        if (!status.dtJar
            || !status.dtStarted) {
            return '';
        }

        var dtJar = new Date(status.dtJar);
        var dtStarted = new Date(status.dtStarted);
        return 'Jar built: ' + ServiceListComponent.formatDate(dtJar) + ', app started: ' + ServiceListComponent.formatDate(dtStarted);
    }


    playButtonClicked(status: QueueReaderStatus) {
        var vm = this;
        vm.viewExchangesForStatus(status);
    }

    warningButtonClicked(status: QueueReaderStatus) {
        var vm = this;
        vm.viewExchangesForStatus(status);
    }

    private viewExchangesForStatus(status: QueueReaderStatus) {
        var vm = this;
        if (!status.isBusyOdsCode) {
            vm.logger.warning('No ODS code found');
            return;
        }

        //console.log('viewing ODS code ' + status.isBusyOdsCode);

        //retrieve service for ODS code
        vm.serviceService.getForOdsCode(status.isBusyOdsCode)
            .subscribe(
                data => {
                    ServiceListComponent.viewExchanges(data, vm.$state, vm.$modal);
                },
                (error) => vm.logger.error('Failed get service', error)
            );

    }

    getInstanceNumber(status: QueueReaderStatus): string {
        if (status.applicationInstanceNumber > 1) {
            return ' #' + status.applicationInstanceNumber;
        } else {
            return '';
        }
    }

    getQueueRoutingDesc(exchangeName: string, routingKey: string): string {
        var vm = this;
        var combined = vm.combineIntoQueueName(exchangeName, routingKey);
        return vm.routingQueueDescs[combined];
    }

}
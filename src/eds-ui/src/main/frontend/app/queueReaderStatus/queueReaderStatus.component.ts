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

@Component({
    template : require('./queueReaderStatus.html')
})
export class QueueReaderStatusComponent {

    //properties derived from Rabbit config
    routingExchangeNames: string[]; //distinct exchange names (EdsInbound, EdsProtocol etc.)
    routingKeys: string[]; //distinct set of all routing keys (A, B, C etc.)
    routingQueueNames: string[]; //set of all queue names (EdsInbound-A, EdsInbound-B etc.)
    rabbitNodes: RabbitNode[];

    //queue reader status
    statusLastRefreshed: Date;
    queueReaderStatusList: QueueReaderStatus[];
    queueReaderStatusMap: {};
    refreshingQueueReaderStatus: boolean;

    //rabbit status
    rabbitQueueStatus: {};
    refreshingRabbitQueueStatus: boolean;

    presetColours: string[];
    hostNameColourMap: {};

    constructor(private queueReaderStatusService:QueueReaderStatusService,
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
        //console.log('getting rabbit config');
        vm.rabbitService.getRoutings()
            .subscribe(
                (result) => {
                    //console.log('got rabbit config');
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

        //get a distinct list of exchange names and routing keys from the array
        var len = routings.length;
        for (var i=0; i<len; i++) {
            var r = routings[i];

            var exchangeName = r.exchangeName;
            if ($.inArray(exchangeName, vm.routingExchangeNames) == -1) {
                vm.routingExchangeNames.push(exchangeName);
                //console.log('got exchange name ' + exchangeName);
            }

            var routingKey = r.routeKey;
            if ($.inArray(routingKey, vm.routingKeys) == -1) {
                vm.routingKeys.push(routingKey);
                //console.log('got routing key ' + routingKey);
            }

            var combined = vm.combineIntoQueueName(exchangeName, routingKey);
            vm.routingQueueNames.push(combined);
        }

        //sort the routing key array so they appear as expected (but don't sort the exchange names)
        vm.routingKeys = linq(vm.routingKeys).OrderBy(s => s.toLowerCase()).ToArray();

        //console.log('got routingExchangeNames ' + vm.routingExchangeNames.length);
    }

    refreshQueueReaders() {
        var vm = this;
        vm.refreshingQueueReaderStatus = true;
        vm.statusLastRefreshed = new Date();

        vm.queueReaderStatusService.getStatus().subscribe(
            (result) => {

                vm.queueReaderStatusList = result;

                //also hash the QueueReaderStatus objects by their queue name, so
                vm.queueReaderStatusMap = {};

                var len = vm.queueReaderStatusList.length;
                for (var i=0; i<len; i++) {
                    var status = vm.queueReaderStatusList[i];
                    var queueName = status.queueName;

                    var statusesForQueue = vm.queueReaderStatusMap[queueName];
                    console.log(statusesForQueue);
                    if (!statusesForQueue) {
                        statusesForQueue = [];
                        vm.queueReaderStatusMap[queueName] = statusesForQueue;
                    }
                    statusesForQueue.push(status);
                }

                vm.calculateColours();

                vm.refreshingQueueReaderStatus = false;

            },
            (error) => {
                vm.logger.error('Failed get Queue Reader status', error, 'Queue Readers');
                vm.refreshingQueueReaderStatus = false;
            }
        );
    }

    calculateColours():void {
        var vm = this;

        //get list of disinct host names
        var distinctHostNames = [];

        var len = vm.queueReaderStatusList.length;
        for (var i=0; i<len; i++) {
            var s = vm.queueReaderStatusList[i];
            var hostName = s.hostName;
            if ($.inArray(hostName, distinctHostNames) == -1) {
                distinctHostNames.push(hostName);
            }
        }

        //sort list
        distinctHostNames = linq(distinctHostNames).OrderBy(s => s.toLowerCase()).ToArray();

        //assign a colour to each server name
        vm.hostNameColourMap = {};

        len = distinctHostNames.length;
        for (var i=0; i<len; i++) {
            hostName = distinctHostNames[i];

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
            var percent = Math.round(100 * (status.currentHeapMb / status.maxHeapMb));
            return ' [' + percent + '% of ' + status.maxHeapDesc + ']';
        } else {
            return '';
        }
    }

    isStatusTooOld(status: QueueReaderStatus): boolean {
        var vm = this;

        var statusTime = status.timestmp;
        var warningTime = vm.statusLastRefreshed.getTime() - (1000 * 60 * 2);

        return statusTime < warningTime;
    }

    getStatusAgeDesc(status: QueueReaderStatus): string {
        var vm = this;
        return ServiceListComponent.getDateDiffDesc(new Date(status.timestmp), vm.statusLastRefreshed);
    }


    getCellColour(status: QueueReaderStatus): any {
        var vm = this;
        var hostName = status.hostName;
        var colour = vm.hostNameColourMap[hostName];
        return {'background-color': colour};
        /*if (exchangeName == 'EdsInbound') {
            console.log('inbound');
            return {'background-color': '#0000FF'};
        } else {
            console.log('not inbound');
            return {'background-color': '#00FFFF'};
        }*/
    }

    getStatuses(exchangeName: string, routingKey: string): QueueReaderStatus[] {
        var vm = this;
        var queueName = vm.combineIntoQueueName(exchangeName, routingKey);
        return vm.queueReaderStatusMap[queueName];
    }

    getQueueSize(exchangeName: string, routingKey: string): number {
        var vm = this;
        if (!vm.rabbitQueueStatus) {
            return null;
        }
        var queueName = vm.combineIntoQueueName(exchangeName, routingKey);
        var queueStatus = vm.rabbitQueueStatus[queueName];
        if (queueStatus) {
            return queueStatus.messages_ready;
        } else {
            return null;
        }
    }
}
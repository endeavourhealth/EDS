import {Component} from "@angular/core";
import {StateService} from "ui-router-ng2";
import {FolderItem} from "../models/FolderContent";
import {RabbitNode} from "../models/RabbitNode";
import {RabbitExchange} from "../models/RabbitExchange";
import {RabbitQueue} from "../models/RabbitQueue";
import {RabbitService} from "../queueing/rabbit.service";
import {DashboardService} from "./dashboard.service";
import {ItemType} from "../models/ItemType";

@Component({
	template: require('./dashboard.html'),
})
export class DashboardComponent {
	recentDocumentsData:FolderItem[];
	rabbitNodes:RabbitNode[];
	quickestNode : RabbitNode;
	inboundExchange : RabbitExchange;
	protocolExchange : RabbitExchange;
	transformExchange : RabbitExchange;
	responseExchange : RabbitExchange;
	subscriberExchange : RabbitExchange;
	inboundQueues : RabbitQueue[] = [];
	protocolQueues : RabbitQueue[] = [];
	transformQueues : RabbitQueue[] = [];
	responseQueues : RabbitQueue[] = [];
	subscriberQueues : RabbitQueue[] = [];

	pingWarn : number = 250;

	constructor(private dashboardService:DashboardService,
							private rabbitService : RabbitService,
							private $state : StateService) {
		this.refresh();
	}

	refresh() {
		this.getRecentDocumentsData();
		this.getRabbitNodes();
	}

	getRecentDocumentsData() {
		var vm = this;
		vm.recentDocumentsData = null;
		vm.dashboardService.getRecentDocumentsData()
			.subscribe(data => vm.recentDocumentsData = data);
	}

	getRabbitNodes() {
		var vm = this;
		vm.rabbitNodes = null;
		vm.rabbitService.getRabbitNodes()
			.subscribe(
				data => {
				vm.rabbitNodes = data;
				vm.getRabbitNodePings();
			});
	}

	getRabbitNodePings() {
		var vm = this;
		vm.quickestNode = null;
		for(var idx in vm.rabbitNodes) {
			vm.rabbitService.pingRabbitNode(vm.rabbitNodes[idx].address)
				.subscribe(
					result => {

					if (vm.quickestNode === null) {
						vm.quickestNode = result;
						vm.getRabbitExchanges();
						vm.getRabbitQueues();
					}

					var rabbitNode : RabbitNode[] = $.grep(vm.rabbitNodes, function(i) { return i.address === result.address;});
					if (rabbitNode.length === 1) {
						rabbitNode[0].ping = result.ping;
					}
				})
		}
	}

	getPingLabelClass(item : RabbitNode) {
		if (item.ping === -1)
			return 'label-danger';
		if (item.ping === 0)
			return 'label-default';
		if (item.ping <= this.pingWarn)
			return 'label-success';
		return 'label-warning';
	}

	getRabbitExchanges() {
		var vm = this;
		vm.rabbitService.getRabbitExchanges(this.quickestNode.address)
			.subscribe(
				data => {
				// Split queues by type
				vm.inboundExchange = $.grep(data, function(e) { return e.name.lastIndexOf('EdsInbound',0)===0;})[0];
				vm.protocolExchange = $.grep(data, function(e) { return e.name.lastIndexOf('EdsProtocol',0)===0;})[0];
				vm.transformExchange = $.grep(data, function(e) { return e.name.lastIndexOf('EdsTransform',0)===0;})[0];
				vm.responseExchange = $.grep(data, function(e) { return e.name.lastIndexOf('EdsResponse',0)===0;})[0];
				vm.subscriberExchange = $.grep(data, function(e) { return e.name.lastIndexOf('EdsSubscriber',0)===0;})[0];
			});
	}

	getRabbitQueues() {
		var vm = this;

		vm.rabbitService.getRabbitQueues(vm.quickestNode.address)
			.subscribe(
				data => {
				// Split queues by type
				vm.inboundQueues = $.grep(data, function(e) { return e.name.lastIndexOf('EdsInbound',0)===0;})
				vm.protocolQueues = $.grep(data, function(e) { return e.name.lastIndexOf('EdsProtocol',0)===0;})
				vm.transformQueues = $.grep(data, function(e) { return e.name.lastIndexOf('EdsTransform',0)===0;})
				vm.responseQueues = $.grep(data, function(e) { return e.name.lastIndexOf('EdsResponse',0)===0;})
				vm.subscriberQueues = $.grep(data, function(e) { return e.name.lastIndexOf('EdsSubscriber',0)===0;})
			});
	}

	actionItem(item : FolderItem, action : string) {
		switch (item.type) {
			case ItemType.Query:
				this.$state.go('app.queryAction', {itemUuid: item.uuid, itemAction: action});
				break;
			case ItemType.DataSet:
				this.$state.go('app.dataSetAction', {itemUuid: item.uuid, itemAction: action});
				break;
			case ItemType.CodeSet:
				this.$state.go('app.codeSetAction', {itemUuid: item.uuid, itemAction: action});
				break;
			case ItemType.Report:
				this.$state.go('app.reportAction', {itemUuid: item.uuid, itemAction: action});
				break;
			case ItemType.Protocol:
				this.$state.go('app.protocolAction', {itemUuid: item.uuid, itemAction: action});
				break;
			case ItemType.System:
				this.$state.go('systemEdit', {itemUuid: item.uuid, itemAction: action});
				break;
		}
	}
}

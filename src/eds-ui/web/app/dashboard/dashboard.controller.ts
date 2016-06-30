/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.dashboard {
	import FolderItem = app.models.FolderItem;
	import ItemType = app.models.ItemType;
	import IDashboardService = app.core.IDashboardService;
	import ILoggerService = app.blocks.ILoggerService;
	import RabbitNode = app.models.RabbitNode;
	import RabbitQueue = app.models.RabbitQueue;
	import RabbitExchange = app.models.RabbitExchange;
	import IRabbitService = app.core.IRabbitService;

	'use strict';

	class DashboardController {
		recentDocumentsData:FolderItem[];
		rabbitNodes:RabbitNode[];
		quickestNode : RabbitNode;
		pingCount : number;
		inboundExchange : RabbitExchange;
		protocolExchange : RabbitExchange;
		transformExchange : RabbitExchange;
		responseExchange : RabbitExchange;
		subscriberExchange : RabbitExchange;
		inboundQueues : RabbitQueue[];
		protocolQueues : RabbitQueue[];
		transformQueues : RabbitQueue[];
		responseQueues : RabbitQueue[];
		subscriberQueues : RabbitQueue[];

		pingWarn : number = 250;
		queueWarn : number = 400;
		queueDanger : number = 500;

		static $inject = ['DashboardService', 'LoggerService', 'RabbitService', '$state'];

		constructor(private dashboardService:IDashboardService,
								private logger:ILoggerService,
								private rabbitService : IRabbitService,
								private $state : IStateService) {
			this.refresh();
		}

		refresh() {
			this.getRecentDocumentsData();
			this.getRabbitNodes();
		}

		getRecentDocumentsData() {
			var vm:DashboardController = this;
			vm.recentDocumentsData = null;
			vm.dashboardService.getRecentDocumentsData()
				.then(function (data:FolderItem[]) {
					vm.recentDocumentsData = data;
				});
		}

		getRabbitNodes() {
			var vm:DashboardController = this;
			vm.rabbitNodes = null;
			vm.quickestNode = null;
			vm.rabbitService.getRabbitNodes()
				.then(function (data : RabbitNode[]) {
					vm.rabbitNodes = data;
					vm.getRabbitNodePings();
				});
		}

		getRabbitNodePings() {
			var vm = this;
			vm.pingCount = 0;
			for(var idx in vm.rabbitNodes) {
				vm.rabbitService.pingRabbitNode(vm.rabbitNodes[idx].address)
					.then(function (result : RabbitNode) {
						var rabbitNode : RabbitNode[] = $.grep(vm.rabbitNodes, function(i) { return i.address === result.address;});
						if (rabbitNode.length === 1) {
							rabbitNode[0].ping = result.ping;
							if (vm.quickestNode === null || (result.ping > -1 && result.ping < vm.quickestNode.ping))
								vm.quickestNode = result;
						}
						vm.pingCount ++;
						if (vm.pingCount === vm.rabbitNodes.length) {
							vm.getRabbitExchanges();
							vm.getRabbitQueues();
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

			vm.rabbitService.getRabbitExchanges(vm.quickestNode.address)
				.then(function(data : RabbitExchange[]){
					// Split queues by type
					vm.inboundExchange = $.grep(data, function(e) { return e.name.lastIndexOf('EdsInbound',0)===0;})[0];
					vm.protocolExchange = $.grep(data, function(e) { return e.name.lastIndexOf('EdsProtocol',0)===0;})[0];
					vm.transformExchange = $.grep(data, function(e) { return e.name.lastIndexOf('EdsTransform',0)===0;})[0];
					vm.responseExchange = $.grep(data, function(e) { return e.name.lastIndexOf('EdsResponse',0)===0;})[0];
					vm.subscriberExchange = $.grep(data, function(e) { return e.name.lastIndexOf('EdsSubscriber',0)===0;})[0];
				});
		}

		getExchangeRateClass(item : RabbitExchange) {
			if (!item || !item.message_stats)
				return 'label-default';

			// All OK if we're delivering faster than receiving
			if (item.message_stats.publish_out_details.rate <= item.message_stats.publish_in_details.rate)
				return 'label-success';

			return 'label-warning';
		}

		getQueueLengthClass(queue : RabbitQueue) {
			if (queue.messages_ready > this.queueDanger)
				return 'label-danger';
			if (queue.messages_ready > this.queueWarn)
				return 'label-warning';
			return 'label-success';
		}

		getPublishRateAsWidthStylePercent(exchange : RabbitExchange){
			if (!exchange || !exchange.message_stats)
				return {width : '50%'};

			var total = exchange.message_stats.publish_in_details.rate + exchange.message_stats.publish_out_details.rate;
			if (total === 0)
				return {width : '50%'};

			var pcnt = (exchange.message_stats.publish_in_details.rate * 100) / total;
			return {width : pcnt + '%'};
		}

		getRabbitQueues() {
			var vm = this;

			vm.rabbitService.getRabbitQueues(vm.quickestNode.address)
				.then(function(data : RabbitQueue[]){
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
				case ItemType.ListOutput:
					this.$state.go('app.listOutputAction', {itemUuid: item.uuid, itemAction: action});
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
					this.$state.go('app.systemAction', {itemUuid: item.uuid, itemAction: action});
					break;
			}
		}
	}

	angular
		.module('app.dashboard')
		.controller('DashboardController', DashboardController);
}

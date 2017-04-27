import {Component} from "@angular/core";
import {RabbitNode} from "./models/RabbitNode";
import {Routing} from "./Routing";
import {RabbitBinding} from "./models/RabbitBinding";
import {RabbitService} from "./rabbit.service";
import {LoggerService, MessageBoxDialog} from "eds-common-js";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {QueueEditDialog} from "./queueingEditor.dialog";

@Component({
	template : require('./queueingList.html')
})
export class QueueingListComponent {
	rabbitNodes:RabbitNode[];
	quickestNode : RabbitNode;
	routings : Routing[];
	inboundBindings : RabbitBinding[];
	protocolBindings : RabbitBinding[];
	transformBindings : RabbitBinding[];
	responseBindings : RabbitBinding[];
	subscriberBindings : RabbitBinding[];

	constructor(private $modal : NgbModal,
							private rabbitService : RabbitService,
							private log : LoggerService) {
		this.getRabbitNodes();
	}

	getRabbitNodes() {
		var vm = this;
		vm.rabbitNodes = null;
		vm.rabbitService.getRabbitNodes()
			.subscribe(
				(data) => {
					vm.rabbitNodes = data;
					vm.getRabbitNodePings();
				},
				(error) => vm.log.error('Error getting rabbit node list', error, 'Error')
			);
	}

	getRabbitNodePings() {
		var vm = this;
		vm.quickestNode = null;
		for (var idx in vm.rabbitNodes) {
			vm.rabbitService.pingRabbitNode(vm.rabbitNodes[idx].address)
				.subscribe(
					(result) => {

						if (vm.quickestNode === null) {
							vm.quickestNode = result;
							vm.getRouteGroups();
							vm.getRabbitBindings();
						}

						var rabbitNode: RabbitNode[] = $.grep(vm.rabbitNodes, function (i) {
							return i.address === result.address;
						});
						if (rabbitNode.length === 1) {
							rabbitNode[0].ping = result.ping;
						}
					},
					(error) => vm.log.error('Error pinging node', vm.rabbitNodes[idx].address, 'Error')
				)
		}
	}

	getRouteGroups() {
		var vm = this;
		vm.rabbitService.getRoutings()
			.subscribe(
				(result) => vm.routings = result,
				(error) => vm.log.error('Failed to load route groups', error, 'Load route groups')
			);
	}

	getRabbitBindings() {
		var vm = this;
		vm.rabbitService.getRabbitBindings(vm.quickestNode.address)
			.subscribe(
				(result) => vm.separateBindings(result),
				(error) => vm.log.error('Failed to load rabbit bindings', error, 'Load rabbit bindings')
			);
	}

	getRouteGroupClass(routeGroup : Routing, bindings : RabbitBinding[]) {
		if (!bindings)
			return 'fa fa-blank text-default';
		if ($.grep(bindings, function(e:RabbitBinding) { return e.routing_key === routeGroup.routeKey; }).length === 0)
			return 'fa fa-plus-circle text-danger';
		return 'fa fa-check-circle text-success';
	}

	bindingExists(item: RabbitBinding) {
		let matches : Routing[] = $.grep(this.routings, function(e:Routing) { return e.routeKey === item.routing_key; });
		if (!matches || matches.length === 0)
			return false;
		return true;
	}

	edit(item : Routing) {
		var vm = this;
		QueueEditDialog.open(vm.$modal, item)
			.result.then(function(editedItem : Routing) {
			jQuery.extend(true, item, editedItem);
			vm.rabbitService.saveRoutings(vm.routings)
				.subscribe(
					() => vm.log.success('Route group saved', editedItem, 'Save routeGroup'),
					(error) => vm.log.error('Failed to save route group', error, 'Save route group')
				);
		})
			.catch((reason) => vm.log.info("Edit cancelled"));
	}

	delete(item : Routing) {
		var vm = this;
		MessageBoxDialog.open(vm.$modal, 'Delete Route group', 'Are you sure you want to delete the route group?', 'Yes', 'No')
			.result.then(function () {
			// remove item from list
			vm.rabbitService.saveRoutings(vm.routings)
				.subscribe(
					() => vm.log.success('Route group deleted', item, 'Delete route group'),
					(error) => vm.log.error('Failed to delete route group', error, 'Delete route group')
				);
		})
			.catch((reason) => vm.log.info("Delete cancelled"));
	}

	sync() {
		var vm = this;
		MessageBoxDialog.open(vm.$modal, 'Synchronise RabbitMQ', 'Are you sure you want to synchronise RabbitMQ with the defined route groups?', 'Yes', 'No')
			.result.then(function() {
				vm.rabbitService.synchronize(vm.quickestNode.address)
				.subscribe(
					(result) => {
					vm.log.success('RabbitMQ synchronized');
					vm.separateBindings(result);
				},
				(error) => {
					vm.log.error('Failed to synchronize', error, 'Synchronize RabbitMQ');
					vm.getRabbitBindings();
				});
		});
	}

	separateBindings(bindings : RabbitBinding[]) {
		var vm = this;
		vm.inboundBindings = $.grep(bindings, function(e:RabbitBinding) {return e.source === 'EdsInbound'; });
		vm.protocolBindings = $.grep(bindings, function(e:RabbitBinding) {return e.source === 'EdsProtocol'; });
		vm.transformBindings = $.grep(bindings, function(e:RabbitBinding) {return e.source === 'EdsTransform'; });
		vm.responseBindings = $.grep(bindings, function(e:RabbitBinding) {return e.source === 'EdsResponse'; });
		vm.subscriberBindings = $.grep(bindings, function(e:RabbitBinding) {return e.source === 'EdsSubscriber'; });
	}
}

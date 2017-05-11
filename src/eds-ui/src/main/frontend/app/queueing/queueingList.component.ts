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
	rabbitNodes: RabbitNode[];
	quickestNode : RabbitNode;
	routingExchangeNames: string[];
	routingMap: {}; //bindings from our config
	bindingMap: {}; //actual bindings from Rabbit

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
				(result) => vm.populateRoutingMap(result),
				(error) => vm.log.error('Failed to load route groups', error, 'Load route groups')
			);
	}

	private populateRoutingMap(routings: Routing[]) {
		this.routingExchangeNames = [];
		this.routingMap = {};

		//hash the routings by their exhcnage name
		var len = routings.length;
		for (var i=0; i<len; i++) {
			var r = routings[i];
			this.addToRoutingMap(r);
		}
	}

	private addToRoutingMap(routing: Routing) {
		var exchangeName = routing.exchangeName;

		var list;
		if ($.inArray(exchangeName, this.routingExchangeNames) > -1) {
			list = this.routingMap[exchangeName];

		} else {
			list = [];
			this.routingMap[exchangeName] = list;
			this.routingExchangeNames.push(exchangeName);
		}

		list.push(routing);
	}


	getRoutingsForExchange(exchangeName: string): Routing[] {
		//if this is called before the initial calls return, we'll have an undefined map
		if (!this.routingMap) {
			return [];
		}
		return this.routingMap[exchangeName];
	}

	getBindingsForExchange(exchangeName: string): RabbitBinding[] {
		//if this is called before the initial calls return, we'll have an undefined map
		if (!this.bindingMap) {
			return [];
		}
		return this.bindingMap[exchangeName];
	}

	private getRoutingAsList(): Routing[] {

		var ret = [];

		var len = this.routingExchangeNames.length;
		for (var i=0; i<len; i++) {
			var exchangeName = this.routingExchangeNames[i];
			var routings = this.routingMap[exchangeName];

			for (var j=0; j<routings.length; j++) {
				var routing = routings[j];
				ret.push(routing);
			}
		}

		return ret;
	}

	getRabbitBindings() {
		var vm = this;
		vm.rabbitService.getRabbitBindings(vm.quickestNode.address)
			.subscribe(
				(result) => vm.populateBindingMap(result),
				(error) => vm.log.error('Failed to load rabbit bindings', error, 'Load rabbit bindings')
			);
	}

	private populateBindingMap(bindings: RabbitBinding[]) {
		this.bindingMap = {};

		//hash the routings by their exhcnage name
		var len = bindings.length;
		for (var i=0; i<len; i++) {
			var r = bindings[i];
			this.addToBindingMap(r);
		}
	}

	private addToBindingMap(binding: RabbitBinding) {
		var exchangeName = binding.source;

		var list = this.bindingMap[exchangeName];
		if (!list) {
			list = [];
			this.bindingMap[exchangeName] = list;
		}
		list.push(binding);
	}


	getRouteGroupStatusIconClass(routeGroup : Routing) {
		var exchangeName = routeGroup.exchangeName;
		var bindings = this.getBindingsForExchange(exchangeName);
		if (!bindings) {
			return 'fa fa-plus-circle text-danger';
		}

		if ($.grep(bindings, function(e:RabbitBinding) { return e.routing_key === routeGroup.routeKey; }).length === 0)
			return 'fa fa-plus-circle text-danger';
		return 'fa fa-check-circle text-success';
	}

	bindingExistsInConfig(binding: RabbitBinding) {
		var exchangeName = binding.source;
		var routings = this.getRoutingsForExchange(exchangeName);
		if (!routings) {
			return false;
		}

		let matches : Routing[] = $.grep(routings, function(e:Routing) { return e.routeKey === binding.routing_key; });
		if (!matches || matches.length === 0)
			return false;
		return true;
	}



	delete(item : Routing) {

		var vm = this;
		MessageBoxDialog.open(vm.$modal, 'Delete Route group', 'Are you sure you want to delete the route group?', 'Yes', 'No')
			.result.then(function () {

				var list = vm.getRoutingAsList();
				var index = list.indexOf(item);
				list.splice(index, 1);

				vm.rabbitService.saveRoutings(list)
					.subscribe(
						() => {
							vm.getRouteGroups();
							vm.log.success('Route group deleted', item, 'Delete route group');
						},
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
					vm.populateBindingMap(result);
				},
				(error) => {
					vm.log.error('Failed to synchronize', error, 'Synchronize RabbitMQ');
					vm.getRabbitBindings();
				});
		});
	}


	edit(item : Routing) {
		var vm = this;
		QueueEditDialog.open(vm.$modal, item)
			.result.then(function(editedItem : Routing) {
				jQuery.extend(true, item, editedItem);
				vm.saveRoutings();
			})
			.catch((reason) => {});
	}

	newRouting(exchangeName: string) {
		var newRouting = new Routing();
		if (exchangeName) {
			newRouting.exchangeName = exchangeName;
		}

		var vm = this;

		QueueEditDialog.open(this.$modal, newRouting)
			.result.then(function(editedItem : Routing) {
				//the edited item seems to be an array rather than a single routing,
				//so copy back into the routing object we created and add that to the map
				jQuery.extend(true, newRouting, editedItem);
				vm.addToRoutingMap(newRouting);
				vm.saveRoutings();
			})
			.catch((reason) => {});
	}

	private saveRoutings() {
		var vm = this;
		this.rabbitService.saveRoutings(this.getRoutingAsList())
			.subscribe(
				() => vm.log.success('Route group saved', 'Save routeGroup'),
				(error) => vm.log.error('Failed to save route group', error, 'Save route group')
			);
	}

	moveUp(routing: Routing) {
		this.move(routing, true);
	}

	moveDown(routing: Routing) {
		this.move(routing, false);
	}

	move(routing: Routing, up: boolean) {

		var exchangeName = routing.exchangeName;
		var list = this.routingMap[exchangeName];
		var index = list.indexOf(routing);

		if (up) {
			if (index == 0) {
				return;
			}

			list.splice(index, 1);
			list.splice(index-1, 0, routing);
		} else {
			if (index+1 == list.length) {
				return;
			}

			list.splice(index, 1);
			list.splice(index+1, 0, routing);
		}

		this.saveRoutings();
	}

	refresh() {
		this.getRabbitNodes();
	}

}

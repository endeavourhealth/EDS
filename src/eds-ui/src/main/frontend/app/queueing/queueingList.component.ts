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
		var vm = this;
		vm.routingExchangeNames = [];
		vm.routingMap = {};

		//hash the routings by their exhcnage name
		var len = routings.length;
		for (var i=0; i<len; i++) {
			var r = routings[i];
			vm.addToRoutingMap(r);
		}
	}

	private addToRoutingMap(routing: Routing) {
		var vm = this;
		var exchangeName = routing.exchangeName;

		var list;
		if ($.inArray(exchangeName, vm.routingExchangeNames) > -1) {
			list = vm.routingMap[exchangeName];

		} else {
			list = [];
			vm.routingMap[exchangeName] = list;
			vm.routingExchangeNames.push(exchangeName);
		}

		list.push(routing);
	}


	getRoutingsForExchange(exchangeName: string): Routing[] {
		//if this is called before the initial calls return, we'll have an undefined map
		var vm = this;
		if (!vm.routingMap) {
			return [];
		}
		return vm.routingMap[exchangeName];
	}

	getBindingsForExchange(exchangeName: string): RabbitBinding[] {
		//if this is called before the initial calls return, we'll have an undefined map
		var vm = this;
		if (!vm.bindingMap) {
			return [];
		}
		return vm.bindingMap[exchangeName];
	}

	private getRoutingAsList(): Routing[] {
		var vm = this;

		var ret = [];

		var len = vm.routingExchangeNames.length;
		for (var i=0; i<len; i++) {
			var exchangeName = vm.routingExchangeNames[i];
			var routings = vm.routingMap[exchangeName];

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
		var vm = this;
		vm.bindingMap = {};

		//hash the routings by their exhcnage name
		var len = bindings.length;
		for (var i=0; i<len; i++) {
			var r = bindings[i];
			vm.addToBindingMap(r);
		}
	}

	private addToBindingMap(binding: RabbitBinding) {
		var vm = this;
		var exchangeName = binding.source;

		var list = vm.bindingMap[exchangeName];
		if (!list) {
			list = [];
			vm.bindingMap[exchangeName] = list;
		}
		list.push(binding);
	}


	getRouteGroupStatusIconClass(routeGroup : Routing) {
		var vm = this;
		var exchangeName = routeGroup.exchangeName;
		var bindings = vm.getBindingsForExchange(exchangeName);
		if (!bindings) {
			return 'fa fa-plus-circle text-danger';
		}

		if ($.grep(bindings, function(e:RabbitBinding) { return e.routing_key === routeGroup.routeKey; }).length === 0)
			return 'fa fa-plus-circle text-danger';
		return 'fa fa-check-circle text-success';
	}

	bindingExistsInConfig(binding: RabbitBinding) {
		var vm = this;
		var exchangeName = binding.source;
		var routings = vm.getRoutingsForExchange(exchangeName);
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

				var exchangeName = item.exchangeName;
				var list = vm.routingMap[exchangeName];
				var index = list.indexOf(item);
				list.splice(index, 1);

				vm.saveRoutings();

				/*var list = vm.getRoutingAsList();
				var index = list.indexOf(item);
				list.splice(index, 1);

				vm.rabbitService.saveRoutings(list)
					.subscribe(
						() => {
							vm.getRouteGroups();
							vm.log.success('Route group deleted', item, 'Delete route group');
						},
						(error) => vm.log.error('Failed to delete route group', error, 'Delete route group')
					);*/
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
		var vm = this;
		var newRouting = new Routing();
		if (exchangeName) {
			newRouting.exchangeName = exchangeName;
		}

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
		vm.rabbitService.saveRoutings(vm.getRoutingAsList())
			.subscribe(
				() => vm.log.success('Route group saved', 'Save routeGroup'),
				(error) => vm.log.error('Failed to save route group', error, 'Save route group')
			);
	}

	moveUp(routing: Routing) {
		var vm = this;
		vm.move(routing, true);
	}

	moveDown(routing: Routing) {
		var vm = this;
		vm.move(routing, false);
	}

	move(routing: Routing, up: boolean) {
		var vm = this;
		var exchangeName = routing.exchangeName;
		var list = vm.routingMap[exchangeName];
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

		vm.saveRoutings();
	}

	refresh() {
		var vm = this;
		vm.getRabbitNodes();
	}

}

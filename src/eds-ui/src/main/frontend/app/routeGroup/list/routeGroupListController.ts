import IModalService = angular.ui.bootstrap.IModalService;

import {RabbitNode} from "../../models/RabbitNode";
import {RouteGroup} from "../RouteGroup";
import {RabbitBinding} from "../../models/RabbitBinding";
import {IRabbitService} from "../../core/rabbit.service";
import {ILoggerService} from "../../blocks/logger.service";
import {IRouteGroupService} from "../service/routeGroup.service";
import {RouteGroupEditorController} from "../editor/routeGroupEditor.controller";
import {MessageBoxController} from "../../dialogs/messageBox/messageBox.controller";

export class RouteGroupListController {
	rabbitNodes:RabbitNode[];
	quickestNode : RabbitNode;
	routeGroups : RouteGroup[];
	inboundBindings : RabbitBinding[];
	protocolBindings : RabbitBinding[];
	transformBindings : RabbitBinding[];
	responseBindings : RabbitBinding[];
	subscriberBindings : RabbitBinding[];

	static $inject = ['$uibModal', 'RouteGroupService', 'RabbitService', 'LoggerService'];

	constructor(private $modal : IModalService,
							private routeGroupService : IRouteGroupService,
							private rabbitService : IRabbitService,
							private log : ILoggerService) {
		this.getRabbitNodes();
	}

	getRabbitNodes() {
		var vm = this;
		vm.rabbitNodes = null;
		vm.rabbitService.getRabbitNodes()
			.then(function (data : RabbitNode[]) {
				vm.rabbitNodes = data;
				vm.getRabbitNodePings();
			});
	}

	getRabbitNodePings() {
		var vm = this;
		vm.quickestNode = null;
		for(var idx in vm.rabbitNodes) {
			vm.rabbitService.pingRabbitNode(vm.rabbitNodes[idx].address)
				.then(function (result : RabbitNode) {

					if (vm.quickestNode === null) {
						vm.quickestNode = result;
						vm.getRouteGroups();
						vm.getRabbitBindings();
					}

					var rabbitNode : RabbitNode[] = $.grep(vm.rabbitNodes, function(i) { return i.address === result.address;});
					if (rabbitNode.length === 1) {
						rabbitNode[0].ping = result.ping;
					}
				})
		}
	}

	getRouteGroups() {
		var vm = this;
		vm.routeGroupService.getRouteGroups()
			.then(function(result) {
				vm.routeGroups = result;
			})
			.catch(function (error) {
				vm.log.error('Failed to load route groups', error, 'Load route groups');
			});
	}

	getRabbitBindings() {
		var vm = this;
		// TODO : Determine fastest node and use for address
		vm.rabbitService.getRabbitBindings(vm.quickestNode.address)
			.then(function(result : RabbitBinding[]) {
				vm.separateBindings(result);
			})
			.catch(function (error) {
				vm.log.error('Failed to load rabbit bindings', error, 'Load rabbit bindings');
			});
	}

	getRouteGroupClass(routeGroup : RouteGroup, bindings : RabbitBinding[]) {
		if (!bindings)
			return 'fa fa-blank text-default';
		if ($.grep(bindings, function(e:RabbitBinding) { return e.routing_key === routeGroup.routeKey; }).length === 0)
			return 'fa fa-plus-circle text-danger';
		return 'fa fa-check-circle text-success';
	}

	bindingExists(item: RabbitBinding) {
		if ($.grep(this.routeGroups, function(e:RouteGroup) { return e.routeKey === item.routing_key; }).length === 0)
			return false;
		return true;
	}

	edit(item : RouteGroup) {
		var vm = this;
		RouteGroupEditorController.open(vm.$modal, item)
			.result.then(function(routeGroup : RouteGroup) {
			jQuery.extend(true, item, routeGroup);
			vm.routeGroupService.saveRouteGroups(vm.routeGroups)
				.then(function() {
					vm.log.success('Route group saved', routeGroup, 'Save routeGroup');
				})
				.catch(function (error : any) {
					vm.log.error('Failed to save route group', error, 'Save route group');
				});
		});
	}

	delete(item : RouteGroup) {
		var vm = this;
		MessageBoxController.open(vm.$modal,
															'Delete Route group', 'Are you sure you want to delete the route group?', 'Yes', 'No')
			.result.then(function() {
				// remove item from list
				vm.routeGroupService.saveRouteGroups(vm.routeGroups)
					.then(function() {
						vm.log.success('Route group deleted', item, 'Delete route group');
					})
					.catch(function(error : any) {
						vm.log.error('Failed to delete route group', error, 'Delete route group');
					});
		});
	}

	sync() {
		var vm = this;
		MessageBoxController.open(vm.$modal,
			'Synchronise RabbitMQ', 'Are you sure you want to synchronise RabbitMQ with the defined route groups?', 'Yes', 'No')
			.result.then(function() {
				//  TODO : Determine fastest node and use for address
				vm.rabbitService.synchronize(vm.quickestNode.address)
				.then(function(result : RabbitBinding[]) {
					vm.log.success('RabbitMQ synchronized');
					vm.separateBindings(result);
				})
				.catch(function(error : any) {
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

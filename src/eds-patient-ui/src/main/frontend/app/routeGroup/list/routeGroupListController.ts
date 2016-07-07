/// <reference path="../../../typings/index.d.ts" />
/// <reference path="../../core/library.service.ts" />

module app.routeGroup {
	import IRouteGroupService = app.core.IRouteGroupService;
	import MessageBoxController = app.dialogs.MessageBoxController;
	import IRabbitService = app.core.IRabbitService;
	import RabbitBinding = app.models.RabbitBinding;
	'use strict';

	export class RouteGroupListController {
		routeGroups : RouteGroup[];
		inboundBindings : RabbitBinding[];
		interimBindings : RabbitBinding[];
		responseBindings : RabbitBinding[];
		subscriberBindings : RabbitBinding[];

		static $inject = ['$uibModal', 'RouteGroupService', 'RabbitService', 'LoggerService'];

		constructor(private $modal : IModalService,
								private routeGroupService : IRouteGroupService,
								private rabbitService : IRabbitService,
								private log : ILoggerService) {
			this.getRouteGroups();
			this.getRabbitBindings();
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
			vm.rabbitService.getRabbitBindings('DUMMYADDRESS')
				.then(function(result : RabbitBinding[]) {
					vm.inboundBindings = $.grep(result, function(e:RabbitBinding) {return e.source === 'EdsInbound'; });
					vm.interimBindings = $.grep(result, function(e:RabbitBinding) {return e.source === 'EdsInterim'; });
					vm.responseBindings = $.grep(result, function(e:RabbitBinding) {return e.source === 'EdsResponse'; });
					vm.subscriberBindings = $.grep(result, function(e:RabbitBinding) {return e.source === 'EdsSubscriber'; });
				})
				.catch(function (error) {
					vm.log.error('Failed to load rabbit bindings', error, 'Load rabbit bindings');
				});
		}

		getRouteGroupClass(routeGroup : RouteGroup, bindings : RabbitBinding[]) {
			if ($.grep(bindings, function(e:RabbitBinding) { return e.routing_key === routeGroup.routeKey; }).length === 0)
				return 'text-danger';
			return 'text-success';
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
					vm.rabbitService.synchronize("DUMMYADDRESS")
					.then(function() {
						vm.log.success('RabbitMQ synchronized');
					})
					.catch(function(error : any) {
						vm.log.error('Failed to synchronize', error, 'Synchronize RabbitMQ');
					});
			});		}
	}

	angular
		.module('app.routeGroup')
		.controller('RouteGroupListController', RouteGroupListController);
}

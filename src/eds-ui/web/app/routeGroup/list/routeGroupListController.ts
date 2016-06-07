/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../core/library.service.ts" />

module app.routeGroup {
	import IRouteGroupService = app.core.IRouteGroupService;
	import MessageBoxController = app.dialogs.MessageBoxController;
	'use strict';

	export class RouteGroupListController {
		routeGroups : RouteGroup[];

		static $inject = ['$uibModal', 'RouteGroupService', 'LoggerService'];

		constructor(private $modal : IModalService,
								private routeGroupService : IRouteGroupService,
								private log : ILoggerService) {
			this.getRouteGroups();
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
	}

	angular
		.module('app.routeGroup')
		.controller('RouteGroupListController', RouteGroupListController);
}

/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../core/library.service.ts" />

module app.service {
	import IServiceService = app.service.IServiceService;
	import MessageBoxController = app.dialogs.MessageBoxController;
	'use strict';

	export class ServiceListController {
		services : Service[];

		static $inject = ['$uibModal', 'ServiceService', 'LoggerService'];

		constructor(private $modal : IModalService,
								private serviceService : IServiceService,
								private log : ILoggerService) {
			this.getAll();
		}

		getAll() {
			var vm = this;
			vm.serviceService.getAll()
				.then(function(result) {
					vm.services = result;
				})
				.catch(function (error) {
					vm.log.error('Failed to load services', error, 'Load services');
				});
		}

		edit(item : Service) {
			var vm = this;
			ServiceEditorController.open(vm.$modal, item)
				.result.then(function(result : Service) {
				jQuery.extend(true, item, result);
				vm.serviceService.save(item)
					.then(function() {
						vm.log.success('Service saved', item, 'Save service');
					})
					.catch(function (error : any) {
						vm.log.error('Failed to save service', error, 'Save service');
					});
			});
		}

		delete(item : Service) {
			var vm = this;
			MessageBoxController.open(vm.$modal,
																'Delete Service', 'Are you sure you want to delete the Service?', 'Yes', 'No')
				.result.then(function() {
					// remove item from list
					vm.serviceService.delete(item.uuid)
						.then(function() {
							vm.log.success('Service deleted', item, 'Delete Service');
						})
						.catch(function(error : any) {
							vm.log.error('Failed to delete Service', error, 'Delete Service');
						});
			});
		}
	}

	angular
		.module('app.service')
		.controller('ServiceListController', ServiceListController);
}

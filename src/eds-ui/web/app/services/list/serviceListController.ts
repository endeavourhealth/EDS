/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../core/library.service.ts" />

module app.service {
	import IServiceService = app.service.IServiceService;
	import MessageBoxController = app.dialogs.MessageBoxController;
	import Service = app.models.Service;
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

		add() {
			var newService : Service = new Service();
			this.edit(newService);
		}

		edit(item : Service) {
			var vm = this;
			ServiceEditorController.open(vm.$modal, item)
				.result.then(function(result : Service) {
					vm.serviceService.save(result)
					.then(function(savedService : Service) {
						if (item.uuid)
							jQuery.extend(true, item, savedService);
						else
							vm.services.push(savedService);

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
							var index = vm.services.indexOf(item);
							vm.services.splice(index, 1);
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

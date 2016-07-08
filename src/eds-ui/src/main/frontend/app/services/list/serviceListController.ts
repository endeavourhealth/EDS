/// <reference path="../../../typings/index.d.ts" />
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
			ServiceEditorController
				.open(vm.$modal, item)
				.result
				.then(function(result : Service) {
					vm.save(item, result);
				});
		}

		save(original : Service, edited : Service) {
			var vm = this;
			vm.serviceService.save(edited)
				.then(function(saved : Service) {
					if (original.uuid)
						jQuery.extend(true, original, saved);
					else
						vm.services.push(saved);

					vm.log.success('Service saved', original, 'Save service');
				})
				.catch(function (error : any) {
					vm.log.error('Failed to save service', error, 'Save service');
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

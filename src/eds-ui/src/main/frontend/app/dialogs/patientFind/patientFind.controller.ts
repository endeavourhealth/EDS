/// <reference path="../../../typings/index.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />

module app.dialogs {
	import IModalService = angular.ui.bootstrap.IModalService;
	import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
	'use strict';

	export class PatientFindController extends BaseDialogController {

		public static open($modal : IModalService) : IModalServiceInstance {
			var options : IModalSettings = {
				templateUrl: 'app/dialogs/patientFind/patientFind.html',
				controller: 'PatientFindController',
				controllerAs: 'ctrl',
				backdrop: 'static'
			};

			var dialog = $modal.open(options);
			return dialog;
		}

		static $inject = ['$uibModalInstance', '$uibModal', 'LoggerService'];

		constructor(protected $uibModalInstance : IModalServiceInstance,
								private $modal : IModalService,
								private log : ILoggerService) {
			super($uibModalInstance);
		}
	}

	angular
		.module('app.dialogs')
		.controller('PatientFindController', PatientFindController);
}

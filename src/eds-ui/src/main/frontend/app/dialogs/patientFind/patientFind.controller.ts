/// <reference path="../../../typings/index.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />

module app.dialogs {
	import IModalService = angular.ui.bootstrap.IModalService;
	import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
    import PatientFindSelection = app.models.PatientFindSelection;

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

            var selection = new PatientFindSelection();
            selection.serviceId = "db7eba14-4a89-4090-abf8-af6c60742cb1";
            selection.systemId = "db8fa60e-08ff-4b61-ba4c-6170e6cb8df7";
            selection.patientId = "81b66483-ac01-47dd-98a7-73b4b5639680";

            this.resultData = selection;
		}
	}

	angular
		.module('app.dialogs')
		.controller('PatientFindController', PatientFindController);
}

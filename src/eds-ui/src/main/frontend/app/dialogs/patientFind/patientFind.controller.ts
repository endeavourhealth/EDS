/// <reference path="../../../typings/index.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />

module app.dialogs {
	import IModalService = angular.ui.bootstrap.IModalService;
	import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
    import IRecordViewerService = app.core.IRecordViewerService;
    import Patient = app.models.Patient;

	'use strict';

	export class PatientFindController extends BaseDialogController {

        searchTerms: string;
        searchedTerms: string;
        foundPatients: Patient[];
        selectedPatient: Patient;

		public static open($modal : IModalService) : IModalServiceInstance {
			var options : IModalSettings = {
				templateUrl: 'app/dialogs/patientFind/patientFind.html',
				controller: 'PatientFindController',
				controllerAs: 'ctrl',
				backdrop: 'static',
                size: 'lg'
			};

			var dialog = $modal.open(options);
			return dialog;
		}

		static $inject = ['$uibModalInstance', '$uibModal', 'RecordViewerService', 'LoggerService'];

		constructor(protected $uibModalInstance : IModalServiceInstance,
								private $modal : IModalService,
                                protected recordViewerService: IRecordViewerService,
								private log : ILoggerService) {
			super($uibModalInstance);
		}

        ok() {
            this.resultData = this.selectedPatient;
            super.ok();
        }

		findPatient() {
            this.searchedTerms = this.searchTerms;

            var vm = this;
            vm.foundPatients = null;
            vm.recordViewerService.findPatient(vm.searchedTerms)
                .then(function (data: Patient[]) {
                    vm.foundPatients = data;
                    if (data == null) {
                    }
                });
        }

        selectPatient(patient: Patient) {
            if (this.selectedPatient == patient)
                this.selectedPatient = null;
            else
                this.selectedPatient = patient;
        }

        searchTermsChanged() {
            this.searchedTerms = null;
            this.foundPatients = null;
            this.selectedPatient = null;
        }
	}

	angular
		.module('app.dialogs')
		.controller('PatientFindController', PatientFindController);
}

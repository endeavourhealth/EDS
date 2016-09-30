/// <reference path="../../../typings/index.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />

module app.dialogs {
	import IModalService = angular.ui.bootstrap.IModalService;
	import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
    import IRecordViewerService = app.core.IRecordViewerService;
    import UIPatient = app.models.UIPatient;

	'use strict';

    enum KeyCodes {
        ReturnKey = 13,
        Escape = 27,
        LeftArrow = 37,
        UpArrow = 38,
        RightArrow = 39,
        DownArrow = 40
    }

	export class PatientFindController extends BaseDialogController {

        searchTerms: string;
        searchedTerms: string;
        foundPatients: UIPatient[];
        selectedPatient: UIPatient;

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
                .then(function (data: UIPatient[]) {
                    vm.foundPatients = data;
                    if (data == null) {
                    }
                });
        }

        selectPatient(patient: UIPatient) {
            if (this.selectedPatient == patient)
                this.selectedPatient = null;
            else
                this.selectedPatient = patient;
        }

        keydown($event: KeyboardEvent) {
            if ($event.keyCode == KeyCodes.UpArrow) {
                this.selectPreviousPatient();
            }
            else if ($event.keyCode == KeyCodes.DownArrow) {
                this.selectNextPatient();
            }
        }

        selectNextPatient() {
            if (this.foundPatients == null)
                return;

            let selectedPatientIndex: number =
                this.foundPatients.indexOf(this.selectedPatient);

            if (++selectedPatientIndex < this.foundPatients.length)
                this.selectedPatient = this.foundPatients[selectedPatientIndex];
        }

        selectPreviousPatient() {
            if (this.foundPatients == null)
                return;

            let selectedPatientIndex: number =
                this.foundPatients.indexOf(this.selectedPatient);

            if (--selectedPatientIndex >= 0)
                this.selectedPatient = this.foundPatients[selectedPatientIndex];
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

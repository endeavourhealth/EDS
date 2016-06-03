/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />

module app.dialogs {
	import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
	import IModalService = angular.ui.bootstrap.IModalService;
	import Report = app.models.Report;
	import RequestParameters = app.models.RequestParameters;
	import IOrganisationService = app.core.IOrganisationService;
	import OrganisationSet = app.models.OrganisationSet;

	'use strict';

	export class QueueReportController extends BaseDialogController {
		public static open($modal : IModalService, reportUuid : string, reportName : string) : IModalServiceInstance {
			var options : IModalSettings = {
				templateUrl:'app/dialogs/queueReport/queueReport.html',
				controller:'QueueReportController',
				controllerAs:'queueReport',
				// size:'lg',
				backdrop: 'static',
				resolve:{
					reportUuid : () => reportUuid,
					reportName : () => reportName
				}
			};

			var dialog = $modal.open(options);
			return dialog;
		}

		patientTypeDisplay : any;
		patientStatusDisplay : any;
		baselineDate : Date;

		static $inject = ['$uibModalInstance', 'LoggerService', 'AdminService', 'OrganisationService', '$uibModal',
			'reportUuid', 'reportName'];

		constructor(protected $uibModalInstance : IModalServiceInstance,
								private logger:app.blocks.ILoggerService,
								private adminService : IAdminService,
								private organisationService : IOrganisationService,
								private $modal : IModalService,
								private reportUuid : string,
								private reportName : string) {
			super($uibModalInstance);

			this.patientTypeDisplay = {
				regular : 'Regular patients',
				nonRegular : 'Non-regular patients',
				all : 'All patients'
			};

			this.patientStatusDisplay = {
				active : 'Active patients',
				all : 'Active and non-active patients'
			};

			var requestParameters:RequestParameters = {
				reportUuid: reportUuid,
				baselineDate: null,
				patientType: 'regular',
				patientStatus: 'active',
				organisation: []
			};

			this.resultData = requestParameters;
		}

		getOrganisationListDisplayText() {
			if (this.resultData.organisation && this.resultData.organisation.length > 0) {
				return this.resultData.organisation.length + ' Organisation(s)';
			} else {
				return 'All Organisations';
			}
		}

		pickOrganisationList() {
			var vm = this;
			OrganisationPickerController.open(vm.$modal, this.resultData.organisation, null)
				.result.then(function(organisationSet : OrganisationSet) {
					vm.resultData.organisation = organisationSet.organisations;
			});
		}

		clearOrganisationList() {
			this.resultData.organisation = [];
		}

		ok() {
			if (this.baselineDate) {
				this.resultData.baselineDate = this.baselineDate.valueOf();
			} else {
				this.resultData.baselineDate = null;
			}

			super.ok();
		}
	}

	angular
		.module('app.dialogs')
		.controller('QueueReportController', QueueReportController);
}

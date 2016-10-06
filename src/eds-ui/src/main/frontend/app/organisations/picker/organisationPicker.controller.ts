import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
import IModalSettings = angular.ui.bootstrap.IModalSettings;
import IModalService = angular.ui.bootstrap.IModalService;

import {BaseDialogController} from "../../dialogs/baseDialog.controller";
import {Organisation} from "../../models/Organisation";
import {IAdminService} from "../../core/admin.service";
import {IOrganisationService} from "../service/organisation.service";
import {ILoggerService} from "../../blocks/logger.service";

export class OrganisationPickerController extends BaseDialogController {
	public static open($modal : IModalService, organisations : Organisation[]) : IModalServiceInstance {
		var options : IModalSettings = {
			template:require('./organisationPicker.html'),
			controller:'OrganisationPickerController',
			controllerAs:'ctrl',
			backdrop: 'static',
			resolve:{
				organisations : () => organisations
			}
		};

		var dialog = $modal.open(options);
		return dialog;
	}

	static $inject = ['$uibModalInstance', 'LoggerService', 'AdminService', 'OrganisationService', 'organisations'];
	searchData : string;
	searchResults : Organisation[];

	constructor(protected $uibModalInstance : IModalServiceInstance,
							private logger:ILoggerService,
							private adminService : IAdminService,
							private organisationService : IOrganisationService,
							private organisations : Organisation[]) {
		super($uibModalInstance);
		this.resultData = jQuery.extend(true, [], organisations);
	}

	private search() {
		var vm = this;
		vm.organisationService.search(vm.searchData)
			.then(function (result : Organisation[]) {
				vm.searchResults = result;
			})
			.catch(function (error : any) {

			});
	}

	private addToSelection(match : Organisation) {
		if ($.grep(this.resultData, function(o:Organisation) { return o.uuid === match.uuid; }).length === 0)
			this.resultData.push(match);
	}

	private removeFromSelection(match : Organisation) {
		var index = this.resultData.indexOf(match, 0);
		if (index > -1)
			this.resultData.splice(index, 1);
	}

}

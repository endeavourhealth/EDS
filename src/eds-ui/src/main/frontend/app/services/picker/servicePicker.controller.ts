import {BaseDialogController} from "../../dialogs/baseDialog.controller";
import IModalService = angular.ui.bootstrap.IModalService;
import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
import IModalSettings = angular.ui.bootstrap.IModalSettings;

import {Service} from "../../models/Service";
import {IAdminService} from "../../core/admin.service";
import {ILoggerService} from "../../blocks/logger.service";
import {IServiceService} from "../service/service.service";

export class ServicePickerController extends BaseDialogController {
	public static open($modal : IModalService, services : Service[]) : IModalServiceInstance {
		var options : IModalSettings = {
			template:require('./servicePicker.html'),
			controller:'ServicePickerController',
			controllerAs:'ctrl',
			backdrop: 'static',
			resolve:{
				services : () => services
			}
		};

		var dialog = $modal.open(options);
		return dialog;
	}

	static $inject = ['$uibModalInstance', 'LoggerService', 'AdminService', 'ServiceService', 'services'];
	searchData : string;
	searchResults : Service[];

	constructor(protected $uibModalInstance : IModalServiceInstance,
							private logger:ILoggerService,
							private adminService : IAdminService,
							private serviceService : IServiceService,
							private services : Service[]) {
		super($uibModalInstance);
		this.resultData = jQuery.extend(true, [], services);
	}

	private search() {
		var vm = this;
		vm.serviceService.search(vm.searchData)
			.then(function (result : Service[]) {
				vm.searchResults = result;
			})
			.catch(function (error : any) {

			});
	}

	private addToSelection(match : Service) {
		if ($.grep(this.resultData, function(s:Service) { return s.uuid === match.uuid; }).length === 0)
			this.resultData.push(match);
	}

	private removeFromSelection(match : Service) {
		var index = this.resultData.indexOf(match, 0);
		if (index > -1)
			this.resultData.splice(index, 1);
	}

}

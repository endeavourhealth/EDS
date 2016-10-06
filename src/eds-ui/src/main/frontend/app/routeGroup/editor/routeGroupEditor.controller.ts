import IModalService = angular.ui.bootstrap.IModalService;
import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
import IModalSettings = angular.ui.bootstrap.IModalSettings;

import {BaseDialogController} from "../../dialogs/baseDialog.controller";
import {IAdminService} from "../../core/admin.service";
import {ILoggerService} from "../../blocks/logger.service";
import {RouteGroup} from "../RouteGroup";

export class RouteGroupEditorController extends BaseDialogController {
	public static open($modal : IModalService, routeGroup : RouteGroup) : IModalServiceInstance {
		var options : IModalSettings = {
			template:require('./routeGroupEditor.html'),
			controller:'RouteGroupEditorController',
			controllerAs:'ctrl',
			backdrop: 'static',
			resolve:{
				routeGroup : () => routeGroup
			}
		};

		var dialog = $modal.open(options);
		return dialog;
	}

	static $inject = ['$uibModalInstance', 'LoggerService', 'AdminService', 'routeGroup'];

	constructor(protected $uibModalInstance : IModalServiceInstance,
							private logger:ILoggerService,
							private adminService : IAdminService,
							private routeGroup : RouteGroup) {
		super($uibModalInstance);
		this.resultData = jQuery.extend(true, {}, routeGroup);
	}

	addFilter(filter : string) {
		this.resultData.regex += filter;
	}
}

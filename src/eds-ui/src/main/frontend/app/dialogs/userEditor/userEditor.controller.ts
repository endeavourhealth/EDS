import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
import IModalSettings = angular.ui.bootstrap.IModalSettings;
import IModalService = angular.ui.bootstrap.IModalService;

import {User} from "../../models/User";
import {BaseDialogController} from "../baseDialog.controller";
import {IAdminService} from "../../core/admin.service";
import {ILoggerService} from "../../blocks/logger.service";

export class UserEditorController extends BaseDialogController {
	public static open($modal : IModalService, user : User) : IModalServiceInstance {
		var options : IModalSettings = {
			template:require('./userEditor.html'),
			controller:'UserEditorController',
			controllerAs:'userEditor',
			backdrop: 'static',
			resolve:{
				user : () => user
			}
		};

		var dialog = $modal.open(options);
		return dialog;
	}

	static $inject = ['$uibModalInstance', 'LoggerService', 'AdminService', 'user'];

	constructor(protected $uibModalInstance : IModalServiceInstance,
							private logger:ILoggerService,
							private adminService : IAdminService,
							private user : User) {
		super($uibModalInstance);
		this.resultData = jQuery.extend(true, {}, user);
	}
}

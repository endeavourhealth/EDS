import IModalService = angular.ui.bootstrap.IModalService;
import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
import IModalSettings = angular.ui.bootstrap.IModalSettings;

import {BaseDialogController} from "../baseDialog.controller";

export class MessageBoxController extends BaseDialogController {
	title : string;
	message : string;
	okText : string;
	cancelText : string;

	public static open($modal : IModalService,
										 title : string,
										 message : string,
										 okText : string,
										 cancelText : string) : IModalServiceInstance {
		var options : IModalSettings = {
			templateUrl:require('./messageBox.html'),
			controller:'MessageBoxController',
			controllerAs:'ctrl',
			backdrop:'static',
			resolve: {
				title : () => title,
				message : () => message,
				okText : () => okText,
				cancelText : () => cancelText
			}
		};

		var dialog = $modal.open(options);
		return dialog;
	}
	static $inject = ['$uibModalInstance', 'title', 'message', 'okText', 'cancelText'];

	constructor(protected $uibModalInstance : IModalServiceInstance,
							title : string,
							message : string,
							okText : string,
							cancelText : string) {
		super($uibModalInstance);
		this.title = title;
		this.message = message;
		this.okText = okText;
		this.cancelText = cancelText;
	}
}

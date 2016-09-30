import {BaseDialogController} from "../baseDialog.controller";
import IModalService = angular.ui.bootstrap.IModalService;
import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
import IModalSettings = angular.ui.bootstrap.IModalSettings;

export class InputBoxController extends BaseDialogController {
	title : string;
	message : string;

	public static open($modal : IModalService,
										 title : string,
										 message : string,
										 value : string) : IModalServiceInstance {
		var options : IModalSettings = {
			template:require('./inputBox.html'),
			controller:'InputBoxController',
			controllerAs:'ctrl',
			backdrop:'static',
			resolve: {
				title : () => title,
				message : () => message,
				value : () => value
			}
		};

		var dialog = $modal.open(options);
		return dialog;
	}
	static $inject = ['$uibModalInstance', 'title', 'message', 'value'];

	constructor(protected $uibModalInstance : IModalServiceInstance,
							title : string,
							message : string,
							value : string) {
		super($uibModalInstance);
		this.title = title;
		this.message = message;
		this.resultData = value;
	}
}

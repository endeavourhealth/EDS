/// <reference path="../../../typings/index.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />

module app.dialogs {
	import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
	import IModalService = angular.ui.bootstrap.IModalService;
	'use strict';

	export class InputBoxController extends BaseDialogController {
		title : string;
		message : string;

		public static open($modal : IModalService,
											 title : string,
											 message : string,
											 value : string) : IModalServiceInstance {
			var options : IModalSettings = {
				templateUrl:'app/dialogs/inputBox/inputBox.html',
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

	angular
		.module('app.dialogs')
		.controller('InputBoxController', InputBoxController);
}

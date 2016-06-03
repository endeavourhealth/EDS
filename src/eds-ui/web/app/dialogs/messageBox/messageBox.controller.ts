/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />

module app.dialogs {
	import ITreeNode = AngularUITree.ITreeNode;
	import ILibraryService = app.core.ILibraryService;
	import LibraryService = app.core.LibraryService;
	import TermlexSearchResult = app.models.TermlexSearchResult;
	import TermlexSearchResultResult = app.models.TermlexSearchResultResult;
	import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
	import FolderNode = app.models.FolderNode;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
	import IModalService = angular.ui.bootstrap.IModalService;
	'use strict';

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
				templateUrl:'app/dialogs/messageBox/messageBox.html',
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

	angular
		.module('app.dialogs')
		.controller('MessageBoxController', MessageBoxController);
}

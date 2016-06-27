/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />

module app.codeSet {
	import LibraryItem = app.models.LibraryItem;
	import ILibraryService = app.core.ILibraryService;
	import IModalScope = angular.ui.bootstrap.IModalScope;
	import IWindowService = angular.IWindowService;
	import CodeSetValue = app.models.CodeSetValue;
	import ICodingService = app.core.ICodingService;
	import Concept = app.models.Concept;
	import CodePickerController = app.dialogs.CodePickerController;
	import LibraryItemModuleBase = app.library.LibraryItemModuleBase;
	'use strict';

	export class CodeSetController extends LibraryItemModuleBase {
		libraryItem : LibraryItem;
		termCache : any;

		static $inject = ['LibraryService', 'LoggerService',
			'$uibModal', 'AdminService', '$window', '$stateParams', 'CodingService'];

		constructor(
			protected libraryService : ILibraryService,
			protected logger : ILoggerService,
			protected $modal : IModalService,
			protected adminService : IAdminService,
			protected $window : IWindowService,
			protected $stateParams : {itemAction : string, itemUuid : string},
			protected codingService : ICodingService) {

			super(libraryService, adminService, logger, $window, $stateParams);
			this.termCache = {};
		}

		create(folderUuid : string) {
			super.create(folderUuid);
			this.libraryItem.codeSet =  {
					codingSystem: 'SNOMED_CT',
					codeSetValue: []
				};
		}

		termShorten(term : string) {
			term = term.replace(' (disorder)', '');
			term = term.replace(' (observable entity)', '');
			term = term.replace(' (finding)', '');
			return term;
		}

		getTerm(code : string) : string {
			var vm = this;
			var term = vm.termCache[code];
			if (term) { return term; }
			vm.termCache[code] = 'Loading...';

			vm.codingService.getPreferredTerm(code)
				.then(function(concept:Concept) {
					vm.termCache[code] = vm.termShorten(concept.preferredTerm);
				});

			return vm.termCache[code];
		}

		showCodePicker() {
			var vm = this;
			CodePickerController.open(vm.$modal, vm.libraryItem.codeSet.codeSetValue)
				.result.then(function(result) {
					vm.libraryItem.codeSet.codeSetValue = result;
			});
		}
	}

	angular
		.module('app.codeSet')
		.controller('CodeSetController', CodeSetController);
}

/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />

module app.protocol {
	import ILoggerService = app.blocks.ILoggerService;
	import IScope = angular.IScope;
	import ILibraryService = app.core.ILibraryService;
	import IModalService = angular.ui.bootstrap.IModalService;
	import IWindowService = angular.IWindowService;
	import LibraryItem = app.models.LibraryItem;
	import LibraryItemModuleBase = app.library.LibraryItemModuleBase;
	import Protocol = app.models.Protocol;

	'use strict';

	export class ProtocolController extends LibraryItemModuleBase {
		protected protocol : Protocol;

		enabled = [true, false];
		consent = [true, false];

		static $inject = ['LibraryService', 'LoggerService',
			'$uibModal', 'AdminService', '$window', '$stateParams'];

		constructor(
			protected libraryService : ILibraryService,
			protected logger : ILoggerService,
			protected $modal : IModalService,
			protected adminService : IAdminService,
			protected $window : IWindowService,
			protected $stateParams : {itemAction : string, itemUuid : string}) {

			super(libraryService, adminService, logger, $window, $stateParams);
		}

		create(folderUuid : string) {
			this.protocol = {
				enabled:true,
				patientConsent:true
			} as Protocol;

			this.libraryItem = {
				uuid : null,
				name : 'New item',
				description : '',
				folderUuid : folderUuid,
				protocol : this.protocol
			} as LibraryItem;

		}


	}

	angular
		.module('app.protocol')
		.controller('ProtocolController', ProtocolController);
}

/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.dashboard {
	import FolderItem = app.models.FolderItem;
	import ItemType = app.models.ItemType;
	import IDashboardService = app.core.IDashboardService;
	import ILoggerService = app.blocks.ILoggerService;
	'use strict';

	class DashboardController {
		recentDocumentsData:FolderItem[];

		static $inject = ['DashboardService', 'LoggerService', '$state'];

		constructor(private dashboardService:IDashboardService,
								private logger:ILoggerService,
								private $state : IStateService) {
			this.refresh();
		}

		refresh() {
			this.getRecentDocumentsData();
		}

		getRecentDocumentsData() {
			var vm:DashboardController = this;
			vm.recentDocumentsData = null;
			vm.dashboardService.getRecentDocumentsData()
				.then(function (data:FolderItem[]) {
					vm.recentDocumentsData = data;
				});
		}

		actionItem(item : FolderItem, action : string) {
			switch (item.type) {
				case ItemType.Query:
					this.$state.go('app.queryAction', {itemUuid: item.uuid, itemAction: action});
					break;
				case ItemType.ListOutput:
					this.$state.go('app.listOutputAction', {itemUuid: item.uuid, itemAction: action});
					break;
				case ItemType.CodeSet:
					this.$state.go('app.codeSetAction', {itemUuid: item.uuid, itemAction: action});
					break;
				case ItemType.Report:
					this.$state.go('app.reportAction', {itemUuid: item.uuid, itemAction: action});
					break;
				case ItemType.Protocol:
					this.$state.go('app.protocolAction', {itemUuid: item.uuid, itemAction: action});
					break;
				case ItemType.System:
					this.$state.go('app.systemAction', {itemUuid: item.uuid, itemAction: action});
					break;
			}
		}
	}

	angular
		.module('app.dashboard')
		.controller('DashboardController', DashboardController);
}

/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />

module app.listOutput {
	import ILoggerService = app.blocks.ILoggerService;
	import IScope = angular.IScope;
	import ILibraryService = app.core.ILibraryService;
	import ListReport = app.models.ListReport;
	import ListReportGroup = app.models.ListReportGroup;
	import IModalService = angular.ui.bootstrap.IModalService;
	import FieldOutput = app.models.FieldOutput;
	import IWindowService = angular.IWindowService;
	import LibraryItem = app.models.LibraryItem;
	import DataSource = app.models.DataSource;
	import TestEditorController = app.dialogs.TestEditorController;
	import Test = app.models.Test;
	import EntityMap = app.models.EntityMap;
	import Entity = app.models.Entity;
	import Field = app.models.Field;
	import LibraryItemModuleBase = app.library.LibraryItemModuleBase;
	'use strict';

	export class ListOutputController extends LibraryItemModuleBase {
		selectedListReportGroup : ListReportGroup;
		selectedFieldOutput : FieldOutput;
		dataSourceAvailableFields : Field[];
		entityMap : EntityMap;

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
			this.loadEntityMap();
		}

		loadEntityMap() {
			var vm = this;
			vm.libraryService.getEntityMap().then(function (result : EntityMap) {
				vm.entityMap = result;
			})
			.catch(function(data) {
				vm.logger.error('Error loading entity map', data, 'Error');
			});
		}

		selectDataSource(datasourceContainer : { dataSource : DataSource }) {
			var vm = this;
			var test : Test = null;

			if (datasourceContainer.dataSource) {
				test = {dataSource: datasourceContainer.dataSource} as Test;
			}

			TestEditorController.open(this.$modal, test, true)
				.result.then(function(dataSourceContainer : { dataSource : DataSource }) {
					datasourceContainer.dataSource = dataSourceContainer.dataSource;
					if (vm.selectedListReportGroup.heading === '') {
						vm.selectedListReportGroup.heading = vm.getDatasourceDisplayName();
					}

					vm.loadDataSourceAvailableFieldList();
					vm.adminService.setPendingChanges();
			});
		}

		loadDataSourceAvailableFieldList() {
			// Find entity in entitymap
			this.dataSourceAvailableFields = [];
			var entityName : string = this.selectedListReportGroup.fieldBased.dataSource.entity;

			var matchingEntities : Entity[] = $.grep(this.entityMap.entity, (e) => e.logicalName === entityName);
			if (matchingEntities.length === 1) {
				this.dataSourceAvailableFields = $.grep(matchingEntities[0].field, (e) => e.availability.indexOf('output') > -1);
			}
		}

		getFieldDisplayName(logicalName : string) : string {
			var matchingFields : Field[] = $.grep(this.dataSourceAvailableFields, (e) => e.logicalName === logicalName);
			if (matchingFields.length === 1) {
				return matchingFields[0].displayName;
			}
			return '<Select...>';
		}

		getDatasourceDisplayName() : string {
			if (this.selectedListReportGroup
				&& this.selectedListReportGroup.fieldBased
				&& this.selectedListReportGroup.fieldBased.dataSource) {
				var logicalName = this.selectedListReportGroup.fieldBased.dataSource.entity;
				var matchingEntities:Entity[] = $.grep(this.entityMap.entity, (e) => e.logicalName === logicalName);
				if (matchingEntities.length === 1) {
					return matchingEntities[0].displayName;
				}
			}
			return '<Unknown>';
		}

		addListGroup() {
			this.selectedListReportGroup = {
				heading: '',
				fieldBased: {
					dataSource: null,
					fieldOutput: []
				}
			} as ListReportGroup;
			this.libraryItem.listReport.group.push(this.selectedListReportGroup);
		}

		removeListGroup(scope : any) {
			this.libraryItem.listReport.group.splice(scope.$index, 1);
			if (this.selectedListReportGroup === scope.item) {
				this.selectedListReportGroup = null;
			}
		}

		addFieldOutput() {
			this.selectedFieldOutput = {
				heading : '',
				field : ''
			} as FieldOutput;
			this.selectedListReportGroup.fieldBased.fieldOutput.push(this.selectedFieldOutput);
		}

		removeFieldOutput(scope : any) {
			this.selectedListReportGroup.fieldBased.fieldOutput.splice(scope.$index, 1);
			if (this.selectedFieldOutput === scope.item) {
				this.selectedFieldOutput = null;
			}
		}

		setItemField(item : FieldOutput, field : Field) {
			item.field = field.logicalName;
			if (item.heading === '') {
				item.heading = field.displayName;
			}
		}

		create(folderUuid : string) {
			super.create(folderUuid);
			this.libraryItem.listReport = {
					group: []
				} as ListReport;
		}
	}

	angular
		.module('app.listOutput')
		.controller('ListOutputController', ListOutputController);
}

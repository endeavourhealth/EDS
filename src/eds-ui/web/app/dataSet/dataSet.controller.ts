/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />

module app.dataSet {
	import ILoggerService = app.blocks.ILoggerService;
	import IScope = angular.IScope;
	import ILibraryService = app.core.ILibraryService;
	import DataSet = app.models.DataSet;
	import Composition = app.models.Composition;
	import Section = app.models.Section;
	import Resource = app.models.Resource;
	import IModalService = angular.ui.bootstrap.IModalService;
	import IWindowService = angular.IWindowService;
	import LibraryItem = app.models.LibraryItem;
	import TestEditorController = app.dialogs.TestEditorController;
	import Test = app.models.Test;
	import EntityMap = app.models.EntityMap;
	import Entity = app.models.Entity;
	import Field = app.models.Field;
	import LibraryItemModuleBase = app.library.LibraryItemModuleBase;
	'use strict';

	export class dataSetController extends LibraryItemModuleBase {
		selectedComposition : Composition;
		selectedSection : Section;
		selectedResource : Resource;
		entityMap : EntityMap;

		sections = ["GP practice",
			"Referral details",
			"Patient demographics",
			"Special requirements",
			"Participation in research",
			"Admission details",
			"Handover details",
			"Outpatient details",
			"Discharge details",
			"Relevant clinical risk factors",
			"Reason for contact",
			"Presenting complaints or issues",
			"History",
			"Medication and medical devices",
			"Allergies and adverse reaction",
			"Safety alerts",
			"Legal information",
			"Social context",
			"Family history",
			"Review of systems",
			"Patient and carer concerns",
			"Examination findings",
			"Assessment scales",
			"Problems and issues",
			"Diagnoses",
			"Procedures",
			"Clinical summary",
			"Investigations and results",
			"Plan and requested actions",
			"Outstanding issues",
			"Information given",
			"Person completing record",
			"Person handing over record",
			"Person receiving handover",
			"Distribution list"];

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

		setCompositionHeading(heading : any) {
			this.selectedComposition.heading = heading;
		}

		setSectionHeading(heading : any) {
			this.selectedSection.heading = heading;
		}

		selectResource(resource : Resource, index : any) {
			var vm = this;
			var test : Test = null;

			if (resource.heading!="") {
				test = {resource} as Test;
			}

			TestEditorController.open(this.$modal, test, true)
				.result.then(function( test : Test ) {
					vm.selectedResource.heading = test.resource.heading;
					vm.selectedResource = test.resource;
					vm.selectedSection.resource[index] = test.resource;
					vm.adminService.setPendingChanges();
			});
		}

		addComposition() {
			this.selectedComposition = {
				heading: '',
				section: []
			} as Composition;
			this.libraryItem.dataSet.composition.push(this.selectedComposition);
		}

		removeComposition(scope : any) {
			this.libraryItem.dataSet.composition.splice(scope.$index, 1);
			if (this.selectedComposition === scope.item) {
				this.selectedComposition = null;
			}
		}

		addSection() {
			this.selectedSection = {
				heading : '',
				resource: []
			} as Section;
			this.selectedComposition.section.push(this.selectedSection);
		}

		removeSection(scope : any) {
			this.selectedComposition.section.splice(scope.$index, 1);
			if (this.selectedSection === scope.item) {
				this.selectedSection = null;
			}
		}

		addResource() {
			this.selectedResource = {
				heading : '',
				filter: []
			} as Resource;
			this.selectedSection.resource.push(this.selectedResource);
		}

		removeResource(scope : any) {
			this.selectedSection.resource.splice(scope.$index, 1);
			if (this.selectedResource === scope.item) {
				this.selectedResource = null;
			}
		}

		create(folderUuid : string) {
			super.create(folderUuid);
			this.libraryItem.dataSet = {
					composition: []
				} as DataSet;
		}
	}

	angular
		.module('app.dataSet')
		.controller('dataSetController', dataSetController);
}

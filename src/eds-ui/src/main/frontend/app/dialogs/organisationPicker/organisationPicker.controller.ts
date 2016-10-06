import IModalService = angular.ui.bootstrap.IModalService;
import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
import IModalSettings = angular.ui.bootstrap.IModalSettings;

import {BaseDialogController} from "../baseDialog.controller";
import {OrganisationSet} from "../../models/OrganisationSet/OrganisationSet";
import {OrganisationSetMember} from "../../models/OrganisationSet/OrganisationSetMember";
import {ILoggerService} from "../../blocks/logger.service";
import {IOrganisationSetService} from "../../core/organisationSet.service";
import {InputBoxController} from "../inputBox/inputBox.controller";
import {MessageBoxController} from "../messageBox/messageBox.controller";

export class OrganisationPickerController extends BaseDialogController {
	organisationSetList : OrganisationSet[];
	searchCriteria : string;
	searchResults : OrganisationSetMember[];
	editMode : boolean;

	public static open($modal : IModalService,
										 organisationList : OrganisationSetMember[],
										 organisationSet : OrganisationSet) : IModalServiceInstance {
		var options : IModalSettings = {
			template:require('./organisationPicker.html'),
			controller:'OrganisationPickerController',
			controllerAs:'ctrl',
			backdrop:'static',
			resolve: {
				organisationList : () => organisationList,
				organisationSet : () => organisationSet
			}
		};

		var dialog = $modal.open(options);
		return dialog;
	}

	static $inject = ['$uibModalInstance', '$uibModal', 'LoggerService', 'OrganisationSetService',
		'organisationList', 'organisationSet'];

	constructor(protected $uibModalInstance : IModalServiceInstance,
							private $modal : IModalService,
							private log : ILoggerService,
							private organisationSetService : IOrganisationSetService,
							organisationList : OrganisationSetMember[],
							organisationSet : OrganisationSet) {
		super($uibModalInstance);

		this.loadOrganisationSets();

		if (organisationSet) {
			this.editMode = true;
			this.selectSet(organisationSet);
		} else {
			this.editMode = false;
			this.resultData = {
				uuid: null,
				name: '<New Organisation Set>',
				organisations: organisationList
			};
		}
	}

	loadOrganisationSets() {
		var vm = this;
		vm.organisationSetService.getOrganisationSets()
			.then(function(result) {
				vm.organisationSetList = result;
			});
	}

	selectSet(organisationSet : OrganisationSet) {
		var vm = this;
		if (organisationSet === null) {
			// Clear uuid and name (but not organisation list in case of "Save As")
			vm.resultData.uuid = null;
			vm.resultData.name = '<New Organisation Set>';
		} else {
			// Create COPY of selected set (in case of "Save As")
			vm.resultData = {
				uuid : organisationSet.uuid,
				name : organisationSet.name
			};
			vm.organisationSetService.getOrganisationSetMembers(organisationSet.uuid)
				.then(function (result) {
					vm.resultData.organisations = result;
				});
		}
	}

	search() {
		var vm = this;
		vm.organisationSetService.searchOrganisations(vm.searchCriteria)
			.then(function(result) {
				vm.searchResults = result;
			});
	}

	addOrganisationToSelection(organisation : OrganisationSetMember) {
		if (this.resultData.organisations.every((item : OrganisationSetMember) => item.odsCode !== organisation.odsCode)) {
			this.resultData.organisations.push(organisation);
		}
	}

	removeOrganisationFromSelection(organisation : OrganisationSetMember) {
		var index = this.resultData.organisations.indexOf(organisation);
		this.resultData.organisations.splice(index, 1);
	}

	removeAll() {
		this.resultData.organisations = [];
	}

	saveSet() {
		var vm = this;
		if (vm.resultData.uuid === null) {
			InputBoxController.open(vm.$modal, 'Save Organisation Set', 'Enter Set Name', 'New Organisation Set')
				.result.then(function(result) {
					vm.resultData.name = result;
					vm.save();
			});
		} else {
			MessageBoxController.open(vm.$modal, 'Save Organisation Set',
				'You are about to update an existing set, are you sure you want to continue?', 'Yes', 'No')
				.result.then(function() {
					vm.save();
			});
		}
	}

	save() {
		var vm = this;
		vm.organisationSetService.saveOrganisationSet(vm.resultData)
			.then(function(result) {
				vm.log.success('Organisation Set Saved', result, 'Saved');
				if (vm.resultData.uuid === null) {
					vm.resultData.uuid = result.uuid;
					vm.organisationSetList.push(vm.resultData);
				}
			});
	}
}

import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService} from "ui-router-ng2";
import {Organisation} from "./models/Organisation";
import {LoggerService} from "../common/logger.service";
import {OrganisationService} from "./organisation.service";
import {MessageBoxDialog} from "../dialogs/messageBox/messageBox.dialog";

@Component({
	template: require('./organisationList.html')
})
export class OrganisationListComponent {
	organisations : Organisation[];

	constructor(private $modal: NgbModal,
							private organisationService : OrganisationService,
							private log : LoggerService,
							protected $state : StateService) {
		this.getOrganisations();
	}

	getOrganisations() {
		var vm = this;
		vm.organisationService.getOrganisations()
			.subscribe(
				result => vm.organisations = result,
				error => vm.log.error('Failed to load organisations', error, 'Load organisations')
			);
		console.log(vm.organisations);
	}

	add() {
		this.$state.go('app.organisationEdit', {itemUuid: null, itemAction: 'add'});
	}

	edit(item : Organisation) {
		console.log(item);
		this.$state.go('app.organisationEdit', {itemUuid: item.uuid, itemAction: 'edit'});
	}

	save(original : Organisation, edited : Organisation) {
		var vm = this;
		vm.organisationService.saveOrganisation(edited)
			.subscribe(
				saved =>  {
					if (original.uuid)
						jQuery.extend(true, original, saved);
					else
						vm.organisations.push(saved);

					vm.log.success('Organisation saved', original, 'Save organisation');
				},
				error => vm.log.error('Failed to save organisation', error, 'Save organisation')
			);
	}

	delete(item : Organisation) {
		var vm = this;
		MessageBoxDialog.open(vm.$modal, 'Delete Organisation', 'Are you sure you want to delete the Organisation?', 'Yes', 'No')
			.result.then(
				() => vm.doDelete(item),
				() => vm.log.info('Delete cancelled')
		);
	}

	doDelete(item : Organisation) {
		var vm = this;
		vm.organisationService.deleteOrganisation(item.uuid)
			.subscribe(
				() => {
					var index = vm.organisations.indexOf(item);
					vm.organisations.splice(index, 1);
					vm.log.success('Organisation deleted', item, 'Delete Organisation');
				},
				(error) => vm.log.error('Failed to delete Organisation', error, 'Delete Organisation')
			);
	}
}

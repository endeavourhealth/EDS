import {Component, Input} from "@angular/core";

import {Organisation} from "./models/Organisation";
import {OrganisationService} from "./organisation.service";
import {LoggerService} from "../common/logger.service";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
	selector: 'ngbd-modal-content',
	template: require('./organisationPicker.html')
})
export class OrganisationPickerDialog {
	public static open(modalService: NgbModal, organisations : Organisation[]) {
		const modalRef = modalService.open(OrganisationPickerDialog, { backdrop : "static"});
		modalRef.componentInstance.resultData = jQuery.extend(true, [], organisations);

		return modalRef;
	}

	@Input() resultData : Organisation[];
	searchData : string;
	searchResults : Organisation[];

	constructor(public activeModal: NgbActiveModal,
								 private log:LoggerService,
								 private organisationService : OrganisationService) {}

	private search() {
		var vm = this;
		vm.organisationService.search(vm.searchData)
			.subscribe(
				(result) => vm.searchResults = result,
				(error) => vm.log.error(error)
			);
	}

	private addToSelection(match : Organisation) {
		if ($.grep(this.resultData, function(o:Organisation) { return o.uuid === match.uuid; }).length === 0)
			this.resultData.push(match);
	}

	private removeFromSelection(match : Organisation) {
		var index = this.resultData.indexOf(match, 0);
		if (index > -1)
			this.resultData.splice(index, 1);
	}

	ok() {
		this.activeModal.close(this.resultData);
		console.log('OK Pressed');
	}

	cancel() {
		this.activeModal.dismiss('cancel');
		console.log('Cancel Pressed');
	}
}

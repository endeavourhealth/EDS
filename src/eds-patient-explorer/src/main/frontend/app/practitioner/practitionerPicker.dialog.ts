import {Input, Component} from "@angular/core";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {Practitioner} from "./models/Practitioner";
import {PractitionerService} from "./practitioner.service";
import {SecurityService} from "../security/security.service";

@Component({
	selector: 'ngbd-modal-content',
	template: require('./practitionerPicker.html')
})
export class PractitionerPickerDialog {
	public static open(modalService: NgbModal) {
		const modalRef = modalService.open(PractitionerPickerDialog, { backdrop : "static" });

		return modalRef;
	}

	@Input() selectedPractitioner;

	searchData : string;
	searchResults : Practitioner[];

	constructor(protected activeModal : NgbActiveModal,
							protected securityService : SecurityService,
							private practitionerService : PractitionerService) {
	}

	search() {
		var vm = this;
		let organisationUuid = vm.securityService.getCurrentUser().organisation;
		vm.practitionerService.search(vm.searchData, organisationUuid)
			.subscribe(
				(result) => {
				vm.searchResults = result;
			});
	}

	selectPractitioner(practitioner : Practitioner, withClose : boolean) {
		this.selectedPractitioner = practitioner;
		if (withClose)
			this.ok();
	}

	ok() {
		this.activeModal.close(this.selectedPractitioner);
		console.log('OK Pressed');
	}

	cancel() {
		this.activeModal.dismiss('cancel');
		console.log('Cancel Pressed');
	}
}

import {Component, Input} from "@angular/core";

import {Service} from "./models/Service";
import {LoggerService} from "../common/logger.service";
import {ServiceService} from "./service.service";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
	selector: 'ngbd-modal-content',
	template: require('./servicePicker.html')
})
export class ServicePickerDialog  {
	public static open(modalService: NgbModal, services : Service[]) {
	const modalRef = modalService.open(ServicePickerDialog, { backdrop : "static"});
	modalRef.componentInstance.resultData = jQuery.extend(true, [], services);

	return modalRef;
	}

	@Input() resultData : Service[];
	searchData : string;
	searchResults : Service[];

	constructor(public activeModal: NgbActiveModal,
							private log:LoggerService,
							private serviceService : ServiceService) {
	}

	private search() {
		var vm = this;
		vm.serviceService.search(vm.searchData)
			.subscribe(
				(result) => vm.searchResults = result,
				(error) => vm.log.error(error)
			);
	}

	private addToSelection(match : Service) {
		if ($.grep(this.resultData, function(s:Service) { return s.uuid === match.uuid; }).length === 0)
			this.resultData.push(match);
	}

	private removeFromSelection(match : Service) {
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

import {Component, Input} from '@angular/core';

import {NgbActiveModal, NgbModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
	selector: 'ngbd-modal-content',
	template: require('./inputBox.html')
})

export class InputBoxDialog {
	@Input() title : string;
	@Input() message : string;
	@Input() resultData : any;

	constructor(public activeModal: NgbActiveModal) {}

	public static open(modalService: NgbModal,
										 title : string,
										 message : string,
										 value : string) {
		const modalRef = modalService.open(InputBoxDialog, { backdrop : "static" });
		modalRef.componentInstance.title = title;
		modalRef.componentInstance.message = message;
		modalRef.componentInstance.resultData = value;

		return modalRef;
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

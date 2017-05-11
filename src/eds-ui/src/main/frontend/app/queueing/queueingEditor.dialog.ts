import {Routing} from "./Routing";
import {Component, Input} from "@angular/core";
import {NgbModal, NgbActiveModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";

@Component({
	selector : 'ngbd-modal-content',
	template : require('./queueingEditor.html')
})
export class QueueEditDialog {

	public static open(modalService: NgbModal, routing : Routing) {
		const modalRef = modalService.open(QueueEditDialog, { backdrop : "static"} as NgbModalOptions);
		modalRef.componentInstance.resultData = jQuery.extend(true, [], routing);
		modalRef.componentInstance.allowEditingExchange = !(routing.exchangeName && routing.exchangeName.length > 0);

		return modalRef;
	}

	@Input() resultData : Routing;
	@Input() allowEditingExchange: boolean;

	constructor(public activeModal: NgbActiveModal) {
	}

	addFilter(filter : string) {
		this.resultData.regex += filter;
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

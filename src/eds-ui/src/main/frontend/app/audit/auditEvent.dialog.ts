import {Component, Input} from "@angular/core";
import {NgbModal, NgbActiveModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {AuditEvent} from "./models/AuditEvent";

@Component({
	selector: 'ngbd-modal-content',
	template: require('./auditEvent.html')
})
export class AuditEventDialog {
	public static open(modalService: NgbModal, title : string, auditEvent : AuditEvent) {
		const modalRef = modalService.open(AuditEventDialog, { backdrop : "static"} as NgbModalOptions);
		modalRef.componentInstance.title = title;
		modalRef.componentInstance.auditEvent = auditEvent;

		return modalRef;
	}

	@Input() title : string;
	@Input() auditEvent : AuditEvent = <AuditEvent>{};

	constructor(protected activeModal : NgbActiveModal) {
	}

	getFormattedEventData() {
		let arrayOfLines = this.auditEvent.data.match(/[^\r\n]+/g);

		let formattedData = '';
		if (arrayOfLines.length > 0) {
			for(let pairIndex = 1; pairIndex < arrayOfLines.length; pairIndex += 2) {
				formattedData += arrayOfLines[pairIndex] + " : " + arrayOfLines[pairIndex + 1] + '\n';
			}
		}

		return formattedData;
	}

	ok() {
		this.activeModal.close();
		console.log('OK Pressed');
	}
}

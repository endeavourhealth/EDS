import {LoggingEvent} from "./models/LoggingEvent";
import {Component, Input} from "@angular/core";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
	selector: 'ngbd-modal-content',
	template: require('./logEntry.html')
})
export class LogEntryDialog {
	public static open(modalService: NgbModal, logEntry : LoggingEvent, stackTrace : string) {
		const modalRef = modalService.open(LogEntryDialog, { backdrop : "static", size : 'lg'});
		modalRef.componentInstance.logEntry = logEntry;
		modalRef.componentInstance.stackTrace = stackTrace;

		return modalRef;
	}

	@Input() logEntry : LoggingEvent;
	@Input() stackTrace : string;

	constructor(protected activeModal : NgbActiveModal) {
	}

	getLevelIcon(level : string) {
		switch (level) {
			case "TRACE" :
				return "fa fa-fw fa-search text-success";
			case "DEBUG":
				return "fa fa-fw fa-bug text-primary";
			case "INFO":
				return "fa fa-fw fa-info text-info";
			case "WARN" :
				return "fa fa-fw fa-exclamation-circle text-warning";
			case "ERROR":
				return "fa fa-fw fa-ban text-danger";
			case "FATAL":
				return "fa fa-fw fa-stop text-danger";
			default:
				return "fa fa-fw fa-space";
		}
	}

	ok() {
		this.activeModal.close();
		console.log('OK Pressed');
	}
}

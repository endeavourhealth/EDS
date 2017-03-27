import {Component, Input} from '@angular/core';

import {NgbActiveModal, NgbModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
	selector: 'ngbd-modal-content',
	template: require('./messageBox.html')
})
export class MessageBoxDialog {
	@Input() title;
	@Input() message;
	@Input() okText;
	@Input() cancelText;

	constructor(public activeModal: NgbActiveModal) {}

	public static open(modalService: NgbModal,
											title : string,
											message : string,
											okText : string,
											cancelText : string) {
		return MessageBoxDialog.openWithSize(modalService, title, message, okText, cancelText, 'sm');
	}

	public static openLarge(modalService: NgbModal,
											title : string,
											message : string,
											okText : string,
											cancelText : string) {
		return MessageBoxDialog.openWithSize(modalService, title, message, okText, cancelText, 'lg');
	}

	private static openWithSize(modalService: NgbModal,
										title : string,
										message : string,
										okText : string,
										cancelText : string,
										size : 'sm' | 'lg') {
		const modalRef = modalService.open(MessageBoxDialog, { backdrop : "static", size: size});
		modalRef.componentInstance.title = title;
		modalRef.componentInstance.message = message;
		modalRef.componentInstance.okText = okText;
		modalRef.componentInstance.cancelText = cancelText;

		return modalRef;
	}


}
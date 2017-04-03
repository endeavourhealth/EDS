import {User} from "./models/User";
import {Component, Input} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
	selector: 'ngbd-modal-content',
	template: require('./userEditor.html')
})
export class UserEditorDialog {
	public static open(modalService: NgbModal, user : User) {
		const modalRef = modalService.open(UserEditorDialog, { backdrop : "static"});
		modalRef.componentInstance.resultData = jQuery.extend(true, [], user);

		return modalRef;
	}

	@Input() resultData : User;
}

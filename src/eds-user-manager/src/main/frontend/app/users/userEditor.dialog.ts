import {User} from "./models/User";
import {Component, Input, ViewChild, ViewChildren} from "@angular/core";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UserService} from "./user.service";
import {UserRole} from "./models/UserRole";
import {LoggerService, MessageBoxDialog} from "eds-common-js";

@Component({
	selector: 'ngbd-modal-content',
	template: require('./userEditor.html')
})
export class UserEditorDialog {
	public static open(modalService: NgbModal, user: User, editMode) {
		const modalRef = modalService.open(UserEditorDialog, {backdrop: "static", size: "lg"});
		modalRef.componentInstance.resultData = jQuery.extend(true, [], user);
		modalRef.componentInstance.editMode = editMode;
		modalRef.componentInstance.$modal = modalService;
		return modalRef;
	}

	@Input() resultData: User;
	@Input() editMode: Boolean;
	@Input() $modal: NgbModal;
	dialogTitle: String;
	availableRoles: UserRole[];

	@ViewChild('username') usernameBox;
	@ViewChild('forename') forenameBox;
	@ViewChild('surname') surnameBox;
	@ViewChild('email') emailBox;
	@ViewChild('photo') photoURLBox;
	@ViewChild('password1') password1Box;
	@ViewChild('password2') password2Box;
	@ViewChildren('username') vc;

	selectedCurrentRole: UserRole;
	selectedAvailableRole: UserRole;

	constructor(private log: LoggerService,
				protected activeModal: NgbActiveModal,
				protected userService: UserService) {

	}

	ngOnInit(): void {
		if (!this.editMode) {
			this.dialogTitle = "Add user";

			this.resultData = {
				uuid: null,
				forename: '',
				surname: '',
				username: '',
				password: '',
				email: '',
				mobile: '',
				photo: '',
				defaultOrgId: '',
				userRoles: []
			} as User;
		}
		else {
			this.dialogTitle = "Edit user";

			this.resultData = {
				uuid: this.resultData.uuid,
				forename: this.resultData.forename,
				surname: this.resultData.surname,
				username: this.resultData.username,
				password: '',
				email: this.resultData.email,
				mobile: this.resultData.mobile,
				photo: this.resultData.photo == null ? '': this.resultData.photo,
				defaultOrgId: this.resultData.defaultOrgId == null ? '': this.resultData.defaultOrgId,
				userRoles: this.resultData.userRoles
			} as User;
		}

		this.getAvailableRealmRoles();
	}

	isEditMode(){
		return this.editMode;
	}

	ngAfterViewInit() {
		if (!this.isEditMode()) {
			this.usernameBox.nativeElement.focus();
		}
		else
			this.forenameBox.nativeElement.focus();
	}

	save() {
		if (this.validateFormInput() == true) {
			this.activeModal.close(this.resultData);
		}
	}

	cancel() {
		MessageBoxDialog.open(this.$modal, "Confirmation", "Are you sure you want to cancel?", "Yes", "No")
            .result.then(
			(result) => {
				this.activeModal.dismiss('cancel');
			},
			(reason) => {}
		);
	}

	getAvailableRealmRoles() {
		var vm = this;
		var userId = vm.resultData.uuid;
		vm.userService.getAvailableRealmRoles(userId)
            .subscribe(
				(result) => vm.availableRoles = result,
				(error) => vm.log.error('Error loading available realm roles', error, 'Error')
			);
	}

	//remove role from current into available table, i.e. remove from resultData
	removeCurrentRole(currentRole) {
		let i = this.resultData.userRoles.indexOf(currentRole);
		if (i !== -1) {
			this.resultData.userRoles.splice(i, 1);
		}

		this.availableRoles.push(currentRole);
	}

	//remove from available and add into current, i.e. add into resultData
	addAvailableRole(availableRole) {
		let i = this.availableRoles.indexOf(availableRole);
		if (i !== -1) {
			this.availableRoles.splice(i, 1);
		}

		this.resultData.userRoles.push(availableRole);
	}

	buildRoleToolTip(role){
		let toolTip = "";
		for (let i = 0; i < role.clientRoles.length; ++i)
		{
			toolTip += role.clientRoles[i].name;
			if (i < role.clientRoles.length-1){toolTip += ', ';}
		}

		return toolTip;
	}

	validateFormInput(){
		//go down each tab. check content and flip to and highlight if not complete
		var vm = this;
		var result = true;

		//username is mandatory
		if (this.resultData.username.trim() == '') {
			vm.log.warning('Username must not be blank');
			this.usernameBox.nativeElement.focus();
			result = false;
		} else
		//forename is mandatory
		if (this.resultData.forename.trim() == '') {
			vm.log.warning('Forename must not be blank');
			this.forenameBox.nativeElement.focus();
			result = false;
		} else
		//surname is mandatory
		if (this.resultData.surname.trim() == '') {
			vm.log.warning('Surname must not be blank');
			this.surnameBox.nativeElement.focus();
			result = false;
		} else
		//email is mandatory
		if (this.resultData.email.trim() == '') {
			vm.log.warning('Email address must not be blank');
			this.emailBox.nativeElement.focus();
			result = false;
		} else
		if (this.resultData.photo != null && this.resultData.photo.length>100) {
			vm.log.warning('Length of image URL is too long. Consider using bitly or similar to shorten it');
			this.photoURLBox.nativeElement.focus();
			result = false;
		} else
		//check changed passwords match and are valid for a new user addition
		{
			let passwordInput = this.resultData.password.trim();
			if (passwordInput == '') {
				if (!this.isEditMode()) {
					vm.log.warning('Password must not be blank');
					this.password1Box.nativeElement.focus();
					result = false;
				}
			} else
			//passwords must match and map onto the password policy (1 upper, 1 digit, 8 length and not be the same as username)
			if (this.password2Box.nativeElement.value != this.password1Box.nativeElement.value) {
				vm.log.warning('Passwords must match');
				this.password2Box.nativeElement.focus();
				result = false;
			} else if (passwordInput.length < 8) {
				vm.log.warning('Password must be at least 8 characters long');
				this.password1Box.nativeElement.focus();
				result = false;
			} else if (!/\d/.test(passwordInput)) {
				vm.log.warning('Password must contain at least 1 number');
				this.password1Box.nativeElement.focus();
				result = false;
			} else if (!/[A-Z]/.test(passwordInput)) {
				vm.log.warning('Password must contain at least 1 Uppercase letter');
				this.password1Box.nativeElement.focus();
				result = false;
			} else if (passwordInput == this.resultData.username.trim()) {
				vm.log.warning('Password cannot be the same as username');
				this.password1Box.nativeElement.focus();
				result = false;
			}
		}

		return result;
	}
}

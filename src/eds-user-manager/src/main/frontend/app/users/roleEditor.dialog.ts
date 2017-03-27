import {Component, Input, ViewChild} from "@angular/core";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UserService} from "./user.service";
import {UserRole} from "./models/UserRole";
import {LoggerService} from "../common/logger.service";
import {MessageBoxDialog} from "../dialogs/messageBox/messageBox.dialog";
import {Organisation} from "./models/Organisation";
import {Client} from "./models/Client";

@Component({
	selector: 'ngbd-modal-content',
	template: require('./roleEditor.html')
})
export class RoleEditorDialog {
	public static open(modalService: NgbModal, role : UserRole, editMode) {
		const modalRef = modalService.open(RoleEditorDialog, { backdrop : "static", size: "lg" });
		modalRef.componentInstance.resultData = jQuery.extend(true, [], role);
		modalRef.componentInstance.editMode = editMode;
		modalRef.componentInstance.$modal = modalService;
		return modalRef;
	}

	@Input() resultData : UserRole;
	@Input() editMode : Boolean;
	@Input() $modal: NgbModal;
	dialogTitle : String;
	availableClients : Client[];

	@ViewChild('rolename') rolenameBox;
	@ViewChild('clientlist') clientList;

	constructor(private log:LoggerService,
				protected activeModal: NgbActiveModal,
				protected userService: UserService) {

	}

	ngOnInit(): void {
		if (!this.editMode) {
			this.dialogTitle = "Add role";

			this.resultData = {
				uuid: '',
				name: '',
				description: '',
				//isClient: false,
				organisation: new Organisation(),
				clientRoles: []
			} as UserRole;
		}
		else
			this.dialogTitle = "Edit role";

		this.getRealmClients();
	}

	ngAfterViewInit() {
		this.rolenameBox.nativeElement.focus();
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

	getRealmClients(){
		var vm = this;
		vm.userService.getRealmClients()
            .subscribe(
				(result) => vm.availableClients = result,
				(error) => vm.log.error('Error loading realm clients', error, 'Error')
			);
	}

	validateFormInput(){
		//go down each tab. check content and flip to and highlight if not complete
		var vm = this;
		var result = true;

		//rolename is mandatory
		if (this.resultData.name.trim() == '') {
			vm.log.warning('Role name must not be blank');
			this.rolenameBox.nativeElement.focus();
			result = false;
		}

		//check user has at least one client access role
		if (this.resultData.clientRoles.length < 1){
			vm.log.warning('You must select at least one client access profile');
			this.clientList.nativeElement.focus();
			result = false;
		}


		return result;
	}

	processCheckedClientRole(e, clientRole) {
		var vm = this;

		if (e == true){
			//Add selected client role into the array
			this.resultData.clientRoles.push(clientRole);
		}
		else {
			//Remove unselected client role from the array
			var i = this.resultData.clientRoles.indexOf(clientRole);
			if (i !== -1) {
				this.resultData.clientRoles.splice(i, 1);
			}
		}
	}
}

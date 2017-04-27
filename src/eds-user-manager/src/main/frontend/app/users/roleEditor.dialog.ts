import {Component, Input, ViewChild, Pipe, PipeTransform} from "@angular/core";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UserService} from "./user.service";
import {UserRole} from "./models/UserRole";
import {LoggerService, MessageBoxDialog} from "eds-common-js";
import {Organisation} from "./models/Organisation";
import {Client} from "./models/Client";

@Component({
	selector: 'ngbd-modal-content',
	template: require('./roleEditor.html')
})
export class RoleEditorDialog {
	public static open(modalService: NgbModal, role : UserRole, editMode : boolean,  roleList : UserRole[]) {
		const modalRef = modalService.open(RoleEditorDialog, { backdrop : "static", size: "lg" });
		modalRef.componentInstance.resultData = jQuery.extend(true, [], role);
		modalRef.componentInstance.editMode = editMode;
		modalRef.componentInstance.roleList = roleList;
		modalRef.componentInstance.$modal = modalService;
		return modalRef;
	}

	@Input() resultData : UserRole;
	@Input() editMode : Boolean;
	@Input() roleList : UserRole[];
	@Input() $modal: NgbModal;
	dialogTitle : String;
	availableClients : Client[];

	@ViewChild('rolename') rolenameBox;
	@ViewChild('description') roledescBox;
	@ViewChild('clientlist') clientList;

	constructor(private log:LoggerService,
				protected activeModal: NgbActiveModal,
				protected userService: UserService) {

	}

	isEditMode(){
		return this.editMode;
	}

	ngAfterViewInit() {
		if (!this.isEditMode()) {
			this.rolenameBox.nativeElement.focus();
		}
		else
			this.roledescBox.nativeElement.focus();
	}

	ngOnInit(): void {
		if (!this.editMode) {
			this.dialogTitle = "Add role";

			this.resultData = {
				uuid: null,
				name: '',
				description: '',
				organisation: new Organisation(),
				clientRoles: []
			} as UserRole;
		}
		else {
			this.dialogTitle = "Edit role";

			this.resultData = {
				uuid: this.resultData.uuid,
				name: this.resultData.name,
				description: this.resultData.description,
				organisation: this.resultData.organisation,
				clientRoles: this.resultData.clientRoles
			} as UserRole;
		}

		this.getRealmClients();
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

	searchOrganisations(){

        //*** Cannot import cross module components   **
	    // var organisation = this.resultData.organisation[0];
         // OrganisationManagerPickerDialog.open(this.$modal, organisation, 'organisation' )
         //     .result.then(function (result : Organisation[]) {
         //     this.resultData.organisation = result;
         // });
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
		var vm = this;
		var result = true;

		//rolename is mandatory
		var roleName = this.resultData.name.trim();
		if (roleName == '') {
			vm.log.warning('Role name must not be blank');
			vm.rolenameBox.nativeElement.focus();
			result = false;
		}else {
			if (!vm.isEditMode() && vm.checkRoleNameExists(roleName)) {
				vm.log.warning('Role name already exists');
				vm.rolenameBox.nativeElement.focus();
				result = false;
			} else
			//check user has at least one client access role
			if (this.resultData.clientRoles.length < 1){
				vm.log.warning('You must select at least one client access profile');
				vm.clientList.nativeElement.focus();
				result = false;
			}
		}

		return result;
	}

	checkRoleNameExists (roleName){
		var result = false;

		for (var i = 0; i <= this.roleList.length-1; ++i){
			if (this.roleList[i].name == roleName) {
				result = true;
				break;
			}
		}
		return result;
	}

	processCheckedClientRole(e, clientRole) {
		var vm = this;

		if (e.currentTarget.checked == true){
			//Add selected client role into the array
			vm.resultData.clientRoles.push(clientRole);
		}
		else {
			//Remove unselected client role from the array
			var i = this.assignedClientRoleIndex(clientRole);
			if (i !== -1) {
				vm.resultData.clientRoles.splice(i, 1);
			}
		}
	}

	isClientRoleAssigned(availableClientRole)
	{
		var result = false;

		for (var i = 0; i <= this.resultData.clientRoles.length-1; ++i){
			if (availableClientRole.uuid == this.resultData.clientRoles[i].uuid) {
				result = true;
				break;
			}
		}
		return result;
	}

	assignedClientRoleIndex(availableClientRole)
	{
		var result = -1;

		for (var i = 0; i <= this.resultData.clientRoles.length-1; ++i){
			if (availableClientRole.uuid == this.resultData.clientRoles[i].uuid) {
				result = i;
				break;
			}
		}
		return result;
	}

	updateRoleName ($event){
		var roleName = $event;
		roleName = roleName.replace(' ','_');
		this.resultData.name = roleName;
	}
}

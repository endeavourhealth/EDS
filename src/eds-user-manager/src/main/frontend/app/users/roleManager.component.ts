import {NgbModal, NgbTabChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {Component} from "@angular/core";
import {UserService} from "./user.service";
import {UserRole} from "./models/UserRole";
import {RoleEditorDialog} from "./roleEditor.dialog";
import {LoggerService, MessageBoxDialog} from "eds-common-js";

@Component({
	template : require('./roleManager.html')
})
export class RoleManagerComponent {
	roleList : UserRole[];
	selectedRole : UserRole;

	constructor(private log:LoggerService,
							private userService : UserService,
							private $modal : NgbModal) {

		this.getRealmRoles();
	}

	editRole(role:UserRole) {
		var vm = this;
		RoleEditorDialog.open(vm.$modal, role, true, vm.roleList)
			.result.then(
			(editedRole) => vm.saveRole(role, editedRole),
			() => vm.log.info('Role edit cancelled')
		);
	}

	addRole() {
		var vm = this;
		RoleEditorDialog.open(vm.$modal, null, false, vm.roleList)
            .result.then(
			(editedRole) => vm.saveRole(null, editedRole),
			() => vm.log.info('Role add cancelled')
		);
	}

	private saveRole(role, editedRole : UserRole) {
		var vm = this;
		var editMode = (role != null);
		vm.userService.saveRole(editedRole, editMode)
			.subscribe(
				(response) => {
					this.getRealmRoles();
					this.selectedRole = editedRole;
					var msg = (!editMode) ? 'Add role' : 'Edit role';
					vm.log.success('Role saved', editedRole, msg);
				},
				(error) => vm.log.error('Error saving role', error, 'Error')
			);
	}

	deleteRole(role:UserRole) {
		var vm = this;

		var roleName = role.name;
		MessageBoxDialog.open(vm.$modal, "Confirmation", "Delete role: " + roleName.trim() + "?", "Yes", "No")
                .result.then(
				(result) => {
					vm.userService.deleteRole(roleName)
                        .subscribe(
							(result) => {
								result;
								this.getRealmRoles();
								this.selectedRole = null;
								vm.log.info("Role deleted");
							},
							(error) => vm.log.error('Error deleting role', error, 'Error')
						);
				},
				(reason) => {
				}
			);
	}

	getRoleList() {
		// Perform ordering and filtering here?
		return this.roleList;
	}

	getRealmRoles(){
		var vm = this;
		vm.userService.getRealmRoles()
            .subscribe(
				(result) => vm.roleList = result,
				(error) => vm.log.error('Error loading realm roles', error, 'Error')
			);
	}

	setSelectedRole(role) {
		this.selectedRole = role;
	}


}


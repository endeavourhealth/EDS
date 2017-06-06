import {User} from "./models/User";
import {UserEditorDialog} from "./userEditor.dialog";
import {NgbModal, NgbTabChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {Component} from "@angular/core";
import {UserService} from "./user.service";
import {UserRole} from "./models/UserRole";
import {SecurityService, MessageBoxDialog, LoggerService} from "eds-common-js";
import {Group} from "./models/Group";

@Component({
	template : require('./userManager.html')
})
export class UserManagerComponent {
	userList : User[];
	searchTerm : string;
	selectedUser : User = null;
	searched : Boolean;
	selectedRole : UserRole;
	activeTabId : string;
	userRoleList : UserRole[];
	loggedOnUserUuid : string;

	constructor(public log:LoggerService,
							private userService : UserService,
							private securityService : SecurityService,
							private $modal : NgbModal) {
		this.searched = false;
		this.loggedOnUserUuid = this.securityService.getCurrentUser().uuid;

		this.getUsers();
	}

	editUser(user:User) {
		var vm = this;
		UserEditorDialog.open(vm.$modal, user, true)
			.result.then(
			(editedUser) => vm.saveUser(user, editedUser),
			() => vm.log.info('User edit cancelled')
		);
	}

	addUser() {
		var vm = this;
		UserEditorDialog.open(vm.$modal, null, false)
            .result.then(
			(editedUser) => vm.saveUser(null, editedUser),
			() => vm.log.info('User add cancelled')
		);
	}

	createGroup() {
		var vm = this;
		vm.userService.createGroup()
            .subscribe(
				(response) => {
					vm.log.success('Group created', response, 'Success');
				},
				(error) => vm.log.error('Error creating group', error, 'Error')
			);
	}

	private saveUser(user, editedUser : User) {
		var vm = this;
		var editMode = (user != null);

		vm.userService.saveUser(editedUser, editMode)
			.subscribe(
				(response) => {
					this.getUsers();
					this.selectedUser = response;
					this.getUserRoles(response);
					var msg = (!editMode) ? 'Add user' : 'Edit user';
					vm.log.success('User saved', response, msg);
				},
				(error) => vm.log.error('Error saving user', error, 'Error')
			);
	}

	deleteUser(user:User) {
		var vm = this;
		if (user.uuid == this.loggedOnUserUuid)
		{
			vm.log.warning("You cannot delete yourself!");
		}
		else {
			let userName = user.forename + " " + user.surname;

			MessageBoxDialog.open(vm.$modal, "Confirmation", "Delete user: " + userName.trim() + "?", "Yes", "No")
                .result.then(
				(result) => {
					var userId = user.uuid;
					vm.userService.deleteUser(userId)
                        .subscribe(
							(result) => {
								result;
								this.getUsers();
								this.selectedUser = null;
								vm.log.info("User deleted");
							},
							(error) => vm.log.error('Error deleting user', error, 'Error')
						);
				},
				(reason) => {
				}
			);
		}
	}

	getUserList() {
		// Perform ordering and filtering here?
		return this.userList;
	}

	getUserRoleList() {
		var vm = this;
		vm.selectedUser.userRoles = this.userRoleList;
		return this.userRoleList;
	}

	getUsers(){
		var vm = this;
		vm.userService.getUsers()
			.subscribe(
				(result) => vm.userList = result,
				(error) => vm.log.error('Error loading users and roles', error, 'Error')
			);
	}

	getUserRoles(user){
		var vm = this;
		var userId = user.uuid;
		vm.userRoleList = null;
		vm.userService.getAssignedRoles(userId)
            .subscribe(
				(result) => vm.userRoleList = result,
				(error) => vm.log.error('Error loading user roles', error, 'Error')
			);
	}

	searchUsers(){
		var vm = this;

		if (this.searchTerm) {
			vm.userService.getUsersSearch(vm.searchTerm)
                .subscribe(
					(result) => vm.userList = result,
					(error) => vm.log.error('Error loading user search', error, 'Error')
				);
			vm.searched = true;
		}
	}

	enableSearch(){
		return this.searchTerm.length > 1;
	}


	clearSearch(){
		this.searched = false;
		this.searchTerm = "";
		this.getUsers();
	}

	setSelectedRole(role) {
		this.selectedRole = role;
	}

	tabChange($event: NgbTabChangeEvent) {
		this.activeTabId = $event.nextId;
	}
}


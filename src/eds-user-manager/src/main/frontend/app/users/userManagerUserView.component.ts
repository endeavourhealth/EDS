import {User} from "./models/User";
import {UserEditorDialog} from "./userEditor.dialog";
import {NgbModal, NgbTabChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {Component} from "@angular/core";
import {UserService} from "./user.service";
import {UserRole} from "./models/UserRole";
import {LoggerService, SecurityService, MessageBoxDialog} from "eds-common-js";

@Component({
	template : require('./userManagerUserView.html')
})
export class UserManagerUserViewComponent {
	selectedUser : User = null;
	selectedRole : UserRole;
	userRoleList : UserRole[];
	loggedOnUserUuid : string;

	constructor(private log:LoggerService,
							private userService : UserService,
							private securityService : SecurityService,
							private $modal : NgbModal) {

			this.loggedOnUserUuid = this.securityService.getCurrentUser().uuid;

			this.loadUser();
	}

	loadUser()
	{
		var vm = this;
		vm.selectedUser = null;
		let userId = vm.loggedOnUserUuid;

		vm.userService.getUser(userId)
            .subscribe(
				(result) => vm.selectedUser = result,
				(error) => vm.log.error('Error loading user', error, 'Error')
			);
	}

	editUser(user:User) {
		var vm = this;
		UserEditorDialog.open(vm.$modal, user, true)
			.result.then(
			(editedUser) => vm.saveUser(user, editedUser),
			() => vm.log.info('User edit cancelled')
		);
	}

	private saveUser(user, editedUser : User) {
		var vm = this;
		vm.userService.saveUser(editedUser, true)
			.subscribe(
				(response) => {
					this.selectedUser = response;
					this.getUserRoles(response);
					vm.log.success('User saved', response, 'Edit user');
				},
				(error) => vm.log.error('Error saving user', error, 'Error')
			);
	}

	getUserRoles(user){
		var vm = this;
		var userId = user.uuid;
		vm.userService.getAssignedRoles(userId)
            .subscribe(
				(result) => vm.userRoleList = result,
				(error) => vm.log.error('Error loading user roles', error, 'Error')
			);
	}


}


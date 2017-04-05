import {User} from "./models/User";
import {UserEditorDialog} from "./userEditor.dialog";
import {LoggerService} from "eds-common-js";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Component} from "@angular/core";
import {UserService} from "./user.service";

@Component({
	template : require('./userList.html')
})
export class UserListComponent {
	userType : string;
	userList : User[];

	constructor(private log:LoggerService,
							private userService : UserService,
							private $modal : NgbModal) {
		this.userType = 'all';
		this.loadUsers();
	}

	editUser(user:User) {
		var vm = this;
		UserEditorDialog.open(vm.$modal, user)
			.result.then(
			(editedUser) => vm.saveUser(user, editedUser),
			() => vm.log.info('User edit cancelled')
		);
	}

	private saveUser(user, editedUser : User) {
		var vm = this;
		vm.userService.saveUser(editedUser)
			.subscribe(
				(response) => {
					editedUser.uuid = response.uuid;
					var i = vm.userList.indexOf(user);
					vm.userList[i] = editedUser;
					vm.log.success('User saved', editedUser, 'Edit user');
				},
				(error) => vm.log.error('Error saving', error, 'Error')
			);
	}

	viewUser(user:User) {
		var vm = this;
		UserEditorDialog.open(vm.$modal, user);
	}

	deleteUser(user:User) {
		this.log.error('Delete ' + user.username);
	}

	getUserList() {
		// Perform ordering and filtering here?
		return this.userList;
	}

	private loadUsers() {
		var vm = this;
		vm.userService.getUserList()
			.subscribe(
				(result) => vm.userList = result.users,
				(error) => vm.log.error('Error loading users', error, 'Error')
			);
	}
}


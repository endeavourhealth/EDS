import {User} from "../models/User";
import {UserEditorDialog} from "./userEditor.dialog";
import {AdminService} from "./admin.service";
import {LoggerService} from "../common/logger.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Component} from "@angular/core";

@Component({
	template : require('./admin.html')
})
export class AdminComponent {
	userType : string;
	userList : User[];

	constructor(private log:LoggerService,
							private adminService : AdminService,
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
		vm.adminService.saveUser(editedUser)
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
		vm.adminService.getUserList()
			.subscribe(
				(result) => vm.userList = result.users,
				(error) => vm.log.error('Error loading users', error, 'Error')
			);
	}
}


import IModalService = angular.ui.bootstrap.IModalService;

import {User} from "../models/User";
import {UserEditorController} from "../dialogs/userEditor/userEditor.controller";
import {IAdminService} from "../core/admin.service";
import {ILoggerService} from "../blocks/logger.service";

export class AdminController {
	userType : string;
	userList : User[];

	static $inject = ['LoggerService', 'AdminService', '$uibModal'];

	constructor(private logger:ILoggerService,
							private adminService : IAdminService,
							private $modal : IModalService) {
		this.userType = 'all';
		this.loadUsers();
	}

	editUser(user:User) {
		var vm = this;
		UserEditorController.open(vm.$modal, user)
			.result.then(function(editedUser : User) {
				vm.adminService.saveUser(editedUser)
					.then(function(response : {uuid : string} ) {
						editedUser.uuid = response.uuid;
						var i = vm.userList.indexOf(user);
						vm.userList[i] = editedUser;
						vm.logger.success('User saved', editedUser, 'Edit user');
					});
		});
	}

	viewUser(user:User) {
		var vm = this;
		UserEditorController.open(vm.$modal, user);
	}

	deleteUser(user:User) {
		this.logger.error('Delete ' + user.username);
	}

	private loadUsers() {
		var vm = this;
		vm.adminService.getUserList()
			.then(function(result) {
					vm.userList = result.users;
			});
	}
}


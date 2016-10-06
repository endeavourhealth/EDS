import {ISecurityService} from "../core/security.service";
import {UserInRole} from "../models/UserInRole";
import {User} from "../models/User";

export class TopnavController {
	currentUser:User;
	selectedRole:UserInRole;

	static $inject = ['SecurityService'];

	constructor(private securityService:ISecurityService) {
		this.getCurrentUser();
	}

	getCurrentUser() {
		var vm:TopnavController = this;
		vm.currentUser = vm.securityService.getCurrentUser();

		//vm.updateRole(vm.currentUser.currentUserInRoleUuid);
	}

	showUserAccountTab() {
		this.securityService.openUserAccountTab();
	}

	updateRole(userInRoleUuid : string) {
		var vm = this;
		var matches = $.grep(vm.currentUser.userInRoles, function (e) {
			return e.userInRoleUuid === userInRoleUuid;
		});
		if (matches.length === 1) {
			vm.securityService.switchUserInRole(userInRoleUuid)
				.then(function(data) {
					vm.currentUser.currentUserInRoleUuid = userInRoleUuid;
					vm.selectedRole = matches[0];
				});
		}
	}
}

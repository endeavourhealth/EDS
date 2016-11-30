import {SecurityService} from "../security/security.service";
import {User} from "../users/models/User";
import {Component} from "@angular/core";

@Component({
	selector: 'topnav-component',
	template: require('./topnav.html')
})
export class TopnavComponent {
	currentUser:User;

	constructor(private securityService:SecurityService) {
		this.getCurrentUser();
	}

	getCurrentUser() {
		this.currentUser = this.securityService.getCurrentUser();
		//vm.updateRole(vm.currentUser.currentUserInRoleUuid);
	}

	updateRole(userInRoleUuid : string) {
		/*var vm = this;
		 var matches = $.grep(vm.currentUser.userInRoles, function (e) {
		 return e.userInRoleUuid === userInRoleUuid;
		 });
		 if (matches.length === 1) {
		 vm.securityService.switchUserInRole(userInRoleUuid)
		 .then(function(data) {
		 vm.currentUser.currentUserInRoleUuid = userInRoleUuid;
		 vm.selectedRole = matches[0];
		 });
		 }*/
	}
}

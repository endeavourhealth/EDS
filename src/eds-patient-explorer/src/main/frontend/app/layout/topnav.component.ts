import {SecurityService} from "../security/security.service";
import {User} from "../security/models/User";
import {Component} from "@angular/core";
import {OrgRole} from "./models/OrgRole";

@Component({
	selector: 'topnav-component',
	template: require('./topnav.html')
})
export class TopnavComponent {
	currentUser:User;
	currentOrgRole : OrgRole;
	userOrganisations : OrgRole[];

	constructor(private securityService:SecurityService) {
		let vm = this;

		vm.currentUser = this.securityService.getCurrentUser();
	}

	navigateUserAccount() {
		window.location.href = "eds-user-manager/#/app/users/userManagerUserView";
	}

	logout() {
		this.securityService.logout();
	};
}

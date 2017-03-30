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
		var url = window.location.protocol + "//" + window.location.host;
		url = url + "/eds-user-manager/#/app/users/userManagerUserView";
		window.location.href = url;
	}

	logout() {
		this.securityService.logout();
	};
}

import {SecurityService} from "../security/security.service";
import {User} from "../security/models/User";
import {Component} from "@angular/core";
import {linq} from "../common/linq";
import {OrganisationGroup} from "../security/models/OrganisationGroup";
import {LayoutService} from "./layout.service";
import {OrgRole} from "./models/OrgRole";

@Component({
	selector: 'topnav-component',
	template: require('./topnav.html')
})
export class TopnavComponent {
	currentUser:User;
	currentOrgRole : OrgRole;
	userOrganisations : OrgRole[];

	constructor(private securityService:SecurityService, private layoutService : LayoutService) {
		let vm = this;
		vm.getCurrentUser();
		}

		getCurrentUser() {
			this.currentUser = this.securityService.getCurrentUser();
		};

		navigateUserAccount() {
			window.location.href = "#/app/users/userManagerUserView";
		};

		logout() {
			this.securityService.logout();
		};
}

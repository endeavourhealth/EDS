import {SecurityService} from "../security/security.service";
import {User} from "../security/models/User";
import {Component} from "@angular/core";
import {linq} from "../common/linq";

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

	getUserOrganisations() {
		return linq(this.currentUser.organisationGroups)
			.Select(g => g.organisationId)
			.Distinct()
			.ToArray();
	}

	switchOrganisation(organisation : string) {
		this.currentUser.organisation = organisation;
	}
}

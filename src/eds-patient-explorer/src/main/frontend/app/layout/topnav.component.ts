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

		vm.currentUser = this.securityService.getCurrentUser();
		vm.layoutService.getServiceName(vm.currentUser.organisation)
			.subscribe(
				(result) => vm.currentOrgRole = new OrgRole(vm.currentUser.organisation, result),
				(error) => vm.currentOrgRole = new OrgRole(null, 'Not selected')
			);
	}

	getUserOrganisations() {
		let vm = this;
		if (!vm.userOrganisations) {

			vm.userOrganisations = [];
			for(let orgGroup of vm.currentUser.organisationGroups) {
				let orgRole = new OrgRole(orgGroup.organisationId, 'Loading...');
				vm.layoutService.getServiceName(orgRole.id)
					.subscribe(
						(result) => {
							if (result != null && result != '') {
								orgRole.name = result;
								vm.userOrganisations.push(orgRole);
							}
						}
					);
			}
		}
		return vm.userOrganisations;
	}

	switchOrganisation(orgRole : OrgRole) {
		this.currentUser.organisation = orgRole.id;
		this.currentOrgRole = orgRole;
	}
}

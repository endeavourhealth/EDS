import {MenuOption} from "./models/MenuOption";
import {SecurityService} from "../security/security.service";
import {LayoutService} from "./layout.service";
import {Component} from "@angular/core";

@Component({
	selector: 'sidebar-component',
	template: require('./sidebar.html')
})

export class SidebarComponent {
	menuOptions:MenuOption[];

	constructor(layoutService:LayoutService, private securityService:SecurityService) {
		this.menuOptions = layoutService.getMenuOptions();
	}

	logout() {
		this.securityService.logout();
	}
}

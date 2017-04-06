// Styling
import '../content/css/index.css';
import '../content/less/index.less';

// Core
import {NgModule} from "@angular/core";
import {Application} from "eds-common-js";

// Modules
import {UserModule} from "./users/user.module";

// State components
import {UserManagerComponent} from "./users/userManager.component";
import {UserEditorDialog} from "./users/userEditor.dialog";
import {RoleManagerComponent} from "./users/roleManager.component";
import {RoleEditorDialog} from "./users/roleEditor.dialog";
import {UserManagerUserViewComponent} from "./users/userManagerUserView.component";
import {UserManagerMenuService} from "./usermanager.menu";

@NgModule(
	Application.Define({
		modules: [
			UserModule
		],
		states: [
			//For v1, only enable the User View and User Editor components - consider settable for superuser
			{name: 'app.userManager', url: '/users/userManager', component: UserManagerComponent},
			{name: 'app.userManagerViewUser', url: '/users/userManagerUserView', component: UserManagerUserViewComponent},
			{name: 'app.userEditor', url: '/users/userEditor', component: UserEditorDialog},
			{name: 'app.roleManager', url: '/users/roleManager', component: RoleManagerComponent},
			{name: 'app.roleEditor', url: '/users/roleEditor', component: RoleEditorDialog},
		],
		defaultState : { state: 'app.userManager', params: {} },
		menuManager : UserManagerMenuService
	})
)
export class AppModule {}

Application.Run(AppModule);
import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {UserManagerComponent} from "./userManager.component";
import {UserService} from "./user.service";
import {UserEditorDialog} from "./userEditor.dialog";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {RoleManagerComponent} from "./roleManager.component";
import {RoleEditorDialog} from "./roleEditor.dialog";
import {UserManagerUserViewComponent} from "./userManagerUserView.component";
import {LoadingIndicatorComponent, LoggerModule} from "eds-common-js";

@NgModule({
	imports:[
		BrowserModule,
		FormsModule,
		NgbModule,
		LoggerModule,
	],
	declarations:[
		UserManagerComponent,
		UserManagerUserViewComponent,
		RoleManagerComponent,
		UserEditorDialog,
		RoleEditorDialog,
		LoadingIndicatorComponent,
	],
	providers:[
		UserService
	]
})
export class UserModule {}
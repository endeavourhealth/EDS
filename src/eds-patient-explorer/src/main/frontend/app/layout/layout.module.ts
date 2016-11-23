import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {UIRouterModule} from "ui-router-ng2";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";

import {ShellComponent} from "./shell.component";
import {SidebarComponent} from "./sidebar.component";
import {TopnavComponent} from "./topnav.component";
import {SecurityModule} from "../security/security.module";
import {LayoutService} from "./layout.service";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		UIRouterModule,
		NgbModule,

		SecurityModule
	],
	declarations : [
		ShellComponent,
		SidebarComponent,
		TopnavComponent,
	],
	providers : [
		LayoutService,
	]
})
export class LayoutModule {}
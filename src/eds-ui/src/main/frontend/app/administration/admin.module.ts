import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";

import {AdminComponent} from "./admin.component";
import {AdminService} from "./admin.service";
// import {UserEditorDialog} from "./userEditor.dialog";

@NgModule({
	imports:[
		BrowserModule,
		FormsModule,
	],
	declarations:[
		// UserEditorDialog,
		AdminComponent,
	],
	providers:[
		AdminService
	]
})
export class AdminModule {}
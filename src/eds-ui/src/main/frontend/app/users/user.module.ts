import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {UserListComponent} from "./userList.component";
import {UserService} from "./user.service";
// import {UserEditorDialog} from "./userEditor.dialog";

@NgModule({
	imports:[
		BrowserModule,
		FormsModule,
	],
	declarations:[
		UserListComponent,
		// UserEditorDialog,
	],
	providers:[
		UserService
	]
})
export class UserModule {}
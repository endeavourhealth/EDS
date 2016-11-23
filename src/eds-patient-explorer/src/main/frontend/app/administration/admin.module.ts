import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";

import {AdminService} from "./admin.service";

@NgModule({
	imports:[
		BrowserModule,
		FormsModule,
	],
	declarations:[
	],
	providers:[
		AdminService
	]
})
export class AdminModule {}
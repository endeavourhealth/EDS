import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {AdminService} from "./admin.service";

@NgModule({
	imports:[
		BrowserModule,
		FormsModule,
	],
	providers:[
		AdminService
	]
})
export class AdminModule {}
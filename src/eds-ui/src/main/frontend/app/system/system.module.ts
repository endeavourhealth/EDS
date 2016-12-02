import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {SystemEditComponent} from "./systemEditor.component";
import {SystemService} from "./system.service";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
	],
	declarations : [
		SystemEditComponent,
	],
	providers : [
		SystemService
	]
})
export class SystemModule {}
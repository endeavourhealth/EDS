import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";

import {SystemEditComponent} from "./systemEditor.component";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
	],
	declarations : [
		SystemEditComponent,
	]
})
export class SystemModule {}
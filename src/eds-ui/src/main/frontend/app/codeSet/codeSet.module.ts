import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {CodeSetEditComponent} from "./codeSetEditor.component";

@NgModule({
	imports:[
		BrowserModule,
		FormsModule
	],
	declarations:[
		CodeSetEditComponent,
	],
})
export class CodeSetModule {}
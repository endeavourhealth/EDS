import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {ProtocolEditComponent} from "./protocolEditor.component";
@NgModule({
	imports : [
		BrowserModule,
		FormsModule
	],
	declarations : [
		ProtocolEditComponent
	]
})
export class ProtocolModule {}
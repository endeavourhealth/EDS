import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";

import {InputBoxDialog} from "./inputBox/inputBox.dialog";
import {MessageBoxDialog} from "./messageBox/messageBox.dialog";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
	],
	declarations : [
		InputBoxDialog,
		MessageBoxDialog,
	],
	entryComponents : [
		InputBoxDialog,
		MessageBoxDialog,
	]
})
export class DialogsModule {}

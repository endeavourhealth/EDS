import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";

import {ExpressionEditDialog} from "./expressionEditor.dialog";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
	],
	declarations : [
		ExpressionEditDialog
	],
	entryComponents : [
		ExpressionEditDialog
	]
})
export class ExpressionsModule {}
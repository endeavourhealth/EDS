import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";

import {FormsModule} from "@angular/forms";
import {TestEditDialog} from "./testEditor.dialog";
import {CodingModule} from "../coding/coding.module";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		NgbModule,

		CodingModule,
	],
	declarations : [
		TestEditDialog
	],
	entryComponents : [
		TestEditDialog
	]
})
export class TestsModule {}
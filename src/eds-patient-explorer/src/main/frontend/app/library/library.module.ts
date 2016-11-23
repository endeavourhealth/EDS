import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {TreeModule} from "angular2-tree-component";

import {LibraryService} from "./library.service";

@NgModule({
	imports:[
		BrowserModule,
		NgbModule,
		TreeModule,
	],
	providers:[
		LibraryService,
	],
})
export class LibraryModule {}
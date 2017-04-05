import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {TreeModule} from "angular2-tree-component";
import {LibraryComponent} from "./library.component";
import {PipesModule} from "../pipes/pipes.module";
import {FolderModule, LibraryService} from "eds-common-js";

@NgModule({
	imports:[
		BrowserModule,
		NgbModule,
		TreeModule,

		PipesModule,
		FolderModule,
	],
	declarations:[
		LibraryComponent,
	],
	providers:[
		LibraryService,
	],
})
export class EdsLibraryModule {}
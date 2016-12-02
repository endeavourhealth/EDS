import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {TreeModule} from "angular2-tree-component";
import {LibraryComponent} from "./library.component";
import {LibraryService} from "./library.service";
import {PipesModule} from "../pipes/pipes.module";
import {FolderModule} from "../folder/folder.module";

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
export class LibraryModule {}
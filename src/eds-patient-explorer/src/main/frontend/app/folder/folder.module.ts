import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {TreeModule} from "angular2-tree-component";
import {FolderComponent} from "./folder.component";
import {FolderService} from "./folder.service";

@NgModule({
	imports:[
		BrowserModule,
		NgbModule,
		TreeModule,
	],
	declarations:[
		FolderComponent,
	],
	providers:[
		FolderService,
	],
	exports:[
		FolderComponent,
	]
})
export class FolderModule {}
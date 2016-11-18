import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {TreeModule} from "angular2-tree-component";

import {LibraryComponent} from "./library.component";
import {LibraryService} from "./library.service";
import {PipesModule} from "../pipes/pipes.module";
import {SystemModule} from "../system/system.module";
import {QueryModule} from "../query/query.module";
import {FolderModule} from "../folder/folder.module";
import {ProtocolModule} from "../protocol/protocol.module";
import {DataSetModule} from "../dataset/dataset.module";
import {CodeSetModule} from "../codeSet/codeSet.module";

@NgModule({
	imports:[
		BrowserModule,
		NgbModule,
		TreeModule,

		PipesModule,

		FolderModule,
		SystemModule,
		QueryModule,
		ProtocolModule,
		DataSetModule,
		CodeSetModule,
	],
	declarations:[
		LibraryComponent,
	],
	providers:[
		LibraryService,
	],
})
export class LibraryModule {}
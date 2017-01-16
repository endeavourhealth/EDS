import {NgModule} from "@angular/core";
import {LibraryService} from "./library.service";

@NgModule({
	providers:[
		LibraryService,
	],
})
export class LibraryModule {}
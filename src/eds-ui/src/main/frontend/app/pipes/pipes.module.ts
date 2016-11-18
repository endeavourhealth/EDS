import {NgModule} from "@angular/core";
import {ItemTypeIdToStringPipe, ItemTypeIdToIconPipe} from "./ItemType";

@NgModule({
	declarations:[
		ItemTypeIdToStringPipe,
		ItemTypeIdToIconPipe,
	],
	exports:[
		ItemTypeIdToStringPipe,
		ItemTypeIdToIconPipe,
	]
})
export class PipesModule {}
import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {StatsComponent} from "./stats.component";
import {StatsService} from "./stats.service";
import {NKDatetimeModule} from "ng2-datetime/ng2-datetime";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		NKDatetimeModule,
	],
	declarations : [
		StatsComponent
	],
	providers : [
		StatsService
	]
})
export class StatsModule {}
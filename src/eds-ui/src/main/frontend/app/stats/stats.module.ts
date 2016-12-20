import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {StatsComponent} from "./stats.component";
import {StatsService} from "./stats.service";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
	],
	declarations : [
		StatsComponent
	],
	providers : [
		StatsService
	]
})
export class StatsModule {}
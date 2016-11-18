import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NKDatetimeModule} from 'ng2-datetime/ng2-datetime';

import {StatsComponent} from "./stats.component";
import {StatsService} from "./stats.service";

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
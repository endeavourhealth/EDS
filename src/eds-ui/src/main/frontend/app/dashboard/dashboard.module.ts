import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {DashboardComponent} from "./dashboard.component";
import {ExchangeComponent} from "./exchange.component";
import {QueueComponent} from "./queue.component";
import {DashboardService} from "./dashboard.service";
import {PipesModule} from "../pipes/pipes.module";

@NgModule({
	imports:[
		BrowserModule,
		NgbModule,

		PipesModule,
	],
	declarations:[
		DashboardComponent,
		ExchangeComponent,
		QueueComponent
	],
	providers:[
		DashboardService,
	]
})
export class DashboardModule {}
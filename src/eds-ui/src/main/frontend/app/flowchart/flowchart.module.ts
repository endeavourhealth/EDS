import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {FlowChartComponent} from "./flowchart.component";
import {MouseCaptureModule} from "../mouseCapture/mouseCapture.module";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		MouseCaptureModule
	],
	declarations : [
		FlowChartComponent,
	],
	exports : [
		FlowChartComponent
	]
})
export class FlowchartModule {}
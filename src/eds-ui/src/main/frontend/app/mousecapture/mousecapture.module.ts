import {NgModule} from "@angular/core";
import {MouseCaptureDirective} from "./mouseCapture.directive";
import {DraggingService} from "./dragging.service";
import {MouseCaptureService} from "./mouseCapture.service";

@NgModule({
	declarations : [
		MouseCaptureDirective,
	],
	providers : [
		DraggingService,
		MouseCaptureService,
	],
	exports : [
		MouseCaptureDirective
	]
})
export class MouseCaptureModule {}


import {NgModule, Renderer} from "@angular/core";
import {MouseCaptureDirective} from "./mousecapture.directive";
import {DraggingService} from "./dragging.service";
import {MouseCaptureService} from "./mousecapture.service";

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


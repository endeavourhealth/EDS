import {Renderer, Directive, ElementRef} from "@angular/core";
import {MouseCaptureService} from "./mousecapture.service";

@Directive({
	selector : '[mousecapture]'
})
export class MouseCaptureDirective {
	constructor ($element : ElementRef, renderer : Renderer, mouseCapture : MouseCaptureService) {
		mouseCapture.registerElement($element, renderer);
	}
}
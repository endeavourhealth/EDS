import {Renderer, Directive, ElementRef} from "@angular/core";
import {MouseCaptureService} from "./mouseCapture.service";

@Directive({
	selector : '[mouseCapture]'
})
export class MouseCaptureDirective {
	constructor ($element : ElementRef, renderer : Renderer, mouseCapture : MouseCaptureService) {
		mouseCapture.registerElement($element, renderer);
	}
}
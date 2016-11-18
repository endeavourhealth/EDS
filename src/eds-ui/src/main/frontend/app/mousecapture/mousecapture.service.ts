import {Injectable, Renderer, ElementRef} from "@angular/core";

@Injectable()
export class MouseCaptureService {
	private id : number;
	private $element : ElementRef;
	private renderer : Renderer;
	private unbindMouseMove;
	private unbindMouseUp;

	constructor() {
		this.id = Math.floor(Math.random() * 100000) + 1;
	}

	mouseMove(evt, config) {
		if (config && config.mouseMove) {
			config.mouseMove(evt);
		}
	}

	mouseUp(evt, config) {
		if (config && config.mouseUp) {
			config.mouseUp(evt);
			config.unbindMouseMove();
			config.unbindMouseUp();
		}
	}

	registerElement(element : ElementRef, renderer : Renderer) {
		this.renderer = renderer;
		this.$element = element;
	}

	acquire(evt, config) {
		var vm = this;

		config.unbindMouseMove = vm.renderer.listen(vm.$element.nativeElement, 'mousemove', (event) => vm.mouseMove(event, config));
		config.unbindMouseUp = vm.renderer.listen(vm.$element.nativeElement, 'mouseup', (event) => vm.mouseUp(event, config));
	}
}

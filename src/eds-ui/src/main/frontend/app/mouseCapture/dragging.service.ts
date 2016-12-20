import {Injectable} from "@angular/core";
import {MouseCaptureService} from "./mouseCapture.service";

@Injectable()
export class DraggingService {
	constructor(private mouseCapture: MouseCaptureService) {}

	public startDrag(evt, config) {
		var threshold : number = 5;
		var dragging = false;
		var x = evt.pageX;
		var y = evt.pageY;

		//
		// Handler for mousemove events while the mouse is 'captured'.
		//
		var mouseMove = function (evt) {

			if (!dragging) {
				if (Math.abs(evt.pageX - x) > threshold ||
					Math.abs(evt.pageY - y) > threshold) {
					dragging = true;

					if (config.dragStarted) {
						config.dragStarted(x, y, evt);
					}

					if (config.dragging) {
						// First 'dragging' call to take into account that we have
						// already moved the mouse by a 'threshold' amount.
						config.dragging(evt.pageX, evt.pageY, evt);
					}
				}
			}
			else {
				if (config.dragging) {
					config.dragging(evt.pageX, evt.pageY, evt);
				}

				x = evt.pageX;
				y = evt.pageY;
			}
		};

		//
		// Handler for mouseup event while the mouse is 'captured'.
		// Mouseup releases the mouse capture.
		//
		var mouseUp = function (evt) {

			if (dragging) {
				if (config.dragEnded) {
					config.dragEnded();
				}
			}
			else {
				if (config.clicked) {
					config.clicked();
				}
			}
		};

		//
		// Acquire the mouse capture and start handling mouse events.
		//
		this.mouseCapture.acquire(evt, {
			mouseMove: mouseMove,
			mouseUp: mouseUp
		});

		evt.stopPropagation();
		evt.preventDefault();
	}
}

import {DraggingService} from "./dragging.service";

angular.module('dragging', ['mouseCapture', ] )
	.factory('dragging', DraggingService);
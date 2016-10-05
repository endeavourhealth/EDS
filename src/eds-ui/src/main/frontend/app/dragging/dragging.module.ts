import DraggingService = app.dragging.DraggingService;

angular.module('dragging', ['mouseCapture', ] )
	.factory('dragging', DraggingService);
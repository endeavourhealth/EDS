import MouseCaptureService = app.mouseCapture.MouseCaptureService;
import MouseCaptureDirective = app.mouseCapture.MouseCaptureDirective;

angular.module('mouseCapture', [])
	.directive('mouseCapture', MouseCaptureDirective.factory())
	.factory('mouseCapture', MouseCaptureService);
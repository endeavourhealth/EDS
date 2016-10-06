import {MouseCaptureDirective} from "./mousecapture.directives";
import {MouseCaptureService} from "./mousecapture.service";

angular.module('mouseCapture', [])
	.directive('mouseCapture', MouseCaptureDirective.factory())
	.factory('mouseCapture', MouseCaptureService);
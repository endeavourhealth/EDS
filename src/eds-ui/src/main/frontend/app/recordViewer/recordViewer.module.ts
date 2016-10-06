import {RecordViewerController} from "./recordViewer.controller";
import {RecordViewerService} from "./recordViewer.service";
import {RecordViewerRoute} from "./recordViewer.route";

angular.module('app.recordViewer', [])
	.controller('RecordViewerController', RecordViewerController)
	.service('RecordViewerService', RecordViewerService)
	.config(RecordViewerRoute);
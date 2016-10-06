import {ServiceEditorController} from "./editor/serviceEditor.controller";
import {ServiceListController} from "./list/serviceListController";
import {ServicePickerController} from "./picker/servicePicker.controller";
import {ServiceService} from "./service/service.service";
import {ServiceRoute} from "./service.route";

angular.module('app.service', [])
	.controller('ServiceEditorController', ServiceEditorController)
	.controller('ServiceListController', ServiceListController)
	.controller('ServicePickerController', ServicePickerController)
	.service('ServiceService', ServiceService)
	.config(ServiceRoute);
import {RouteGroupEditorController} from "./editor/routeGroupEditor.controller";
import {RouteGroupListController} from "./list/routeGroupListController";
import {RouteGroupService} from "./service/routeGroup.service";
import {RouteGroupRoute} from "./routeGroup.route";

angular.module('app.routeGroup', [])
	.controller('RouteGroupEditorController', RouteGroupEditorController)
	.controller('RouteGroupListController', RouteGroupListController)
	.service('RouteGroupService', RouteGroupService)
	.config(RouteGroupRoute);
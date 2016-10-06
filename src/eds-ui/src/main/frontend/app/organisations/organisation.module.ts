/// <reference path="../../typings/index.d.ts" />

import {OrganisationEditorController} from "./editor/organisationEditor.controller";
import {OrganisationListController} from "./list/organisationListController";
import {OrganisationPickerController} from "./picker/organisationPicker.controller";
import {OrganisationService} from "./service/organisation.service";
import {OrganisationRoute} from "./organisation.route";

angular.module('app.organisation', [])
	.controller('OrganisationEditorController', OrganisationEditorController)
	.controller('OrganisationListController', OrganisationListController)
	.controller('OrganisationPickerController', OrganisationPickerController)
	.service('OrganisationService', OrganisationService)
	.config(OrganisationRoute);
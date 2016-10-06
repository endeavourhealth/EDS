import {CodePickerController} from "./codePicker/codePicker.controller";
import {ExpressionEditorController} from "./expressionEditor/expressionEditor.controller";
import {InputBoxController} from "./inputBox/inputBox.controller";
import {MessageBoxController} from "./messageBox/messageBox.controller";
import {OrganisationPickerController} from "./organisationPicker/organisationPicker.controller";
import {PatientFindController} from "./patientFind/patientFind.controller";
import {QueryPickerController} from "./queryPicker/queryPicker.controller";
import {TestEditorController} from "./testEditor/testEditor.controller";
import {UserEditorController} from "./userEditor/userEditor.controller";

angular.module('app.dialogs', [])
	.controller('CodePickerController', CodePickerController)
	.controller('ExpressionEditorController', ExpressionEditorController)
	.controller('InputBoxController', InputBoxController)
	.controller('MessageBoxController', MessageBoxController)
	.controller('OrganisationPickerController', OrganisationPickerController)
	.controller('PatientFindController', PatientFindController)
	.controller('QueryPickerController', QueryPickerController)
	.controller('TestEditorController', TestEditorController)
	.controller('UserEditorController', UserEditorController);
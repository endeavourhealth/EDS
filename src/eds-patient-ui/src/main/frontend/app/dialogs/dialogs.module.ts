import {InputBoxController} from "./inputBox/inputBox.controller";
import {MessageBoxController} from "./messageBox/messageBox.controller";

angular.module('app.dialogs', ['ui.bootstrap'])
	.controller('InputBoxController', InputBoxController)
	.controller('MessageBoxController', MessageBoxController);
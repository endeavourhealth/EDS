import '../../content/css/sidebar.css';

import {ShellController} from "./shell.controller";
import {SidebarController} from "./sidebar.controller";
import {TopnavController} from "./topnav.controller";

angular.module('app.layout', ['ngIdle', 'ui.bootstrap'])
	.controller('ShellController', ShellController)
	.controller('SidebarController', SidebarController)
	.controller('TopnavController', TopnavController);


import '../../content/css/sidebar.css';

import {ShellController, ShellComponent} from "./shell.component";
import {SidebarController, SidebarComponent} from "./sidebar.component";
import {TopnavController, TopnavComponent} from "./topnav.component";

angular.module('app.layout', [])
	.controller('ShellController', ShellController)
	.component('shellComponent',  new ShellComponent())
	.controller('SidebarController', SidebarController)
	.component('sidebarComponent', new SidebarComponent())
	.controller('TopnavController', TopnavController)
	.component('topnavComponent', new TopnavComponent());

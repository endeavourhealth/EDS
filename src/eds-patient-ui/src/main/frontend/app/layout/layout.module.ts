import '../../content/css/sidebar.css';

import {ShellController, ShellComponent} from "./shell.component";
import {SidebarComponent, SidebarController} from "./sidebar.component";
import {TopnavComponent} from "./topnav.component";
import {upgradeAdapter} from "../upgradeAdapter";

angular.module('app.layout', [])
	.controller('ShellController', ShellController)
	.component('shellComponent',  new ShellComponent())

	.controller('SidebarController', SidebarController)
	.component('sidebarComponent', new SidebarComponent())

	.directive('topnavComponent', <angular.IDirectiveFactory>upgradeAdapter.downgradeNg2Component(TopnavComponent));

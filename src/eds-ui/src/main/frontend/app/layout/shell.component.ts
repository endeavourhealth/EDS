import '../../content/css/sidebar.css';

import {Component, ViewContainerRef} from "@angular/core";
import {ToastsManager} from "ng2-toastr";

@Component({
	selector: 'app',
	template: require('./shell.html')
})
export class ShellComponent {
	constructor(public toastr: ToastsManager, vRef: ViewContainerRef) {
		this.toastr.setRootViewContainerRef(vRef);
	}
}
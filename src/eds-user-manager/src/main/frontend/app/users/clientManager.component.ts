import {LoggerService} from "eds-common-js";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Component} from "@angular/core";
import {UserService} from "./user.service";
import {Client} from "./models/Client";

@Component({
	template : require('./clientManager.html')
})
export class ClientManagerComponent {
	clientList : Client[];
	selectedClient : Client;

	constructor(private log:LoggerService,
							private userService : UserService,
							private $modal : NgbModal) {

		this.getRealmClients();
	}

	getClientList() {
		// Perform ordering and filtering here?
		return this.clientList;
	}

	getRealmClients(){
		var vm = this;
		vm.userService.getRealmClients()
            .subscribe(
				(result) => vm.clientList = result,
				(error) => vm.log.error('Error loading clients', error, 'Error')
			);
	}

}


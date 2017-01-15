import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService, Transition} from "ui-router-ng2";
import {Service} from "../services/models/Service";
import {Organisation} from "../organisations/models/Organisation";
import {System} from "../system/models/System";
import {TechnicalInterface} from "../system/models/TechnicalInterface";
/*import {Endpoint} from "./models/Endpoint";*/
import {AdminService} from "../administration/admin.service";
/*import {ServiceService} from "./service.service";*/
import {OrganisationPickerDialog} from "../organisations/organisationPicker.dialog";
import {MessageBoxDialog} from "../dialogs/messageBox/messageBox.dialog";
import {LoggerService} from "../common/logger.service";
import {SystemService} from "../system/system.service";

import {ExchangeAuditTransformErrorSummary} from "./ExchangeAuditTransformErrorSummary";
import {ExchangeAuditTransformErrorDetail} from "./ExchangeAuditTransformErrorDetail";
import {ExchangeAuditService} from "./exchangeAudit.service";
import {Exchange} from "./Exchange";
import {ServiceService} from "../services/service.service";
import {Subscription} from "rxjs/Subscription";


@Component({
	template : require('./exchangeAudit.html')
})
export class ExchangeAuditComponent {

	service: Service;
	exchangesToShow: number;
	exchanges: Exchange[];
	selectedExchange: Exchange;
	busyPostingToExchange: Subscription;


	constructor(private $modal : NgbModal,
				private $window : StateService,
				private log : LoggerService,
				private serviceService : ServiceService,
				private exchangeAuditService : ExchangeAuditService,
				private transition : Transition) {

		this.service = new Service();
		this.exchangesToShow = 100;

		var uuid = transition.params()['serviceUuid'];

		serviceService.get(uuid)
			.subscribe(
				(result) => {
					this.service = result;
					this.refreshExchanges();
				},
				(error) => log.error('Failed to retrieve service', error, 'Refresh Service')
			)

	}


	close() {
		this.$window.go(this.transition.from());
	}

	refreshExchanges() {
		var vm = this;
		var serviceId = vm.service.uuid;

		vm.exchangeAuditService.getExchangeList(serviceId, vm.exchangesToShow).subscribe(
			(result) => {
				vm.exchanges = result;
				if (result.length == 0) {
					vm.log.success('No exchanges found');
				}
			},
			(error) => vm.log.error('Failed to retrieve exchanges', error, 'View Exchanges')
		)
	}



	/*createMissingData() {
		var vm = this;
		vm.exchangeAuditService.createMissingData().subscribe(
			(result) => vm.log.success('Data created', null, 'Create Missing Data'),
			(error) => vm.log.error('Failed to create data', error, 'Create Missing Data')
		)
	}*/

	selectExchange(exchange : Exchange) {

		var vm = this;
		vm.selectedExchange = exchange;
		vm.loadExchangeEventsIfRequired(exchange);
	}

	private loadExchangeEventsIfRequired(exchange: Exchange) {

		//if they're already loaded, just return out
		if (exchange.events) {
			return;
		}

		this.loadExchangeEvents(exchange);
	}

	private loadExchangeEvents(exchange: Exchange) {
		var vm = this;
		vm.exchangeAuditService.getExchangeEvents(exchange.exchangeId).subscribe(
			(result) => {
				exchange.events = result;
			},
			(error) => vm.log.error('Failed to retrieve exchange events', error, 'View Exchanges')
		)
	}

	getSelectedExchangeHeaderKeys(): string[] {

		var vm = this;
		if (!vm.selectedExchange) {
			return null;
		}

		return Object.keys(vm.selectedExchange.headers);
	}

	postToExchange(exchangeName: string) {
		var vm = this;
		var exchangeId = vm.selectedExchange.exchangeId;

		this.busyPostingToExchange = vm.exchangeAuditService.postToExchange(exchangeId, exchangeName).subscribe(
			(result) => {
				vm.log.success('Successfully posted to ' + exchangeName + ' exchange', 'Post to Exchange');

				//re-load the events for the exchange, as we'll have added to them
				this.loadExchangeEvents(vm.selectedExchange);
				this.busyPostingToExchange = null;
			},
			(error) => {
				vm.log.error('Failed to post to ' + exchangeName + ' exchange', error, 'Post to Exchange')

				//clear down to say we're not busy
				this.busyPostingToExchange = null;
			}
		)
	}

}

import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService, Transition} from "ui-router-ng2";
import {Service} from "../services/models/Service";
import {LoggerService} from "eds-common-js";
import {ExchangeAuditService} from "./exchangeAudit.service";
import {Exchange} from "./Exchange";
import {ServiceService} from "../services/service.service";
import {Subscription} from "rxjs/Subscription";
import {TransformErrorDetail} from "./TransformErrorDetail";
import {TransformErrorsDialog} from "./transformErrors.dialog";
import {Protocol} from "./Protocol";

@Component({
	template : require('./exchangeAudit.html')
})
export class ExchangeAuditComponent {

	service: Service;

	//exchange filters
	searchTab: string;
	exchangesToShow: number;
	exchangeIdSearch: string;
	exchangeSearchFrom: Date;
	exchangeSearchTo: Date;

	//results
	exchanges: Exchange[];
	protocols: Protocol[];
	selectedExchange: Exchange;
	busyPostingToExchange: Subscription;
	postAllExchanges: boolean;
	postSpecificProtocol: string;


	constructor(private $modal : NgbModal,
				private $window : StateService,
				private log : LoggerService,
				private serviceService : ServiceService,
				private exchangeAuditService : ExchangeAuditService,
				private transition : Transition) {

		this.service = new Service();
		this.exchangesToShow = 100;
		this.postAllExchanges = false;

		var uuid = transition.params()['serviceUuid'];

		serviceService.get(uuid)
			.subscribe(
				(result) => {
					this.service = result;
					this.refreshExchanges();
					this.refreshProtocols();
				},
				(error) => log.error('Failed to retrieve service', error, 'Refresh Service')
			)

	}


	close() {
		this.$window.go(this.transition.from());
	}

	refreshProtocols() {
		var vm = this;
		var serviceId = vm.service.uuid;

		this.exchangeAuditService.getProtocolsList(serviceId).subscribe(
			(result) => {
				vm.protocols = result;
			},
			(error) => vm.log.error('Failed to retrieve protocols', error, 'Get Protocols')
		);
	}

	refreshExchanges() {
		var vm = this;
		var serviceId = vm.service.uuid;

		//make sure to clear this down, so it's clear we've got new content
		this.selectedExchange = null;

		console.log('tab = ' + this.searchTab);

		if (!this.searchTab
			|| this.searchTab == 'tabDates') {

			if (this.exchangeSearchFrom
				&& this.exchangeSearchTo
				&& this.exchangeSearchTo.getTime() < this.exchangeSearchFrom.getTime()) {
				this.log.error('Search date range is invalid');
				return;
			}

			console.log("Search from " + this.exchangeSearchFrom);
			console.log("Search to " + this.exchangeSearchTo);

			vm.exchangeAuditService.getExchangeList(serviceId, vm.exchangesToShow, this.exchangeSearchFrom, this.exchangeSearchTo).subscribe(
				(result) => {
					vm.exchanges = result;
					if (result.length == 0) {
						vm.log.success('No exchanges found');
					}
				},
				(error) => vm.log.error('Failed to retrieve exchanges', error, 'View Exchanges')
			)

		} else {

			if (!this.exchangeIdSearch) {
				this.log.error('Enter an exchange ID to search');
				return;
			}

			vm.exchangeAuditService.getExchangeById(serviceId, this.exchangeIdSearch).subscribe(
				(result) => {
					vm.exchanges = result;
					if (result.length == 0) {
						vm.log.success('No exchanges found');
					}
				},
				(error) => vm.log.error('Failed to retrieve exchanges', error, 'View Exchanges')
			)
		}

	}


	selectExchange(exchange : Exchange) {

		var vm = this;
		vm.selectedExchange = exchange;
		vm.loadExchangeEventsIfRequired(exchange);
		vm.loadTransformAuditsIfRequired(exchange);
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

	private loadTransformAuditsIfRequired(exchange: Exchange) {

		//if they're already loaded, just return out
		if (exchange.transformAudits) {
			return;
		}

		var serviceId = exchange.headers['SenderServiceUuid'];
		var systemId = exchange.headers['SenderSystemUuid'];
		var exchangeId = exchange.exchangeId;

		//if we don't have either of the UUIDs, we can't load the transform history
		if (!serviceId || !systemId) {
			return;
		}

		var vm = this;
		vm.exchangeAuditService.getTransformErrorDetail(serviceId, systemId, exchangeId, false, false).subscribe(
			(result) => {
				exchange.transformAudits = result;
			},
			(error) => vm.log.error('Failed to retrieve transform audits', error, 'View Exchanges')
		);
	}

	getSelectedExchangeHeaderKeys(): string[] {

		var vm = this;
		if (!vm.selectedExchange) {
			return null;
		}

		return Object.keys(vm.selectedExchange.headers);
	}

	getSelectedExchangeHBodyLines(): string[] {

		if (!this.selectedExchange) {
			//console.log('No selected exchange');
			return null;
		}
		//console.log('Got selected exchange');
		//console.log('Body = ' + this.selectedExchange.bodyLines);

		return this.selectedExchange.bodyLines;
	}

	postToExchange(exchangeName: string) {
		var vm = this;
		var exchangeId = vm.selectedExchange.exchangeId;
		var serviceId = this.service.uuid;

		this.busyPostingToExchange = vm.exchangeAuditService.postToExchange(exchangeId, serviceId, exchangeName, this.postAllExchanges, this.postSpecificProtocol).subscribe(
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

	showTransformErrors(transformAudit: TransformErrorDetail) {

		if (!transformAudit.hadErrors) {
			this.log.success('No errors to view');
			return;
		}

		var serviceId = this.selectedExchange.headers['SenderServiceUuid'];
		var systemId = this.selectedExchange.headers['SenderSystemUuid'];

		var vm = this;
		TransformErrorsDialog.open(vm.$modal, serviceId, systemId, transformAudit);
	}



	tabSelected(tab: string) {
		this.searchTab = tab;
		console.log("Tab: " + tab);

	}

	checkboxChanged() {
		console.log('checkbox changed = ' + this.postAllExchanges);
	}

	copyBodyToClipboard() {
		//join the body lines into a single string
		var lines = this.getSelectedExchangeHBodyLines();
		var joined = lines.join('\r\n');

		//create a text area containing the text and insert into the document
		var txtArea = document.createElement("textarea");
		txtArea.style.position = 'fixed';
		txtArea.value = joined;
		document.body.appendChild(txtArea);
		txtArea.select();

		var vm = this;

		//invoke the copy action on the text area
		try {
			var successful = document.execCommand('copy');
			if (successful) {
				this.log.success('Copied to clipboard');
			} else {
				this.log.error('Failed to copy to clipboard');
			}

		} catch (err) {
			console.log('Failed to copy text to clipboard');
			console.log(err);

			this.log.error('Failed to copy to clipboard', err, 'Copy');
		}

		//remove from the document
		document.body.removeChild(txtArea);
	}

}

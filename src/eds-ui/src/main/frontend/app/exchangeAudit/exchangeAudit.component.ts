import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService, Transition} from "ui-router-ng2";
import {Service} from "../services/models/Service";
import {linq, LoggerService} from "eds-common-js";
import {ExchangeAuditService} from "./exchangeAudit.service";
import {Exchange} from "./Exchange";
import {ServiceService} from "../services/service.service";
import {Subscription} from "rxjs/Subscription";
import {TransformErrorDetail} from "./TransformErrorDetail";
import {TransformErrorsDialog} from "./transformErrors.dialog";
import {Protocol} from "./Protocol";
import {MessageBoxDialog} from "eds-common-js/dist/index";
import {ServiceListComponent} from "../services/serviceList.component";
import {MultiSelecterOption} from "./MultiSelecterOption";

@Component({
	template : require('./exchangeAudit.html')
})
export class ExchangeAuditComponent {

	service: Service;
	systemId: string;
	publisherMode: string;
	cachedTagStr: string;

	//exchange filters
	/*searchMode: string;
	exchangesToShow: number;
	exchangeIdSearch: string;
	exchangeSearchFrom: Date;
	exchangeSearchTo: Date;*/

	//results
	exchanges: Exchange[];
	//protocols: Protocol[];
	subscriberConfigNames: MultiSelecterOption[]; //String[];

	selectedExchange: Exchange;

	//for re-queuing
	busyPostingToExchange: Subscription;
	//postSpecificProtocol: string; //this varies from publisher to publisher, so don't move to the service class
	postSpecificProtocols: MultiSelecterOption[]; //this varies from publisher to publisher, so don't move to the service class
	//moved to the service to stop losing their values
	/*postMode: string;
	postExchange: string;
	postFilterFileTypes: boolean;
	postDeleteErrorState: boolean;
	postFilterFileTypesSelected: string;
	postReason: string;*/

	//for colouring exchanges
	exchangeSizeColours: {}; //cached colours dynamically calculated for each exchange
	minLog: number; //cached for calculating colours




	/*dropdownList = [];
	selectedItems = [];*/
	dropdownSettings = {};

	constructor(private $modal : NgbModal,
				private log : LoggerService,
				private serviceService : ServiceService,
				private exchangeAuditService : ExchangeAuditService,
				private transition : Transition,
				private $state : StateService) {

		this.service = new Service();
		//this.postMode = 'Onwards';

		this.systemId = transition.params()['systemId'];
		var serviceId = transition.params()['serviceId'];

		serviceService.get(serviceId)
			.subscribe(
				(result) => {
					this.service = result;
					this.refreshExchanges(true);
					this.refreshSubscriberConfigNames();
					//this.refreshProtocols();
					this.refreshPublisherMode();
				},
				(error) => log.error('Failed to retrieve service', error, 'Refresh Service')
			)




	}


	ngOnInit(){
		/*this.dropdownList = [
			{"id":1,"itemName":"India"},
			{"id":2,"itemName":"Singapore"},
			{"id":3,"itemName":"Australia"},
			{"id":4,"itemName":"Canada"},
			{"id":5,"itemName":"South Korea"},
			{"id":6,"itemName":"Germany"},
			{"id":7,"itemName":"France"},
			{"id":8,"itemName":"Russia"},
			{"id":9,"itemName":"Italy"},
			{"id":10,"itemName":"Sweden"}
		];
		this.selectedItems = [
			{"id":2,"itemName":"Singapore"},
			{"id":3,"itemName":"Australia"},
			{"id":4,"itemName":"Canada"},
			{"id":5,"itemName":"South Korea"}
		];*/

		var vm = this;
		vm.dropdownSettings = {
			singleSelection: false,
			text: '',
			selectAllText: 'Select All',
			unSelectAllText: 'Clear Selection',
			enableSearchFilter: false
		};

		vm.postSpecificProtocols = [];
		vm.subscriberConfigNames = []; //this will be populated soon after creation

	}

	/*onItemSelect(item:any){
		console.log(item);
		console.log(this.selectedItems);
	}
	OnItemDeSelect(item:any){
		console.log(item);
		console.log(this.selectedItems);
	}
	onSelectAll(items: any){
		console.log(items);
	}
	onDeSelectAll(items: any){
		console.log(items);
	}*/

	close() {
		var vm = this;
		vm.$state.go(vm.transition.from());
	}

	refreshSubscriberConfigNames() {

		var vm = this;
		var serviceId = vm.service.uuid;

		if (vm.exchangeAuditService.postShowAllSubscriberConfigNames) {

			vm.exchangeAuditService.getAllSubscriberConfigNameList().subscribe(
				(result) => {
					vm.populateSubscriberNameList(result);
				},
				(error) => vm.log.error('Failed to retrieve subscriber config names', error, 'Get Subscriber Config Names')
			);

		} else {

			vm.exchangeAuditService.getSubscriberConfigNameList(serviceId).subscribe(
				(result) => {
					vm.populateSubscriberNameList(result);
				},
				(error) => vm.log.error('Failed to retrieve subscriber config names', error, 'Get Subscriber Config Names')
			);
		}
	}

	private populateSubscriberNameList(result: string[]) {
		var vm = this;
		vm.subscriberConfigNames = [];
		for (var i=0; i<result.length; i++) {
			var s = result[i];
			var option = new MultiSelecterOption;
			option.id = s;
			option.itemName = s;
			vm.subscriberConfigNames.push(option);
		}
		//vm.subscriberConfigNames = result;
	}

	/*refreshProtocols() {
		var vm = this;
		var serviceId = vm.service.uuid;

		this.exchangeAuditService.getProtocolsList(serviceId, true).subscribe(
			(result) => {
				vm.protocols = result;
			},
			(error) => vm.log.error('Failed to retrieve protocols', error, 'Get Protocols')
		);
	}*/

	refreshExchanges(autoSelectFirst: boolean) {
		var vm = this;
		var serviceId = vm.service.uuid;

		//make sure to clear this down, so it's clear we've got new content
		vm.selectedExchange = null;

		//console.log('searchMode = ' + this.searchMode);

		if (vm.exchangeAuditService.searchMode == 'Recent') {

			vm.exchangeAuditService.getRecentExchanges(serviceId, vm.systemId, vm.exchangeAuditService.exchangesToShow).subscribe(
				(result) => {
					vm.exchanges = result;
					if (result.length == 0) {
						vm.log.success('No exchanges found');
					} else if (autoSelectFirst) {
						vm.selectFirstExchange();
					}
				},
				(error) => vm.log.error('Failed to retrieve exchanges', error, 'View Exchanges')
			)

		} else if (vm.exchangeAuditService.searchMode == 'DateRange') {

			if (this.exchangeAuditService.exchangeSearchFrom
				&& this.exchangeAuditService.exchangeSearchTo
				&& this.exchangeAuditService.exchangeSearchTo.getTime() < this.exchangeAuditService.exchangeSearchFrom.getTime()) {
				this.log.error('Search date range is invalid');
				return;
			}

			vm.exchangeAuditService.getExchangesByDate(serviceId, vm.systemId, vm.exchangeAuditService.exchangesToShow, vm.exchangeAuditService.exchangeSearchFrom, vm.exchangeAuditService.exchangeSearchTo).subscribe(
				(result) => {
					vm.exchanges = result;
					if (result.length == 0) {
						vm.log.success('No exchanges found');
					} else if (autoSelectFirst) {
						vm.selectFirstExchange();
					}
				},
				(error) => vm.log.error('Failed to retrieve exchanges', error, 'View Exchanges')
			)

		} else if (vm.exchangeAuditService.searchMode == 'FirstError') {

			vm.exchangeAuditService.getExchangesFromFirstError(serviceId, vm.systemId, vm.exchangeAuditService.exchangesToShow).subscribe(
				(result) => {
					vm.exchanges = result;
					if (result.length == 0) {
						vm.log.success('No exchanges found');
					} else if (autoSelectFirst) {
						vm.selectFirstExchange();
					}
				},
				(error) => vm.log.error('Failed to retrieve exchanges', error, 'View Exchanges')
			)

		} else if (vm.exchangeAuditService.searchMode == 'ExchangeId') {

			if (!vm.exchangeAuditService.exchangeIdSearch) {
				this.log.error('Enter an exchange ID to search');
				return;
			}

			vm.exchangeAuditService.getExchangeById(serviceId, vm.systemId, vm.exchangeAuditService.exchangeIdSearch).subscribe(
				(result) => {
					vm.exchanges = result;
					if (result.length == 0) {
						vm.log.success('No exchanges found');
					} else if (autoSelectFirst) {
						vm.selectFirstExchange();
					}

				},
				(error) => vm.log.error('Failed to retrieve exchanges', error, 'View Exchanges')
			)

		} else {
			vm.log.error('Unknown search mode ' + vm.exchangeAuditService.searchMode);
		}
	}

	selectFirstExchange() {
		var vm = this;
		var firstExchange = vm.exchanges[0];
		vm.selectExchange(firstExchange);
	}

	selectExchange(exchange : Exchange) {
		var vm = this;
		vm.selectedExchange = exchange;

		vm.loadTransformAuditsIfRequired(exchange);
	}

	/*private loadExchangeEventsIfRequired(exchange: Exchange) {

		//if they're already loaded, just return out
		if (exchange.events) {
			return;
		}

		this.loadExchangeEvents(exchange);
	}*/

	/*private loadExchangeEvents(exchange: Exchange) {
		var vm = this;
		vm.exchangeAuditService.getExchangeEvents(exchange.exchangeId).subscribe(
			(result) => {
				exchange.events = result;
			},
			(error) => vm.log.error('Failed to retrieve exchange events', error, 'View Exchanges')
		)
	}*/

	private loadTransformAuditsIfRequired(exchange: Exchange) {

		//if they're already loaded, just return out
		if (exchange.transformAudits) {
			return;
		}

		this.loadTransformAudits(exchange);
	}

	private loadTransformAudits(exchange: Exchange) {

		var serviceId = exchange.headers['SenderServiceUuid'];
		var systemId = exchange.headers['SenderSystemUuid'];
		var exchangeId = exchange.exchangeId;

		//if we don't have either of the UUIDs, we can't load the transform history
		if (!serviceId || !systemId) {
			return;
		}

		var vm = this;
		vm.exchangeAuditService.getInboundTransformAudits(serviceId, systemId, exchangeId, true).subscribe(
			(result) => {
				//sort the events/audits in REVERSE date, so the most recent is at the top
				exchange.transformAudits = linq(result).OrderByDescending(s => s.transformStart).ToArray();
				//exchange.transformAudits = result;
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

		return this.selectedExchange.bodyLines;
	}

	postToExchange() {
		var vm = this;
		var exchangeId = vm.selectedExchange.exchangeId;
		var serviceId = this.service.uuid;
		var deleteErrorState = vm.exchangeAuditService.postDeleteErrorState;

		var exchangeName = vm.exchangeAuditService.postExchange;
		if (!exchangeName) {
			vm.log.error('No exchange selected');
			return;
		}

		var mode = vm.exchangeAuditService.postMode;
		if (!mode) {
			vm.log.error('Select which exchanges to post');
			return;
		}

		var specificSubscriberConfigNames = [];
		if (vm.postSpecificProtocols) {
			for (var i=0; i<vm.postSpecificProtocols.length; i++) {
				var item = vm.postSpecificProtocols[i];
				specificSubscriberConfigNames.push(item.id);
			}
		}

		/*if ((mode == 'FullDelete'
				|| mode == 'FullRefresh'
				|| mode == 'QuickRefresh'
				|| mode == 'FullRefreshAdminOnly')
			&& specificSubscriberConfigNames.length == 0) {
				//TODO - confirm full load
			vm.log.error('Select a protocol to refresh/delete for');
			return;
		}*/


		var fileTypesToFilterOn;
		if (vm.exchangeAuditService.postFilterFileTypes) {
			fileTypesToFilterOn = vm.exchangeAuditService.postFilterFileTypesSelected;
			if (!fileTypesToFilterOn) {
				vm.log.error('No file types selected');
				return;
			}
		}

		var reason = vm.exchangeAuditService.postReason;

		this.busyPostingToExchange = vm.exchangeAuditService.postToExchange(exchangeId, serviceId, vm.systemId, exchangeName, mode, specificSubscriberConfigNames, fileTypesToFilterOn, deleteErrorState, reason).subscribe(
			(result) => {

				//it may return nothing (which is good) or a string (which will be an error messag)
				var errorMessage = result as string;
				if (errorMessage) {
					vm.log.warning(errorMessage);

				} else {
					vm.log.success('Successfully posted to ' + exchangeName + ' exchange', 'Post to Exchange');

					//re-load the events for the exchange, as we'll have added to them
					this.loadTransformAudits(vm.selectedExchange);
				}
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

		if (!transformAudit.transformError) {
			this.log.success('No errors to view');
			return;
		}

		var serviceId = this.selectedExchange.headers['SenderServiceUuid'];
		var systemId = this.selectedExchange.headers['SenderSystemUuid'];

		var vm = this;
		TransformErrorsDialog.open(vm.$modal, serviceId, systemId, transformAudit);
	}


	copyBodyToClipboard(asJson: boolean) {
		var vm = this;

		//join the body lines into a single string
		var lines = this.getSelectedExchangeHBodyLines();
		var joined = lines.join('\r\n');

		//if we want it formatted as JSON, then try that
		if (asJson) {
			try {
				//turn into object
				var obj = JSON.parse(joined);

				//pretty-print object
				joined = JSON.stringify(obj, null, 2);

			} catch (e) {
				vm.log.error('Not valid JSON');
				return;
			}
		}

		//create a text area containing the text and insert into the document
		var txtArea = document.createElement("textarea");
		txtArea.style.position = 'fixed';
		txtArea.value = joined;
		document.body.appendChild(txtArea);
		txtArea.select();

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

	canBeQueued(exchange: Exchange): boolean {
		var key = exchange.headers['AllowQueueing'];


		//if header key not present, it CAN be queued
		if (key && key == 'false') {
			return false;
		} else {
			return true;
		}
	}

	formatTransformAuditDuration(transformAudit: TransformErrorDetail) : string {
		if (!transformAudit
			|| !transformAudit.transformStart
			|| !transformAudit.transformEnd) {
			return '';
		}

		var startDate = new Date();
		startDate.setTime(transformAudit.transformStart);

		var endDate = new Date();
		endDate.setTime(transformAudit.transformEnd);

		return ServiceListComponent.getDateDiffDesc(startDate, endDate, 2);
	}

	getCellColour(exchange: Exchange): any {
		var vm = this;

		if (!vm.exchangeSizeColours) {
			vm.exchangeSizeColours = {};
		}

		var colour = vm.exchangeSizeColours[exchange.exchangeId];
		if (!colour) {
			var size = exchange.exchangeSizeBytes;
			colour = vm.generateColourForSize(size);
			//colour = '#FFFFFF';
			vm.exchangeSizeColours[exchange.exchangeId] = colour;
		}
		return {'background-color': colour};
	}

	testCellColour(size: number): any {
		var vm = this;
		var colour = vm.generateColourForSize(size);
		return {'background-color': colour};
	}

	private generateColourForSize(size: number): string {

		var vm = this;

		if (!size
			|| size == 0) {
			return '#FFFFFF';

		} else {

			//scale into MB so tiny extracts don't take up most of the colour range
			size = size / (1024 * 1024);

			//use a logarithmic scale to generate a number between 0 and 10
			var log10 = Math.log10(size);

			//adjust by the smallest possible value so it's all > 0
			if (!vm.minLog) {
				vm.minLog = Math.log10(1 / (1024 * 1024));
			}
			log10 += Math.abs(vm.minLog);

			//if over a max value, just cap so it doesn't wrap around
			var max = 10;
			if (log10 > max) {
				log10 = max;
			}

			//generate a HSB colour (red to blue) from that
			var hue = ((max - log10) / max); //convert value into decimal between 0 and 1
			hue *= 0.66; //convert into decomal between 0 and 0.66 to only span red to blue range of colour wheel
			var saturation = 40 / 100;
			var brightness = 1;

			//convert HSB to RGB
			return vm.HSVtoRGB(hue, saturation, brightness);
		}
	}

	/**
	 * converts HSB colour (all values between 0 and 1) to a RGB HTML string
     */
	private HSVtoRGB(h: number, s: number, v: number): string {
		var r, g, b, i, f, p, q, t;

		i = Math.floor(h * 6);
		f = h * 6 - i;
		p = v * (1 - s);
		q = v * (1 - f * s);
		t = v * (1 - (1 - f) * s);
		switch (i % 6) {
			case 0: r = v, g = t, b = p; break;
			case 1: r = q, g = v, b = p; break;
			case 2: r = p, g = v, b = t; break;
			case 3: r = p, g = q, b = v; break;
			case 4: r = t, g = p, b = v; break;
			case 5: r = v, g = p, b = q; break;
		}

		//scale up to 0-255 range and convert to hex strings
		var rStr = Math.round(r * 255).toString(16);
		var gStr = Math.round(g * 255).toString(16);
		var bStr = Math.round(b * 255).toString(16);

		var ret = '#';
		if (rStr.length == 1) {
			ret += '0';
		}
		ret += rStr;
		if (gStr.length == 1) {
			ret += '0';
		}
		ret += gStr;
		if (bStr.length == 1) {
			ret += '0';
		}
		ret += bStr;

		return ret;
	}

	private refreshPublisherMode():void {
		var vm = this;
		vm.publisherMode = null;

		for (var i=0; i<vm.service.endpoints.length; i++) {
			var endpoint = vm.service.endpoints[i];
			if (endpoint.systemUuid == vm.systemId
				&& endpoint.endpoint.startsWith('Publisher_')) {
				vm.publisherMode = endpoint.endpoint;
			}
		}
	}

	private savePublisherMode() {
		var vm = this;

		for (var i=0; i<vm.service.endpoints.length; i++) {
			var endpoint = vm.service.endpoints[i];
			if (endpoint.systemUuid == vm.systemId) {
				endpoint.endpoint = vm.publisherMode;
			}
		}

		vm.serviceService.validateSave(vm.service)
			.subscribe(
				(result) => {

					//if we have a message then log
					if (result) {
						vm.log.warning(result);
					} else {
						vm.reallySavePublisherMode();
					}
				},
				(error) => {
					vm.log.error('Error validating', error, 'Error');
				}
			);
	}

	private reallySavePublisherMode() {
		var vm = this;

		vm.serviceService.save(vm.service)
			.subscribe(
				(saved) => {
					vm.log.success('Service saved');
				},
				(error) => vm.log.error('Error saving', error, 'Error')
			);
	}

	getTagStr(service: Service) : string {
		var vm = this;

		if (!vm.cachedTagStr) {
			vm.cachedTagStr = vm.serviceService.createTagStr(service);
		}
		return vm.cachedTagStr
	}

	isBulk(exchange: Exchange): boolean {
		var headers = exchange.headers;
		return headers.hasOwnProperty('is-bulk')
			&& headers['is-bulk'];
	}

	editService() {
		var vm = this;

		//first close this view
		vm.$state.go(vm.transition.from());

		//var fromState = vm.transition.from();
		//var fromState = vm.transition.$from;

		//this.$state.go(vm.transition.from());

		//then invoke the new one from the new view

		var serviceUuid = vm.service.uuid;

		setTimeout(()=>{
			ServiceListComponent.editService(serviceUuid, vm.$state);
		}, 2000);

		//ServiceListComponent.editService(serviceUuid, fromState);
		//ServiceListComponent.editService(serviceUuid, vm.$state);


		//vm.$state.go(vm.transition.from());
	}

	showLast(numExchanges: number) {
		var vm = this;
		vm.exchangeAuditService.exchangesToShow = numExchanges;

		vm.refreshExchanges(false);
	}
}

import {Protocol} from "./models/Protocol";
import {ServiceContract} from "./models/ServiceContract";
import {Service} from "../services/models/Service";
import {System} from "../system/models/System";
import {TechnicalInterface} from "../system/models/TechnicalInterface";
import {DataSet} from "../dataSet/models/Dataset";
import {ServiceService} from "../services/service.service";
import {AdminService, LibraryService, linq, LoggerService} from "eds-common-js";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Transition, StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {SystemService} from "../system/system.service";
import {DataSetService} from "../dataSet/dataSet.service";
import {ProtocolService} from "./protocol.service";
import {EdsLibraryItem} from "../edsLibrary/models/EdsLibraryItem";
import {LibraryItem} from "eds-common-js/dist/library/models/LibraryItem";
import {ServicePickerDialog} from "../services/servicePicker.dialog";

@Component({
	template : require('./protocolEditor.html')
})
export class ProtocolEditComponent {
	libraryItem : EdsLibraryItem;
	//protected protocol : Protocol;
	selectedContract : ServiceContract;
	services : Service[];
	systems : System[];
	dataSets : DataSet[];
	protocols : EdsLibraryItem[];
	technicalInterfaces : TechnicalInterface[];
	odsCodeByServiceUuid = {}; //cache to quickly find service ODS code for a UUID
	serviceUuidByOdsCode = {};
	serviceNameByOdsCode = {};

	cohortSelected: string;
	cohortOdsCodes: string[];
	cohortOdsCodesStr: string;

	//hard-code two cohort strings until the cohort editor is implemented
	cohorts: string[];
	//cohorts : Cohort[];

	enabled = ["TRUE", "FALSE"];
	consent = ["OPT-IN", "OPT-OUT"];
	type = ["PUBLISHER", "SUBSCRIBER"];

	constructor(
		protected libraryService : LibraryService,
		protected serviceService : ServiceService,
		protected systemService : SystemService,
		protected dataSetService : DataSetService,
		protected protocolService : ProtocolService,
		protected logger : LoggerService,
		protected $modal : NgbModal,
		protected adminService : AdminService,
		protected state : StateService,
		protected transition : Transition) {

		this.libraryItem = <EdsLibraryItem>{
			protocol: {}
		};

		this.performAction(transition.params()['itemAction'], transition.params()['itemUuid']);

		this.loadServices();
		this.loadSystems();

		this.cohorts = ['All Patients', 'Explicit Patients', 'Defining Services'];
		//this.cohorts = ['All Patients', 'Explicit Patients'];
		//this.loadCohorts(); //hard-coding these for now
		//console.log('Cohorts = ' + this.cohorts);
		//console.log('Cohorts = ' + this.cohorts.length);

		this.loadDatasets();
	}

	protected performAction(action:string, itemUuid:string) {
		switch (action) {
			case 'add':
				this.create(itemUuid);
				break;
			case 'edit':
				this.load(itemUuid);
				break;
		}
	}

	load(uuid : string) {
		var vm = this;
		vm.libraryService.getLibraryItem<EdsLibraryItem>(uuid)
			.subscribe(
				(libraryItem) => {
					vm.libraryItem = libraryItem;
					vm.updateCohortOdsCodeDesc();
				},
				(data) => vm.logger.error('Error loading', data, 'Error')
			);
	}

	save(close : boolean) {
		var vm = this;
		vm.libraryService.saveLibraryItem(vm.libraryItem)
			.subscribe(
				(libraryItem) => {
					vm.libraryItem.uuid = libraryItem.uuid;
					vm.adminService.clearPendingChanges();
					vm.logger.success('Item saved', vm.libraryItem, 'Saved');
					if (close) {
						vm.state.go(vm.transition.from());
					}
				},
				(error) => vm.logger.error('Error saving', error, 'Error')
			);
	}

	close() {
		this.adminService.clearPendingChanges();
		this.state.go(this.transition.from());
	}

	create(folderUuid : string) {
		var vm = this;

		var protocol = {
			enabled: 'TRUE',
			patientConsent: 'OPT-IN',
			cohort: '0',
			dataSet: '0',
			serviceContract: []
		} as Protocol;

		vm.libraryItem = {
			uuid: null,
			name: '',
			description: '',
			folderUuid: folderUuid,
			protocol: protocol
		} as EdsLibraryItem;

		vm.updateCohortOdsCodeDesc();
	}

	addContract() {
		this.selectedContract = {
			type: '',
			service: new Service(),
			system: new System(),
			technicalInterface: new TechnicalInterface(),
			active: 'TRUE'
		} as ServiceContract;

		//ensure the service contract array isn't null, which it will be if we're amending a protocol with no contracts in it
		if (!this.libraryItem.protocol.serviceContract) {
			this.libraryItem.protocol.serviceContract = [];
		}

		this.libraryItem.protocol.serviceContract.push(this.selectedContract);
	}

	removeContract(index: number, scope : any) {
		this.libraryItem.protocol.serviceContract.splice(index, 1);
		if (this.selectedContract === scope.item) {
			this.selectedContract = null;
		}
	}

	setService() {

		//the combo includes the ODS code now, so we can't just carry over the combo display text
		/*var serviceName = $("#service>option:selected").html()
		this.selectedContract.service.name = serviceName;*/

		var vm = this;

		vm.selectedContract.service.name = null;

		var selectedServiceUuid = vm.selectedContract.service.uuid;
		if (selectedServiceUuid) {
			var i;
			for (i=0; i<vm.services.length; i++) {
				var service = vm.services[i];
				if (service.uuid == selectedServiceUuid) {

					this.selectedContract.service.name = service.name;
					break;
				}
			}
		}
	}

	setSystem() {
		var systemName = $("#system>option:selected").html()
		this.selectedContract.system.name = systemName;
	}

	setTechnicalInterface() {
		var technicalInterfaceName = $("#technicalInterface>option:selected").html()
		this.selectedContract.technicalInterface.name = technicalInterfaceName;
	}

	loadServices() {
		var vm = this;
		vm.serviceService.getAll()
			.subscribe(
				(result) => vm.services = linq(result).OrderBy(s => s.name.toLowerCase()).ToArray(),
				(error) => vm.logger.error('Failed to load services', error, 'Load services')
			);
	}

	/*loadCohorts() {
		var vm = this;
		vm.protocolService.getCohorts()
			.subscribe(
				(result) => vm.cohorts = result,
				(error) => vm.logger.error('Failed to load cohorts', error, 'Load cohorts')
			);
	}*/

	loadDatasets() {
		var vm = this;
		vm.dataSetService.getDatasets()
			.subscribe(
				(result) => vm.dataSets = linq(result).OrderBy(ds => ds.name).ToArray(),
				(error) => vm.logger.error('Failed to load dataSets', error, 'Load dataSets')
			);

		/*vm.libraryService.getProtocols("edf5ac83-1491-4631-97ff-5c7a283c73b1")
		 .then(function(result) {
		 vm.protocols = result;
		 })
		 .catch(function (error) {
		 vm.logger.error('Failed to load protocols', error, 'Load protocols');
		 });*/
	}

	loadSystems() {
		var vm = this;
		vm.systemService.getSystems()
			.subscribe(
				(result) => vm.processSystems(result),
				(error) => vm.logger.error('Failed to load systems', error, 'Load systems')
			);
	}

	private processSystems(systems:System[]) {
		var vm = this;
		vm.systems = linq(systems).OrderBy(s => s.name).ToArray();
		vm.technicalInterfaces = [];
		console.log(vm.systems[0].technicalInterface.length);
		console.log(vm.systems[0].technicalInterface[0].name);

		for (var i = 0; i < vm.systems.length; ++i) {
			for (var j = 0; j < vm.systems[i].technicalInterface.length; ++j) {
				var technicalInterface = {
					uuid: vm.systems[i].technicalInterface[j].uuid,
					name: vm.systems[i].technicalInterface[j].name,
					messageType: vm.systems[i].technicalInterface[j].messageType,
					messageFormat: vm.systems[i].technicalInterface[j].messageFormat,
					messageFormatVersion: vm.systems[i].technicalInterface[j].messageFormatVersion
				} as TechnicalInterface;
				vm.technicalInterfaces.push(technicalInterface);
			}
		}
		vm.technicalInterfaces = linq(vm.technicalInterfaces).OrderBy(ti => ti.name).ToArray();
	}

	isCohortDefinedByServices(): boolean {
		/*var p = this.libraryItem.protocol;
		var cohort = p.cohort;
		return cohort == 'Defining Services';*/
		return this.cohortSelected == 'Defining Services';
	}

	getServiceContracts() {


		var vm = this;

		//sort by ODS code now, as that's more consistent than name (given each practice seems to have two names)
		return linq(vm.libraryItem.protocol.serviceContract)
			.OrderBy(sc => sc.type)
			.ThenBy(sc => {
				return vm.getOdsCodeForService(sc.service).toLowerCase();
			})
			.ToArray();

		//awful syntax to handle the service name being undefined when adding new service contrals
		/*return linq(vm.libraryItem.protocol.serviceContract)
			.OrderBy(sc => sc.type)
			.ThenBy(sc => {
				if (sc.service.name) {
					return sc.service.name.toLowerCase();
				} else {
					return '';
				}
			})
			.ToArray();*/

	}

	publisherCount(): number {
		return this.getServiceContractCount("PUBLISHER");
	}

	subscriberCount(): number {
		return this.getServiceContractCount("SUBSCRIBER");
	}

	private getServiceContractCount(typeToCheck: string): number {
		var contracts = this.getServiceContracts();
		var count = 0;

		for (var i=0; i<contracts.length; i++) {
			var contract = contracts[i];
			if (contract.type == typeToCheck) {
				count ++;
			}
		}

		return count;
	}

	searchForService() {
		var vm = this;

		//don't bother passing in the current selection
		var currentSelection = [];

		ServicePickerDialog.open(vm.$modal, currentSelection)
			.result.then(function (result : Service[]) {
				//the dialog allows multiple selection, so just handle it with an error rather than hack at that
				if (result.length > 1) {
					vm.logger.error('Multiple services selected');
				} else {
					var selectedService = result[0];
					vm.selectedContract.service.name = selectedService.name;
					vm.selectedContract.service.uuid = selectedService.uuid;
				}
			}
		);
	}



	getTypeDescShort(type: string): string {
		if (type == 'PUBLISHER') {
			return 'P';
		} else if (type == 'SUBSCRIBER') {
			return 'S';
		} else {
			return type;
		}
	}

	cohortOdsCodesChanged() {
		var vm = this;

		if (vm.cohortSelected.startsWith('Defining Services')) {
			vm.libraryItem.protocol.cohort = vm.cohortSelected + ':' + vm.cohortOdsCodesStr;
			vm.cohortOdsCodes = vm.cohortOdsCodesStr.split(/\r|\n|,| |;/);

		} else {
			vm.libraryItem.protocol.cohort = vm.cohortSelected;
		}
	}

	updateCohortOdsCodeDesc() {
		var vm = this;

		var cohort = vm.libraryItem.protocol.cohort;
		if (cohort.startsWith('Defining Services')) {

			var index = cohort.indexOf(':');
			vm.cohortSelected = cohort.substring(0, index);
			vm.cohortOdsCodesStr = cohort.substring(index+1);
			//no idea why the regex only works when separated with slashes rather than quotes
			vm.cohortOdsCodes = vm.cohortOdsCodesStr.split(/\r|\n|,| |;/);
			//vm.cohortOdsCodes = vm.cohortOdsCodesStr.split('\r|\n|,| |;');

		} else {
			vm.cohortSelected = cohort;
			vm.cohortOdsCodesStr = '';
			vm.cohortOdsCodes = [];
		}
	}

	isServicePartOfCohort(serviceContract:ServiceContract): boolean {

		if (serviceContract.type != 'PUBLISHER') {
			return null;
		}

		var vm = this;
		var odsCode = vm.getOdsCodeForService(serviceContract.service);

		return vm.cohortOdsCodes.indexOf(odsCode) > -1;
	}

	getOrgNameForOdsCode(odsCode:string) : string {

		if (odsCode == '' || !odsCode) {
			return '';
		}

		var vm = this;

		//don't bother doing anything until our services list has been retrieved
		if (!vm.services) {
			return '';
		}

		//console.log('looking for ' + odsCode);

		var serviceName = vm.serviceNameByOdsCode[odsCode];
		if (!serviceName) {
			var i;
			for (i=0; i<vm.services.length; i++) {
				var s = vm.services[i];
				if (s.localIdentifier == odsCode) {
					serviceName = s.name;
					vm.serviceNameByOdsCode[odsCode] = serviceName;
				}
			}
		}

		//if no match from looking at our Service records, then try ODS
		if (!serviceName) {
			serviceName = 'checking...';
			vm.serviceNameByOdsCode[odsCode] = 'checking...';

			vm.serviceService.getOpenOdsRecord(odsCode).subscribe(
				(result) => {
					//console.log('got result');
					//console.log(result);

					if (!result) {
						vm.serviceNameByOdsCode[odsCode] = 'no ODS match';
					} else {
						vm.serviceNameByOdsCode[odsCode] = result['organisationName'] + ' (from ODS)';
					}

				},
				(error) => {
					vm.serviceNameByOdsCode[odsCode] = 'no ODS match';
				}
			);
		}

		return serviceName;
	}

	isOrgPublisher(odsCode:string) : boolean {

		var vm = this;
		var serviceId = vm.serviceUuidByOdsCode[odsCode];
		if (!serviceId) {
			if (vm.services) {
				var i;
				for (i=0; i<vm.services.length; i++) {
					var s = vm.services[i];
					if (s.localIdentifier == odsCode) {
						serviceId = s.uuid;
						vm.serviceUuidByOdsCode[odsCode] = serviceId;
					}
				}
			}
		}

		if (!serviceId) {
			return false;
		}

		var contracts = this.getServiceContracts();
		var i;
		for (i = 0; i < contracts.length; i++) {
			var contract = contracts[i];
			if (contract.type == 'PUBLISHER'
				&& contract.service.uuid == serviceId) {

				return true;
			}
		}

		return false;
	}

	/**
	 * looks up a local ID (i.e. ODS Code) for a service
	 */
	getOdsCodeForService(service: Service): string {
		var uuid = service.uuid;

		var vm = this;
		var ret = vm.odsCodeByServiceUuid[uuid];
		if (!ret) {
			if (vm.services) {
				var i;
				for (i=0; i<vm.services.length; i++) {
					var s = vm.services[i];
					if (s.uuid == uuid) {
						ret = s.localIdentifier;
						vm.odsCodeByServiceUuid[uuid] = ret;
					}
				}
			}
		}
		//always return something non-null so the sorting fn doesn't need to handle undefined/null
		if (!ret) {
			ret = '';
		}
		return ret;
	}


}

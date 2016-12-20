import {Protocol} from "./models/Protocol";
import {ServiceContract} from "./models/ServiceContract";
import {Service} from "../services/models/Service";
import {System} from "../system/models/System";
import {Cohort} from "./models/Cohort";
import {LibraryItem} from "../library/models/LibraryItem";
import {TechnicalInterface} from "../system/models/TechnicalInterface";
import {LibraryService} from "../library/library.service";
import {DataSet} from "../dataSet/models/Dataset";
import {ServiceService} from "../services/service.service";
import {LoggerService} from "../common/logger.service";
import {AdminService} from "../administration/admin.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Transition, StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {SystemService} from "../system/system.service";
import {DataSetService} from "../dataSet/dataSet.service";
import {ProtocolService} from "./protocol.service";

@Component({
	template : require('./protocolEditor.html')
})
export class ProtocolEditComponent {
	libraryItem : LibraryItem;
	protected protocol : Protocol;
	selectedContract : ServiceContract;
	services : Service[];
	systems : System[];
	cohorts : Cohort[];
	dataSets : DataSet[];
	protocols : LibraryItem[];
	technicalInterfaces : TechnicalInterface[];

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

		this.libraryItem = <LibraryItem>{
			protocol: {}
		};

		this.performAction(transition.params()['itemAction'], transition.params()['itemUuid']);

		this.loadServices();
		this.loadSystems();
		this.loadCohorts();
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
		vm.libraryService.getLibraryItem(uuid)
			.subscribe(
				(libraryItem) => vm.libraryItem = libraryItem,
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
		this.protocol = {
			enabled: 'TRUE',
			patientConsent: 'OPT-IN',
			cohort: '0',
			dataSet: '0',
			serviceContract: []
		} as Protocol;

		this.libraryItem = {
			uuid: null,
			name: '',
			description: '',
			folderUuid: folderUuid,
			protocol: this.protocol
		} as LibraryItem;

	}

	addContract() {
		this.selectedContract = {
			type: '',
			service: null,
			system: null,
			technicalInterface: null,
			active: 'TRUE'
		} as ServiceContract;

		this.libraryItem.protocol.serviceContract.push(this.selectedContract);
	}

	removeContract(scope : any) {
		this.libraryItem.protocol.serviceContract.splice(scope.$index, 1);
		if (this.selectedContract === scope.item) {
			this.selectedContract = null;
		}
	}

	setService() {
		var serviceName = $("#service>option:selected").html()
		this.selectedContract.service.name = serviceName;
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
				(result) => vm.services = result,
				(error) => vm.logger.error('Failed to load services', error, 'Load services')
			);
	}

	loadCohorts() {
		var vm = this;
		vm.protocolService.getCohorts()
			.subscribe(
				(result) => vm.cohorts = result,
				(error) => vm.logger.error('Failed to load cohorts', error, 'Load cohorts')
			);
	}

	loadDatasets() {
		var vm = this;
		vm.dataSetService.getDatasets()
			.subscribe(
				(result) => vm.dataSets = result,
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

	private processSystems(systems) {
		var vm = this;
		vm.systems = systems;
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
	}
}

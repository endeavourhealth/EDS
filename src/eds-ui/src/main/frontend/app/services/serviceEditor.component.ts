import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService, Transition} from "ui-router-ng2";
import {Service} from "./models/Service";
import {System} from "../system/models/System";
import {TechnicalInterface} from "../system/models/TechnicalInterface";
import {Endpoint} from "./models/Endpoint";
import {ServiceService} from "./service.service";
import {LoggerService, MessageBoxDialog} from "eds-common-js";
import {SystemService} from "../system/system.service";
import {EdsLibraryItem} from "../edsLibrary/models/EdsLibraryItem";
import {OdsSearchDialog} from "./odsSearch.dialog";
import {OrganisationType} from "./models/OrganisationType";
import {linq} from "eds-common-js/dist/index";
import {Tag} from "./models/Tag";
import {SubscriberHistory} from "./models/SubscriberHistory";
import {DpaHistory} from "./models/DpaHistory";
import {DateTimeFormatter} from "../utility/DateTimeFormatter";

@Component({
	template : require('./serviceEditor.html')
})
export class ServiceEditComponent {

	//SD-338 - need to import the static formatting functions so they can be used by the HTML template
	formatYYYYMMDDHHMM = DateTimeFormatter.formatYYYYMMDDHHMM;

	service : Service = <Service>{};
	systems : System[];
	technicalInterfaces : TechnicalInterface[];
	//protocols: EdsLibraryItem[];
	selectedEndpoint : Endpoint;
	tags: Tag[];
	dsmDetailsJson: string;
	currentHasDpa: boolean;
	currentSubscribers: string[];
	historyHasDpa: DpaHistory[];
	historySubscribers: SubscriberHistory[];

	//for populating the org type combo
	organisationTypes: OrganisationType[];
	comboSelectedTagName: string;

	constructor(private $modal : NgbModal,
							private $state : StateService,
							private log : LoggerService,
							private serviceService : ServiceService,
							private systemService : SystemService,
							private transition : Transition) {

		var vm = this;
		vm.loadOrganisationTypes();
		vm.loadSystems();

		var action = transition.params()['itemAction'];
		var serviceId = transition.params()['itemUuid'];
		vm.performAction(action, serviceId);
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

	create(uuid : string) {
		var vm = this;
		vm.service = {
			uuid : uuid,
			name : '',
			endpoints : [],
			tags: {}
		} as Service;

		vm.tags = [];
	}

	load(uuid : string) {
		var vm = this;
		vm.serviceService.get(uuid)
			.subscribe(
				(result) => {
					vm.service = result;
					//vm.getServiceProtocols();
					vm.populateTags();
					vm.populateDsmDetails();
					vm.populateDpaHistory();
					vm.populateSubscribersHistory();
				},
				(error) => vm.log.error('Error loading', error, 'Error')
			);
	}

	save(close : boolean) {
		var vm = this;

		vm.saveTags();

		//doesn't immediately save - calls validation function first which checks the save is safe
		vm.serviceService.validateSave(vm.service)
			.subscribe(
				(result) => {

					//if we have a message then log
					if (result) {
						vm.log.warning(result);
					} else {
						vm.reallySave(close);
					}
				},
				(error) => {
					vm.log.error('Error validating', error, 'Error');
				}
			);
	}

	private reallySave(close: boolean) {
		var vm = this;

		vm.serviceService.save(vm.service)
			.subscribe(
				(saved) => {
					vm.service.uuid = saved.uuid;
					vm.log.success('Item saved', vm.service, 'Saved');
					if (close) {
						vm.$state.go(vm.transition.from());
					}
				},
				(error) => vm.log.error('Error saving', error, 'Error')
			);
	}

	close() {
		var vm = this;
		console.log('Closing service editor');
		console.log('Transition = ' + vm.transition);

		vm.$state.go(vm.transition.from());
	}

	private addEndpoint() {
		var newEndpoint = {} as Endpoint;
		newEndpoint.endpoint = 'Publisher_Bulk';
		this.service.endpoints.push(newEndpoint);
		this.selectedEndpoint = newEndpoint;
	}

	removeEndpoint(index: number, scope : any) {
		this.service.endpoints.splice(index, 1);
		if (this.selectedEndpoint === scope.item) {
			this.selectedEndpoint = null;
		}
	}


	private getSystem(systemUuid : string) : System {
		if (!systemUuid || !this.systems)
			return null;

		var sys : System[] = $.grep(this.systems, function(s : System) { return s.uuid === systemUuid;});

		if (sys.length > 0)
			return sys[0];
		else
			return null;
	}

	/*private getTechnicalInterface(technicalInterfaceUuid : string) : TechnicalInterface {
		if (!technicalInterfaceUuid || !this.technicalInterfaces)
			return null;

		var ti : TechnicalInterface[] = $.grep(this.technicalInterfaces, function(ti : TechnicalInterface) { return ti.uuid === technicalInterfaceUuid;});

		if (ti.length > 0)
			return ti[0];
		else
			return null;
	}*/

	/*private getInterfaceTypeLetter(o: Endpoint): string {
		var vm = this;

		if (!o) {
			return null;
		} else if (vm.isPublisher(o)) {
			return 'Publisher';
		} else {
			return 'Subscriber';
		}
	}

	private isPublisher(o: Endpoint): boolean {
		return o.endpoint.startsWith('Publisher_');
	}*/

	private loadOrganisationTypes() {
		var vm = this;

		//if already done, return
		if (vm.organisationTypes) {
			return;
		}

		var vm = this;
		vm.serviceService.getOrganisationTypeList()
			.subscribe(
				(result) => {
					vm.organisationTypes = result;
				},
				(error) => {
					vm.log.error('Failed to retrieve organisation type list');
				}
			);
	}

	loadSystems() {
		var vm = this;
		vm.systemService.getSystems()
			.subscribe(
				(result) => {
					//vm.systems = result;
					vm.systems = linq(result).OrderBy(s => s.name.toLowerCase()).ToArray();

					vm.technicalInterfaces = [];
					/*console.log(vm.systems[0].technicalInterface.length);
					console.log(vm.systems[0].technicalInterface[0].name);*/

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
				},
				(error) => {
				vm.log.error('Failed to load systems', error, 'Load systems');
				MessageBoxDialog.open(vm.$modal, 'Load systems', 'Failed to load Systems.  Ensure Systems are configured in the protocol manager', 'OK', null);
			});
	}

	deleteService() {
		var vm = this;
		MessageBoxDialog.open(vm.$modal, 'Delete Service', 'Are you sure you want to delete the Service?', 'Yes', 'No')
			.result.then(
			() => vm.doDeleteService(vm.service),
			() => vm.log.info('Delete cancelled')
		);
	}

	private doDeleteService(item : Service) {
		var vm = this;
		vm.serviceService.delete(item.uuid)
			.subscribe(
				(result) => {
					console.log('result = ' + result);
					if (result) {
						//if the delete fn returns a string, then it's a validation error
						vm.log.error(result);

					} else {
						vm.log.success('Service deleted', item, 'Delete Service');
						vm.close();
					}
				},
				(error) => {
					console.log('err = ' + error);
					vm.log.error('Failed to delete Service', error, 'Delete Service');
				}
			);
	}


	deleteData() {
		var vm = this;

		MessageBoxDialog.open(vm.$modal, 'Delete Data', 'Are you sure you want to delete all data for this Service?', 'Yes', 'No')
			.result.then(
			() => vm.doDeleteData(vm.service),
			() => vm.log.info('Delete data cancelled')
		);

	}


	private doDeleteData(service: Service) {
		var vm = this;
		vm.serviceService.deleteData(service.uuid)
			.subscribe(
				() => {
					vm.log.success('Data deletion queued up', service, 'Delete Data');
					vm.close();
				},
				(error) => vm.log.error('Failed to delete data', error, 'Delete Data')
			);
	}

	/*private getServiceProtocols() {
		var vm = this;
		vm.serviceService.getServiceProtocols(vm.service.uuid)
			.subscribe(
				(result) => {
					vm.protocols = result;
					//vm.protocolJson = JSON.stringify(result, null, 2);
				},
				(error) => vm.log.error('Failed to load service protocols', error, 'Load service protocols')
			);
	}*/

	odsSearch() {
		var vm = this;
		OdsSearchDialog.open(vm.$modal);
	}

	autoFillDetailsFromOds() {
		var vm = this;
		var odsCode = vm.service.localIdentifier;
		if (!odsCode) {
			vm.log.error('No ODS code entered');
			return;
		}

		vm.serviceService.getOpenOdsRecord(vm.service.localIdentifier)
			.subscribe(
				(result) => {

					//validate we got something
					if (!result['organisationName']) {
						vm.log.error('No ODS record found for ' + odsCode);
						return;
					}

					var newName = result['organisationName'];
					if (newName) {
						vm.service.name = newName;
					} else {
						vm.service.name = '';
					}

					var newPostcode = result['postcode'];
					if (newPostcode) {
						vm.service.postcode = newPostcode;
					} else {
						vm.service.postcode = '';
					}

					//ccg
					var newParents = result['parents'];
					if (newParents) {
						var parentKeys = Object.keys(newParents);

						vm.service.ccgCode = '';

						if (parentKeys.length == 1) {
							var parentKey = parentKeys[0];
							vm.service.ccgCode = parentKey;

						} else {

							for (var i = 0; i < parentKeys.length; i++) {
								var parentKey = parentKeys[i];
								var parentVal = newParents[parentKey] as string;
								parentVal = parentVal.toUpperCase();

								//some orgs have multiple parents which seems to work OK if we ignore the old
								//SHA parents and GENOMIC ones
								if (parentVal.indexOf('STRATEGIC HEALTH AUTHORITY') == -1
									&& parentVal.indexOf('GENOMIC') == -1) {

									vm.service.ccgCode = parentKey;
								}
							}
						}
					}

					//org type
					var orgTypeName = result['organisationType'];
					if (orgTypeName) {

						vm.service.organisationTypeCode = '';
						vm.service.organisationTypeDesc = '';

						for (var i = 0; i < vm.organisationTypes.length; i++) {
							var organisationType = vm.organisationTypes[i];
							var name = organisationType.name;

							//the org type in the ODS results is the NAME of the OrganisationType enum, not the code or desc
							if (name == orgTypeName) {
								vm.service.organisationTypeCode = organisationType.code;
								vm.service.organisationTypeDesc = organisationType.description;
								break;
							}
						}

					}

					vm.populateDsmDetails();
				},
				(error) => {
					vm.log.error('Failed to find ODS record');
				}
			);
	}

	addNewConfigName() {
		var vm = this;

		var newConfigName = prompt('Config name');
		if (newConfigName == null) {
			return;
		}

		//validate not already present in cache
		for (var i=0; i<vm.serviceService.publisherConfigNameCache.length; i++) {
			var configName = vm.serviceService.publisherConfigNameCache[i];
			if (configName.toLowerCase() == newConfigName.toLowerCase()) {
				vm.log.warning('Config name already used');
				return;
			}
		}

		//add to cache
		var list = vm.serviceService.publisherConfigNameCache;
		list.push(newConfigName); //just stick it on the end for now

		//set on service
		vm.service.publisherConfigName = newConfigName;
	}

	addNewTag() {
		var vm = this;

		var newTag = prompt('Tag name');
		if (newTag == null) {
			return;
		}

		//validate not already present in cache
		for (var i=0; i<vm.serviceService.tagNameCache.length; i++) {
			var tagName = vm.serviceService.tagNameCache[i];
			if (tagName.toLowerCase() == newTag.toLowerCase()) {
				vm.log.warning('Tag name already used');
				return;
			}
		}

		//add
		var list = vm.serviceService.tagNameCache;
		list.push(newTag); //just stick it on the end for now

		vm.comboSelectedTagName = newTag;
		vm.addTag();
	}

	addTag() {
		var vm = this;

		//validate something in the combo is selected
		if (!vm.comboSelectedTagName) {
			vm.log.warning('No tag selected');
			return;
		}

		//validate the tag isn't already added
		for (var i=0; i<vm.tags.length; i++) {
			var tag = vm.tags[i];
			if (tag.name == vm.comboSelectedTagName) {
				vm.log.warning('Tag already added');
				return;
			}
		}

		//add to the service
		var newTag = {} as Tag;
		newTag.name = vm.comboSelectedTagName;
		newTag.value = '';
		vm.tags.push(newTag);

		vm.tags = linq(vm.tags).OrderBy(s => s.name.toLowerCase()).ToArray();
	}

	getTagValue(tagName: string): string {
		var vm = this;
		return vm.service.tags[tagName];
	}

	/**
	 * finds list of all tag names in use
	 */
	/*getTagNames() {
		var vm = this;

		var list = [];

		//get from service (and make sure non-null)
		if (!vm.service.tags) {
			vm.service.tags = {};
		}
		var keys = Object.keys(vm.service.tags);
		for (var i=0; i<keys.length; i++) {
			var key = keys[i];
			var tagName = vm.service.tags[key];
			list.push(tagName);
			console.log('found key ' + tagName);
		}

		//get from all other services
		if (vm.serviceService.tagNameCache) {
			for (var j=0; j<vm.serviceService.tagNameCache.length; j++) {
				var otherTagName = vm.serviceService.tagNameCache[j];
				if (list.indexOf(otherTagName) == -1) {
					list.push(otherTagName);
				}
			}
			console.log('adding extra ' + otherTagName);
		}


		vm.tagNames = list;
		vm.sortTagNames();
	}

	sortTagNames() {
		var vm = this;
		vm.tagNames = linq(vm.tagNames).OrderBy(s => s.toLowerCase()).ToArray();
	}


	tidyTags() {
		var vm = this;

		for (var i=0; i<vm.tagNames.length; i++) {
			var tagName = vm.tagNames[i];
			var tagValue = vm.service.tags[tagName];

			//if the value is empty, properly delete it from the object
			console.log('tag [' + tagName + '] val [' + tagValue + ']');
			if (!tagValue) {
				console.log('delete tag ' + tagName);
				delete vm.service.tags[tagValue];
			}
		}
		console.log('saving tags ' + JSON.stringify(vm.service.tags));

		//make sure our service cache of tag names is updated with the lastest list of known tags
		vm.serviceService.tagNameCache = vm.tagNames;
	}*/

	getTagNames(): string[] {
		var vm = this;
		if (!vm.service
			|| !vm.service.tags) {
			return [];
		}
		//console.log('getting tags for ' + JSON.stringify(vm.service));
		var ret = Object.keys(vm.service.tags);

		/*for (var i=0; i<ret.length; i++) {
			var tagName = ret[i];
			console.log('tag ' + tagName + ' has val = ' + vm.service.tags[tagName])
		}*/

		return ret;
	}

	removeTag(tag: Tag) {
		var vm = this;
		var index = vm.tags.indexOf(tag);
		vm.tags.splice(index, 1);
		//delete vm.service.tags[tagName];
	}

	populateTags() {
		var vm = this;
		vm.tags = [];

		var tagNames = Object.keys(vm.service.tags);
		for (var i=0; i<tagNames.length; i++) {
			var tagName = tagNames[i];
			var tagValue = vm.service.tags[tagName];

			var tag = {} as Tag;
			tag.name = tagName;
			tag.value = tagValue;

			vm.tags.push(tag);
		}
	}

	saveTags() {
		var vm = this;
		var newObj = {};
		//console.log('saving tags');
		for (var i=0; i<vm.tags.length; i++) {
			var tag = vm.tags[i];
			//console.log('tag ' + tag.name + ' val ' + tag.value)
			newObj[tag.name] = tag.value;
		}

		vm.service.tags = newObj;
		//console.log('sevice now ' + JSON.stringify(vm.service));
	}

	populateDsmDetails() {
		var vm = this;
		var odsCode = vm.service.localIdentifier;
		vm.serviceService.getDsmDetails(odsCode).subscribe(
			(result) => {

				if (result) {
					vm.dsmDetailsJson = JSON.stringify(result, null, 2);

					//pull out the interesting stuff
					vm.currentHasDpa = result['hasDPA'];

					vm.currentSubscribers = [];

					var projectsArr = result['distributionProjects'];
					if (projectsArr) {
						for (var i = 0; i < projectsArr.length; i++) {
							var project = projectsArr[i];
							var configName = project['configName'];
							if (configName) {
								vm.currentSubscribers.push(configName);
							}
						}

						vm.currentSubscribers.sort();
					}

				} else {
					vm.dsmDetailsJson = 'no DSM details found';
				}
			},
			(error) => {
				vm.log.error('Error getting DSM data');
			}
		);
	}

	populateDpaHistory() {
		var vm = this;
		vm.serviceService.getDpaHistory(vm.service.uuid).subscribe(
			(result) => {
				vm.historyHasDpa = result.reverse();
			},
			(error) => {
				vm.log.error('Error getting DPA history');
			}
		);
	}

	populateSubscribersHistory() {
		var vm = this;
		vm.serviceService.getSubscriberHistory(vm.service.uuid).subscribe(
			(result) => {
				vm.historySubscribers = result.reverse();
			},
			(error) => {
				vm.log.error('Error getting subscriber history');
			}
		);

	}

	joinArr(arr: string[]): string {
		if (arr) {
			return arr.join(', ');
		} else {
			return '' + arr;
		}
		/*var s = '';
		for (var i=0; i<arr.length; i++) {
			if (i > 0) {
				s += ', ';
			}
			s += arr[i];
		}
		return s;*/
	}

	/**
	 * when a system is selected from the combo, automatically set the technical interface UUID too
	 * we only have one technical interface per system, so this can be done automatically
	 */
	systemSelected() {
		var vm = this;

		var systemUuid = vm.selectedEndpoint.systemUuid;
		if (systemUuid) {
			var system = vm.getSystem(systemUuid);
			var technicalInterfaces = system.technicalInterface;
			var technicalInterface = technicalInterfaces[0];
			vm.selectedEndpoint.technicalInterfaceUuid = technicalInterface.uuid;

		} else {
			vm.selectedEndpoint.technicalInterfaceUuid = null;

		}
	}
}

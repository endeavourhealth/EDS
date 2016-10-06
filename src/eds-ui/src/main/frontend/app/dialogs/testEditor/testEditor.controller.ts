import IModalService = angular.ui.bootstrap.IModalService;
import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
import IModalSettings = angular.ui.bootstrap.IModalSettings;

import {BaseDialogController} from "../baseDialog.controller";
import {CodeSetValue} from "../../models/CodeSetValue";
import {Test} from "../../models/Test";
import {ICodingService} from "../../core/coding.service";
import {ILoggerService} from "../../blocks/logger.service";
import {IsAny} from "../../models/IsAny";
import {Resource} from "../../models/Resource";
import {CodePickerController} from "../codePicker/codePicker.controller";
import {CodeSet} from "../../models/CodeSet";
import {FieldTest} from "../../models/FieldTest";
import {ValueFrom} from "../../models/ValueFrom";
import {ValueTo} from "../../models/ValueTo";
import {ValueSet} from "../../models/ValueSet";
import {Restriction} from "../../models/Restriction";
import {Concept} from "../../models/Concept";

export class TestEditorController extends BaseDialogController {
	title : string;
	resourceOnly : boolean = false;
	viewFieldTest : boolean = false;
	codeEditor : boolean = false;
	dateEditor : boolean = false;
	dobEditor : boolean = false;
	valueEditor : boolean = false;
	valueSetEditor : boolean = false;
	sexEditor : boolean = false;
	valueField : string;
	valueSetField : string;
	dateLabel : string;
	dobLabel : string;
	sexLabel : string;
	fieldTestCodeEditor : boolean = false;
	fieldTestDateEditor : boolean = false;
	fieldTestDobEditor : boolean = false;
	fieldTestValueEditor : boolean = false;
	fieldTestValueSetEditor : boolean = false;
	fieldTestSexEditor : boolean = false;
	fieldTestValueField : string;
	fieldTestValueSetField : string;
	fieldTestDateLabel : string;
	fieldTestDobLabel : string;
	fieldTestSexLabel : string;
	addRestriction : boolean = false;
	showRestriction : boolean = false;
	addFilter : boolean = false;
	ruleDatasource : string;
	filterDateFrom : Date;
	filterDateTo : Date;
	filterDateFromRelativeValue : String;
	filterDateToRelativeValue : String;
	filterDateFromRelativePeriod : String;
	filterDateToRelativePeriod : String;
	datetype: String;
	dobdatetype: String;
	fieldTestDatetype: String;
	fieldTestDobdatetype: String;
	filterDOBFrom : Date;
	filterDOBTo : Date;
	filterDOBFromRelativeValue : String;
	filterDOBToRelativeValue : String;
	filterDOBFromRelativePeriod : String;
	filterDOBToRelativePeriod : String;
	filterValueSet : string;
	filterValueFrom : string;
	filterValueTo : string;
	filterSex : string;

	fieldTestDateFrom : Date;
	fieldTestDateTo : Date;
	fieldTestDateFromRelativeValue : String;
	fieldTestDateToRelativeValue : String;
	fieldTestDateFromRelativePeriod : String;
	fieldTestDateToRelativePeriod : String;
	fieldTestDOBFrom : Date;
	fieldTestDOBTo : Date;
	fieldTestDOBFromRelativeValue : String;
	fieldTestDOBToRelativeValue : String;
	fieldTestDOBFromRelativePeriod : String;
	fieldTestDOBToRelativePeriod : String;
	fieldTestValueFrom : string;
	fieldTestValueTo : string;
	fieldTestValueSet : string;
	fieldTestSex : string;

	restrictionFieldName: string;
	restrictionOrderDirection: string;
	restrictionCount: string = "1";
	codeFilter : boolean = false;
	dateFilter : boolean = false;
	valueFilter : boolean = false;
	dobFilter : boolean = false;
	sexFilter : boolean = false;
	ageFilter : boolean = false;
	regFilter : boolean = false;
	nhsFilter : boolean = false;
	disableRestrictionCount : boolean = false;

	editMode : boolean = false;

	codeSelection : CodeSetValue[] = [];
	fieldTestCodeSelection : CodeSetValue[] = [];
	termCache : any;

	datasources = ['',
		'AllergyIntolerance',
		'Appointment',
		'AppointmentResponse',
		'AuditEvent',
		'Basic',
		'Binary',
		'BodySite',
		'Bundle',
		'CarePlan',
		'Claim',
		'ClaimResponse',
		'ClinicalImpression',
		'Communication',
		'CommunicationRequest',
		'Composition',
		'ConceptMap',
		'Condition',
		'Conformance',
		'Contract',
		'DetectedIssue',
		'Coverage',
		'DataElement',
		'Device',
		'DeviceComponent',
		'DeviceMetric',
		'DeviceUseRequest',
		'DeviceUseStatement',
		'DiagnosticOrder',
		'DiagnosticReport',
		'DocumentManifest',
		'DocumentReference',
		'EligibilityRequest',
		'EligibilityResponse',
		'Encounter',
		'EnrollmentRequest',
		'EnrollmentResponse',
		'EpisodeOfCare',
		'ExplanationOfBenefit',
		'FamilyMemberHistory',
		'Flag',
		'Goal',
		'Group',
		'HealthcareService',
		'ImagingObjectSelection',
		'ImagingStudy',
		'Immunization',
		'ImmunizationRecommendation',
		'ImplementationGuide',
		'List',
		'Location',
		'Media',
		'Medication',
		'MedicationAdministration',
		'MedicationDispense',
		'MedicationOrder',
		'MedicationStatement',
		'MessageHeader',
		'NamingSystem',
		'NutritionOrder',
		'Observation',
		'OperationDefinition',
		'OperationOutcome',
		'Order',
		'OrderResponse',
		'Organization',
		'Parameters',
		'Patient',
		'PaymentNotice',
		'PaymentReconciliation',
		'Person',
		'Practitioner',
		'Procedure',
		'ProcessRequest',
		'ProcessResponse',
		'ProcedureRequest',
		'Provenance',
		'Questionnaire',
		'QuestionnaireResponse',
		'ReferralRequest',
		'RelatedPerson',
		'RiskAssessment',
		'Schedule',
		'SearchParameter',
		'Slot',
		'Specimen',
		'StructureDefinition',
		'Subscription',
		'Substance',
		'SupplyRequest',
		'SupplyDelivery',
		'TestScript',
		'ValueSet',
		'VisionPrescription'];

	sortorders = ['','ASCENDING','DESCENDING'];
	fields = ['','EFFECTIVE_DATE','TIMESTAMP','VALUE'];
	genders = ['','MALE','FEMALE','UNKNOWN'];
	periods = ['','WEEK','MONTH','YEAR'];

	public static open($modal : IModalService, test : Test, resourceOnly : boolean) : IModalServiceInstance {
		var options : IModalSettings = {
			template:require('./testEditor.html'),
			controller:'TestEditorController',
			controllerAs:'testEditor',
			size:'lg',
			backdrop: 'static',
			resolve:{
				test : () => test,
				resourceOnly : () => resourceOnly
			}
		};

		var dialog = $modal.open(options);
		return dialog;
	}

	static $inject = ['$uibModalInstance', 'LoggerService', '$uibModal', 'test', 'CodingService', 'resourceOnly'];

	constructor(protected $uibModalInstance : IModalServiceInstance,
				private logger : ILoggerService,
				private $modal : IModalService,
				private test: Test,
				private codingService : ICodingService,
				resourceOnly : boolean) {

		super($uibModalInstance);

		var vm = this;

		this.termCache = {};
		this.resultData = test;
		this.resourceOnly = resourceOnly;

		var ds : Resource = {
			heading: "",
			resourceUuid: null,
			calculation: null,
			filter: [],
			restriction: null
		};

		var isAny : IsAny = {}

		var newTest : Test = {
			resource: ds,
			resourceUuid: null,
			isAny: isAny,
			fieldTest: []
		};

		if (!this.resultData||!this.resultData.resource)
			this.resultData = newTest;
		else
			this.initialiseEditMode(this.resultData);

		if (!this.resourceOnly) {
			vm.viewFieldTest = true;
			vm.title = "Test Editor";
			vm.disableRestrictionCount = true;
		}
		else {
			vm.title = "Resource Editor";
			vm.viewFieldTest = false;
			vm.disableRestrictionCount = false;
		}
	}

	initialiseEditMode(resultData : Test) {
		var vm = this;

		vm.ruleDatasource = resultData.resource.heading;

		this.resourceChange(resultData.resource.heading);

		vm.editMode = true;

		if (resultData.resource.filter === null) {
			resultData.resource.filter = [];
		}

		if (!vm.resourceOnly) {
			if (resultData.fieldTest === null) {
				resultData.fieldTest = [];
			}
		}

		for (var i = 0; i < resultData.resource.filter.length; ++i) {
			var filter = resultData.resource.filter[i];
			var field = filter.field;

			this.showFilter(field);

			switch(field) {
				case "CODE":
					vm.codeSelection = filter.codeSet.codeSetValue;
					break;
				case "DOB":
					if (filter.valueFrom) {
						if (filter.valueFrom.absoluteUnit) {
							vm.filterDOBFrom = new Date(filter.valueFrom.constant);
							vm.dobdatetype = "dobabsolute";
						}
						else if (filter.valueFrom.relativeUnit) {
							vm.filterDOBFromRelativeValue = filter.valueFrom.constant;
							vm.filterDOBFromRelativePeriod = filter.valueFrom.relativeUnit;
							vm.dobdatetype = "dobrelative";
						}
					}
					if (filter.valueTo) {
						if (filter.valueTo.absoluteUnit) {
							vm.filterDOBTo = new Date(filter.valueTo.constant);
							vm.dobdatetype = "dobabsolute";
						}
						else if (filter.valueTo.relativeUnit) {
							vm.filterDOBToRelativeValue = filter.valueTo.constant;
							vm.filterDOBToRelativePeriod = filter.valueTo.relativeUnit;
							vm.dobdatetype = "dobrelative";
						}
					}
					break;
				case "EFFECTIVE_DATE":
				case "REGISTRATION_DATE":
					if (filter.valueFrom) {
						if (filter.valueFrom.absoluteUnit) {
							vm.filterDateFrom = new Date(filter.valueFrom.constant);
							vm.datetype = "absolute";
						}
						else if (filter.valueFrom.relativeUnit) {
							vm.filterDateFromRelativeValue = filter.valueFrom.constant;
							vm.filterDateFromRelativePeriod = filter.valueFrom.relativeUnit;
							vm.datetype = "relative";
						}
					}
					if (filter.valueTo) {
						if (filter.valueTo.absoluteUnit) {
							vm.filterDateTo = new Date(filter.valueTo.constant);
							vm.datetype = "absolute";
						}
						else if (filter.valueTo.relativeUnit) {
							vm.filterDateToRelativeValue = filter.valueTo.constant;
							vm.filterDateToRelativePeriod = filter.valueTo.relativeUnit;
							vm.datetype = "relative";
						}
					}
					break;
				case "VALUE":
				case "AGE":
					if (filter.valueFrom)
						vm.filterValueFrom = filter.valueFrom.constant;
					if (filter.valueTo)
						vm.filterValueTo = filter.valueTo.constant;
					break;
				case "SEX":
					if (filter.valueSet)
						vm.filterSex = filter.valueSet.value[0];
					break;
				case "NHS_NO":
					if (filter.valueSet) {
						//console.log(filter.valueSet.value);
						vm.filterValueSet = filter.valueSet.value.join();
					}

					break;
				default:
			}
		}

		if (!vm.resourceOnly) {
			for (var i = 0; i < resultData.fieldTest.length; ++i) {
				var fieldTest = resultData.fieldTest[i];
				var field = fieldTest.field;

				this.showFieldTest(field);

				switch(field) {
					case "CODE":
						vm.fieldTestCodeSelection = fieldTest.codeSet.codeSetValue;
						break;
					case "DOB":
						if (fieldTest.valueFrom) {
							if (fieldTest.valueFrom.absoluteUnit) {
								vm.fieldTestDOBFrom = new Date(fieldTest.valueFrom.constant);
								vm.fieldTestDobdatetype = "fieldtestdobabsolute";
							}
							else if (fieldTest.valueFrom.relativeUnit) {
								vm.fieldTestDOBFromRelativeValue = fieldTest.valueFrom.constant;
								vm.fieldTestDOBFromRelativePeriod = fieldTest.valueFrom.relativeUnit;
								vm.fieldTestDobdatetype = "fieldtestdobrelative";
							}
						}
						if (fieldTest.valueTo) {
							if (fieldTest.valueTo.absoluteUnit) {
								vm.fieldTestDOBTo = new Date(fieldTest.valueTo.constant);
								vm.fieldTestDobdatetype = "fieldtestdobabsolute";
							}
							else if (fieldTest.valueTo.relativeUnit) {
								vm.fieldTestDOBToRelativeValue = fieldTest.valueTo.constant;
								vm.fieldTestDOBToRelativePeriod = fieldTest.valueTo.relativeUnit;
								vm.fieldTestDobdatetype = "fieldtestdobrelative";
							}
						}
						break;
					case "EFFECTIVE_DATE":
					case "REGISTRATION_DATE":
						if (fieldTest.valueFrom) {
							if (fieldTest.valueFrom.absoluteUnit) {
								vm.fieldTestDateFrom = new Date(fieldTest.valueFrom.constant);
								vm.fieldTestDatetype = "fieldtestabsolute";
							}
							else if (fieldTest.valueFrom.relativeUnit) {
								vm.fieldTestDateFromRelativeValue = fieldTest.valueFrom.constant;
								vm.fieldTestDateFromRelativePeriod = fieldTest.valueFrom.relativeUnit;
								vm.fieldTestDatetype = "fieldtestrelative";
							}
						}
						if (fieldTest.valueTo) {
							if (fieldTest.valueTo.absoluteUnit) {
								vm.fieldTestDateTo = new Date(fieldTest.valueTo.constant);
								vm.fieldTestDatetype = "fieldtestabsolute";
							}
							else if (fieldTest.valueTo.relativeUnit) {
								vm.fieldTestDateToRelativeValue = fieldTest.valueTo.constant;
								vm.fieldTestDateToRelativePeriod = fieldTest.valueTo.relativeUnit;
								vm.fieldTestDatetype = "fieldtestrelative";
							}
						}
						break;
					case "VALUE":
					case "AGE":
						if (fieldTest.valueFrom)
							vm.fieldTestValueFrom = fieldTest.valueFrom.constant;
						if (fieldTest.valueTo)
							vm.fieldTestValueTo = fieldTest.valueTo.constant;
						break;
					case "SEX":
						if (fieldTest.valueSet)
							vm.fieldTestSex = fieldTest.valueSet.value[0];
						break;
					case "NHS_NO":
						if (fieldTest.valueSet)
							vm.fieldTestValueSet = fieldTest.valueSet.value.join();
						break;
					default:
				}
			}

		}

		if (resultData.resource.restriction) {
			vm.showRestriction = true;
			vm.restrictionFieldName = resultData.resource.restriction.fieldName;
			vm.restrictionOrderDirection = resultData.resource.restriction.orderDirection;
		}

	}

	formatDate(inputDate : Date) {
		return this.zeroFill(inputDate.getDate(),2)  + "-" + this.zeroFill((inputDate.getMonth()+1),2) + "-" + inputDate.getFullYear();
	}

	showCodePicker() {
		var vm = this;

		CodePickerController.open(this.$modal, vm.codeSelection)
			.result.then(function(resultData : CodeSetValue[]){

			if (vm.codeSelection.length>0) {
				for (var i = 0; i < vm.resultData.resource.filter.length; ++i) {
					var filter = vm.resultData.resource.filter[i];

					if (filter.field=="CODE")
						vm.resultData.resource.filter.splice(i, 1);
				}
			}

			vm.codeSelection = resultData;

			if (resultData.length==0) {
				return;
			}

			var codeSet : CodeSet = {
				codingSystem : "SNOMED_CT",
				codeSetValue : resultData
			}

			var fieldTest : FieldTest = {
				field: "CODE",
				valueFrom: null,
				valueTo: null,
				valueRange: null,
				valueEqualTo: null,
				codeSet: null,
				valueSet: null,
				codeSetLibraryItemUuid: null,
				negate: false
			};

			fieldTest.codeSet = codeSet;

			vm.resultData.resource.filter.push(fieldTest);
		});
	}

	removeFilter(filter: any) {
		var vm = this;

		for (var i = vm.resultData.resource.filter.length-1; i >= 0; --i) {
			var f = vm.resultData.resource.filter[i];

			switch(filter) {
				case "code":
					if (f.field=="CODE") {
						vm.codeEditor = false;
						vm.resultData.resource.filter.splice(i, 1);
					}
					break;
				case "dob":
					if (f.field=="DOB") {
						vm.dobEditor = false;
						vm.resultData.resource.filter.splice(i, 1);
					}
					break;
				case "sex":
					if (f.field=="SEX") {
						vm.sexEditor = false;
						vm.resultData.resource.filter.splice(i, 1);
					}
					break;
				case "date":
					if (f.field=="EFFECTIVE_DATE"||f.field=="REGISTRATION_DATE") {
						vm.dateEditor = false;
						vm.resultData.resource.filter.splice(i, 1);
					}
					break;
				case "value":
					if (f.field=="VALUE"||f.field=="AGE") {
						vm.valueEditor = false;
						vm.resultData.resource.filter.splice(i, 1);
					}
					break;
				case "valueSet":
					if (f.field=="NHS_NO") {
						vm.valueSetEditor = false;
						vm.resultData.resource.filter.splice(i, 1);
					}
					break;
				case "restriction":
					vm.showRestriction = false;
					vm.resultData.resource.restriction = null;
					break;

			}


		}

	}

	removeFieldTest(filter: any) {
		var vm = this;

		for (var i = vm.resultData.fieldTest.length-1; i >= 0; --i) {
			var f = vm.resultData.fieldTest[i];

			switch(filter) {
				case "code":
					if (f.field=="CODE") {
						vm.fieldTestCodeEditor = false;
						vm.resultData.fieldTest.splice(i, 1);
					}
					break;
				case "dob":
					if (f.field=="DOB") {
						vm.fieldTestDobEditor = false;
						vm.resultData.fieldTest.splice(i, 1);
					}
					break;
				case "sex":
					if (f.field=="SEX") {
						vm.fieldTestSexEditor = false;
						vm.resultData.fieldTest.splice(i, 1);
					}
					break;
				case "date":
					if (f.field=="EFFECTIVE_DATE"||f.field=="REGISTRATION_DATE") {
						vm.fieldTestDateEditor = false;
						vm.resultData.fieldTest.splice(i, 1);
					}
					break;
				case "value":
					if (f.field=="VALUE"||f.field=="AGE") {
						vm.fieldTestValueEditor = false;
						vm.resultData.fieldTest.splice(i, 1);
					}
					break;
				case "valueSet":
					if (f.field=="NHS_NO") {
						vm.fieldTestValueSetEditor = false;
						vm.resultData.fieldTest.splice(i, 1);
					}
					break;


			}


		}

	}

	showFieldTestCodePicker() {
		var vm = this;

		CodePickerController.open(this.$modal, vm.fieldTestCodeSelection)
			.result.then(function(resultData : CodeSetValue[]){

			if (vm.fieldTestCodeSelection.length>0) {
				for (var i = 0; i < vm.resultData.fieldTest.length; ++i) {
					var fTest = vm.resultData.fieldTest[i];

					if (fTest.field=="CODE")
						vm.resultData.fieldTest.splice(i, 1);
				}
			}

			vm.fieldTestCodeSelection = resultData;

			var codeSet : CodeSet = {
				codingSystem : "SNOMED_CT",
				codeSetValue : resultData
			}

			var fieldTest : FieldTest = {
				field: "CODE",
				valueFrom: null,
				valueTo: null,
				valueRange: null,
				valueEqualTo: null,
				codeSet: null,
				valueSet: null,
				codeSetLibraryItemUuid: null,
				negate: false
			};

			fieldTest.codeSet = codeSet;

			vm.resultData.fieldTest.push(fieldTest);
		});
	}

	resourceChange(value : any) {
		var vm = this;

		this.resultData.resource.heading = value;

		vm.codeEditor = false;
		vm.dateEditor = false;
		vm.dobEditor = false;
		vm.valueEditor = false;
		vm.valueSetEditor = false;
		vm.sexEditor = false;
		vm.fieldTestCodeEditor = false;
		vm.fieldTestDateEditor = false;
		vm.fieldTestDobEditor = false;
		vm.fieldTestValueEditor = false;
		vm.fieldTestValueSetEditor = false;
		vm.fieldTestSexEditor = false;
		vm.addRestriction = true;
		vm.showRestriction = false;
		vm.addFilter = true;
		vm.codeFilter = false;
		vm.dateFilter = false;
		vm.valueFilter = false;
		vm.nhsFilter = false;
		vm.dobFilter = false;
		vm.sexFilter = false;
		vm.ageFilter = false;
		vm.regFilter = false;
		vm.viewFieldTest = true;

		switch(value) {
			case "Observation":
			case "Condition":
				vm.codeFilter = true;
				vm.dateFilter = true;
				vm.valueFilter = true;
				break;
			case "MedicationOrder":
				vm.codeFilter = true;
				vm.dateFilter = true;
				break;
			case "Patient":
				vm.dobFilter = true;
				vm.sexFilter = true;
				vm.ageFilter = true;
				vm.regFilter = true;
				vm.nhsFilter = true;
				break;
			default:
		}
	};

	showFilter(value : any) {
		var vm = this;

		switch(value) {
			case "CODE":
				vm.codeEditor = true;
				break;
			case "DOB":
				vm.dobEditor = true;
				vm.dobLabel = value;
				break;
			case "EFFECTIVE_DATE":
			case "REGISTRATION_DATE":
				vm.dateEditor = true;
				vm.dateLabel = value;
				break;
			case "VALUE":
			case "AGE":
				vm.valueEditor = true;
				vm.valueField = value;
				break;
			case "SEX":
				vm.sexEditor = true;
				vm.sexLabel = value;
				break;
			case "NHS_NO":
				vm.valueSetEditor = true;
				vm.valueSetField = value;
				break;
			default:
		}
	}

	showFieldTest(value : any) {
		var vm = this;

		switch(value) {
			case "CODE":
				vm.fieldTestCodeEditor = true;
				break;
			case "DOB":
				vm.fieldTestDobEditor = true;
				vm.fieldTestDobLabel = value;
				break;
			case "EFFECTIVE_DATE":
			case "REGISTRATION_DATE":
				vm.fieldTestDateEditor = true;
				vm.fieldTestDateLabel = value;
				break;
			case "VALUE":
			case "AGE":
				vm.fieldTestValueEditor = true;
				vm.fieldTestValueField = value;
				break;
			case "SEX":
				vm.fieldTestSexEditor = true;
				vm.fieldTestSexLabel = value;
				break;
			case "NHS_NO":
				vm.fieldTestValueSetEditor = true;
				vm.fieldTestValueSetField = value;
				break;
			default:
		}
	}

	filterDateFromChange(value : any, dateField : any) {
		var vm = this;

		if (!value)
			value="";

		var datestring : string = "";

		if (value!="" && value!=null)
			datestring = value.getFullYear()  + "-" + this.zeroFill((value.getMonth()+1),2) + "-" + this.zeroFill(value.getDate(),2);

		var valueFrom : ValueFrom = {
			constant: datestring,
			parameter: null,
			absoluteUnit: "DATE",
			relativeUnit: null,
			operator: "GREATER_THAN_OR_EQUAL_TO"
		}

		var fieldTest : FieldTest = {
			field: dateField,
			valueFrom: valueFrom,
			valueTo: null,
			valueRange: null,
			valueEqualTo: null,
			codeSet: null,
			valueSet: null,
			codeSetLibraryItemUuid: null,
			negate: false
		};

		var foundEntry : boolean = false;

		for (var i = 0; i < vm.resultData.resource.filter.length; ++i) {
			var filter = vm.resultData.resource.filter[i];

			if (filter.field==dateField && filter.valueFrom && value!="" && value!=null) {
				foundEntry = true;
				filter.valueFrom = valueFrom;
				break;
			}
			else if (filter.field==dateField && filter.valueFrom && (value=="" || value==null))
				vm.resultData.resource.filter.splice(i, 1);
		}

		if (!foundEntry && value!="" && value!=null)
			vm.resultData.resource.filter.push(fieldTest);
	}

	filterRelativeDateFromChange(value : any, period : any, dateField : any) {
		var vm = this;

		if (!value)
			value="";

		var valueFrom : ValueFrom = {
			constant: value,
			parameter: null,
			absoluteUnit: null,
			relativeUnit: period,
			operator: "GREATER_THAN_OR_EQUAL_TO"
		}

		var fieldTest : FieldTest = {
			field: dateField,
			valueFrom: valueFrom,
			valueTo: null,
			valueRange: null,
			valueEqualTo: null,
			codeSet: null,
			valueSet: null,
			codeSetLibraryItemUuid: null,
			negate: false
		};

		var foundEntry : boolean = false;

		for (var i = 0; i < vm.resultData.resource.filter.length; ++i) {
			var filter = vm.resultData.resource.filter[i];

			if (filter.field==dateField && filter.valueFrom && value!="" && value!=null) {
				foundEntry = true;
				filter.valueFrom = valueFrom;
				break;
			}
			else if (filter.field==dateField && filter.valueFrom && (value=="" || value==null))
				vm.resultData.resource.filter.splice(i, 1);
		}

		if (!foundEntry && value!="" && value!=null)
			vm.resultData.resource.filter.push(fieldTest);
	}

	filterDateToChange(value : any, dateField : any) {
		var vm = this;

		if (!value)
			value="";

		var datestring : string = "";

		if (value!="" && value!=null)
			datestring = value.getFullYear()  + "-" + this.zeroFill((value.getMonth()+1),2) + "-" + this.zeroFill(value.getDate(),2);

		var valueTo : ValueTo = {
			constant: datestring,
			parameter: null,
			absoluteUnit: "DATE",
			relativeUnit: null,
			operator: "LESS_THAN_OR_EQUAL_TO"
		}

		var fieldTest : FieldTest = {
			field: dateField,
			valueFrom: null,
			valueTo: valueTo,
			valueRange: null,
			valueEqualTo: null,
			codeSet: null,
			valueSet: null,
			codeSetLibraryItemUuid: null,
			negate: false
		};

		var foundEntry : boolean = false;

		for (var i = 0; i < vm.resultData.resource.filter.length; ++i) {
			var filter = vm.resultData.resource.filter[i];

			if (filter.field==dateField && filter.valueTo && value!="" && value!=null) {
				foundEntry = true;
				filter.valueTo = valueTo;
				break;
			}
			else if (filter.field==dateField && filter.valueTo && (value=="" || value==null))
				vm.resultData.resource.filter.splice(i, 1);
		}

		if (!foundEntry && value!="" && value!=null)
			vm.resultData.resource.filter.push(fieldTest);
	}

	filterRelativeDateToChange(value : any, period : any, dateField : any) {
		var vm = this;

		if (!value)
			value="";

		var valueTo : ValueTo = {
			constant: value,
			parameter: null,
			absoluteUnit: null,
			relativeUnit: period,
			operator: "LESS_THAN_OR_EQUAL_TO"
		}

		var fieldTest : FieldTest = {
			field: dateField,
			valueFrom: null,
			valueTo: valueTo,
			valueRange: null,
			valueEqualTo: null,
			codeSet: null,
			valueSet: null,
			codeSetLibraryItemUuid: null,
			negate: false
		};

		var foundEntry : boolean = false;

		for (var i = 0; i < vm.resultData.resource.filter.length; ++i) {
			var filter = vm.resultData.resource.filter[i];

			if (filter.field==dateField && filter.valueTo && value!="" && value!=null) {
				foundEntry = true;
				filter.valueTo = valueTo;
				break;
			}
			else if (filter.field==dateField && filter.valueTo && (value=="" || value==null))
				vm.resultData.resource.filter.splice(i, 1);
		}

		if (!foundEntry && value!="" && value!=null)
			vm.resultData.resource.filter.push(fieldTest);
	}

	zeroFill( number : any, width : any ) {
		width -= number.toString().length;
		if ( width > 0 )
		{
			return new Array( width + (/\./.test( number ) ? 2 : 1) ).join( '0' ) + number;
		}
		return number + ""; // always return a string
	}

	filterValueChange(value : any, valueField : any) {
		var vm = this;

		if (!value)
			value="";

		var valueSet : ValueSet = {
			value: []
		}

		valueSet.value.push(value);

		var fieldTest : FieldTest = {
			field: valueField,
			valueFrom: null,
			valueTo: null,
			valueRange: null,
			valueEqualTo: null,
			codeSet: null,
			valueSet: valueSet,
			codeSetLibraryItemUuid: null,
			negate: false
		};

		var foundEntry : boolean = false;

		for (var i = 0; i < vm.resultData.resource.filter.length; ++i) {
			var filter = vm.resultData.resource.filter[i];

			if (filter.field==valueField && filter.valueSet && value!="") {
				foundEntry = true;
				filter.valueSet = valueSet;
				break;
			}
			else if (filter.field==valueField && filter.valueSet && value=="")
				vm.resultData.resource.filter.splice(i, 1);
		}

		if (!foundEntry && value!="")
			vm.resultData.resource.filter.push(fieldTest);
	}

	filterValueFromChange(value : any) {
		var vm = this;

		if (!value)
			value="";

		var valueFrom : ValueFrom = {
			constant: value,
			parameter: null,
			absoluteUnit: "NUMERIC",
			relativeUnit: null,
			operator: "GREATER_THAN_OR_EQUAL_TO"
		}

		var fieldTest : FieldTest = {
			field: vm.valueField,
			valueFrom: valueFrom,
			valueTo: null,
			valueRange: null,
			valueEqualTo: null,
			codeSet: null,
			valueSet: null,
			codeSetLibraryItemUuid: null,
			negate: false
		};

		var foundEntry : boolean = false;

		for (var i = 0; i < vm.resultData.resource.filter.length; ++i) {
			var filter = vm.resultData.resource.filter[i];

			if (filter.field==vm.valueField && filter.valueFrom && value!="") {
				foundEntry = true;
				filter.valueFrom = valueFrom;
				break;
			}
			else if (filter.field==vm.valueField && filter.valueFrom && value=="")
				vm.resultData.resource.filter.splice(i, 1);
		}

		if (!foundEntry && value!="")
			vm.resultData.resource.filter.push(fieldTest);
	}

	filterValueToChange(value : any) {
		var vm = this;

		if (!value)
			value="";

		var valueTo : ValueTo = {
			constant: value,
			parameter: null,
			absoluteUnit: "NUMERIC",
			relativeUnit: null,
			operator: "LESS_THAN_OR_EQUAL_TO"
		}

		var fieldTest : FieldTest = {
			field: vm.valueField,
			valueFrom: null,
			valueTo: valueTo,
			valueRange: null,
			valueEqualTo: null,
			codeSet: null,
			valueSet: null,
			codeSetLibraryItemUuid: null,
			negate: false
		};

		var foundEntry : boolean = false;

		for (var i = 0; i < vm.resultData.resource.filter.length; ++i) {
			var filter = vm.resultData.resource.filter[i];

			if (filter.field==vm.valueField && filter.valueTo && value!="") {
				foundEntry = true;
				filter.valueTo = valueTo;
				break;
			}
			else if (filter.field==vm.valueField && filter.valueTo && value=="")
				vm.resultData.resource.filter.splice(i, 1);
		}

		if (!foundEntry && value!="")
			vm.resultData.resource.filter.push(fieldTest);

	}

	filterValueSetChange(value : any) {
		var vm = this;

		if (!value)
			value="";

		var array = value.split(',');

		var valueSet : ValueSet = {
			value: array
		}

		var fieldTest : FieldTest = {
			field: vm.valueSetField,
			valueFrom: null,
			valueTo: null,
			valueRange: null,
			valueEqualTo: null,
			codeSet: null,
			valueSet: valueSet,
			codeSetLibraryItemUuid: null,
			negate: false
		};

		var foundEntry : boolean = false;

		for (var i = 0; i < vm.resultData.resource.filter.length; ++i) {
			var filter = vm.resultData.resource.filter[i];

			if (filter.field==vm.valueSetField && filter.valueSet && value!="") {
				foundEntry = true;
				filter.valueSet = valueSet;
				break;
			}
			else if (filter.field==vm.valueSetField && filter.valueSet && value=="")
				vm.resultData.resource.filter.splice(i, 1);
		}

		if (!foundEntry && value!="")
			vm.resultData.resource.filter.push(fieldTest);
	}

	fieldTestDateFromChange(value : any, dateField : any) {
		var vm = this;

		if (!value)
			value="";

		var datestring : string = "";

		if (value!="" && value!=null)
			datestring = value.getFullYear()  + "-" + this.zeroFill((value.getMonth()+1),2) + "-" + this.zeroFill(value.getDate(),2);

		var valueFrom : ValueFrom = {
			constant: datestring,
			parameter: null,
			absoluteUnit: "DATE",
			relativeUnit: null,
			operator: "GREATER_THAN_OR_EQUAL_TO"
		}

		var fieldTest : FieldTest = {
			field: dateField,
			valueFrom: valueFrom,
			valueTo: null,
			valueRange: null,
			valueEqualTo: null,
			codeSet: null,
			valueSet: null,
			codeSetLibraryItemUuid: null,
			negate: false
		};

		var foundEntry : boolean = false;

		for (var i = 0; i < vm.resultData.fieldTest.length; ++i) {
			var ftest = vm.resultData.fieldTest[i];

			if (ftest.field==dateField && ftest.valueFrom && value!="" && value!=null) {
				foundEntry = true;
				ftest.valueFrom = valueFrom;
				break;
			}
			else if (ftest.field==dateField && ftest.valueFrom && (value=="" || value==null))
				vm.resultData.fieldTest.splice(i, 1);
		}

		if (!foundEntry && value!="" && value!=null)
			vm.resultData.fieldTest.push(fieldTest);
	}

	fieldTestRelativeDateFromChange(value : any, period : any, dateField : any) {
		var vm = this;

		if (!value)
			value="";

		var valueFrom : ValueFrom = {
			constant: value,
			parameter: null,
			absoluteUnit: null,
			relativeUnit: period,
			operator: "GREATER_THAN_OR_EQUAL_TO"
		}

		var fieldTest : FieldTest = {
			field: dateField,
			valueFrom: valueFrom,
			valueTo: null,
			valueRange: null,
			valueEqualTo: null,
			codeSet: null,
			valueSet: null,
			codeSetLibraryItemUuid: null,
			negate: false
		};

		var foundEntry : boolean = false;

		for (var i = 0; i < vm.resultData.fieldTest.length; ++i) {
			var ftest = vm.resultData.fieldTest[i];

			if (ftest.field==dateField && ftest.valueFrom && value!="" && value!=null) {
				foundEntry = true;
				ftest.valueFrom = valueFrom;
				break;
			}
			else if (ftest.field==dateField && ftest.valueFrom && (value=="" || value==null))
				vm.resultData.fieldTest.splice(i, 1);
		}

		if (!foundEntry && value!="" && value!=null)
			vm.resultData.fieldTest.push(fieldTest);
	}

	fieldTestDateToChange(value : any, dateField : any) {
		var vm = this;

		if (!value)
			value="";

		var datestring : string = "";

		if (value!="" && value!=null)
			datestring = value.getFullYear()  + "-" + this.zeroFill((value.getMonth()+1),2) + "-" + this.zeroFill(value.getDate(),2);

		var valueTo : ValueTo = {
			constant: datestring,
			parameter: null,
			absoluteUnit: "DATE",
			relativeUnit: null,
			operator: "LESS_THAN_OR_EQUAL_TO"
		}

		var fieldTest : FieldTest = {
			field: dateField,
			valueFrom: null,
			valueTo: valueTo,
			valueRange: null,
			valueEqualTo: null,
			codeSet: null,
			valueSet: null,
			codeSetLibraryItemUuid: null,
			negate: false
		};

		var foundEntry : boolean = false;

		for (var i = 0; i < vm.resultData.fieldTest.length; ++i) {
			var ftest = vm.resultData.fieldTest[i];

			if (ftest.field==dateField && ftest.valueTo && value!="" && value!=null) {
				foundEntry = true;
				ftest.valueTo = valueTo;
				break;
			}
			else if (ftest.field==dateField && ftest.valueTo && (value=="" || value==null))
				vm.resultData.fieldTest.splice(i, 1);
		}

		if (!foundEntry && value!="" && value!=null)
			vm.resultData.fieldTest.push(fieldTest);
	}

	fieldTestRelativeDateToChange(value : any, period : any, dateField : any) {
		var vm = this;

		if (!value)
			value="";

		var valueTo : ValueTo = {
			constant: value,
			parameter: null,
			absoluteUnit: null,
			relativeUnit: period,
			operator: "LESS_THAN_OR_EQUAL_TO"
		}

		var fieldTest : FieldTest = {
			field: dateField,
			valueFrom: null,
			valueTo: valueTo,
			valueRange: null,
			valueEqualTo: null,
			codeSet: null,
			valueSet: null,
			codeSetLibraryItemUuid: null,
			negate: false
		};

		var foundEntry : boolean = false;

		for (var i = 0; i < vm.resultData.fieldTest.length; ++i) {
			var ftest = vm.resultData.fieldTest[i];

			if (ftest.field==dateField && ftest.valueTo && value!="" && value!=null) {
				foundEntry = true;
				ftest.valueTo = valueTo;
				break;
			}
			else if (ftest.field==dateField && ftest.valueTo && (value=="" || value==null))
				vm.resultData.fieldTest.splice(i, 1);
		}

		if (!foundEntry && value!="" && value!=null)
			vm.resultData.fieldTest.push(fieldTest);
	}

	fieldTestValueChange(value : any, valueField : any) {
		var vm = this;

		if (!value)
			value="";

		var valueSet : ValueSet = {
			value: []
		}

		valueSet.value.push(value);

		var fieldTest : FieldTest = {
			field: valueField,
			valueFrom: null,
			valueTo: null,
			valueRange: null,
			valueEqualTo: null,
			codeSet: null,
			valueSet: valueSet,
			codeSetLibraryItemUuid: null,
			negate: false
		};

		var foundEntry : boolean = false;

		for (var i = 0; i < vm.resultData.fieldTest.length; ++i) {
			var ftest = vm.resultData.fieldTest[i];

			if (ftest.field==valueField && ftest.valueSet && value!="") {
				foundEntry = true;
				ftest.valueSet = valueSet;
				break;
			}
			else if (ftest.field==valueField && ftest.valueSet && value=="")
				vm.resultData.fieldTest.splice(i, 1);
		}

		if (!foundEntry && value!="")
			vm.resultData.fieldTest.push(fieldTest);
	}

	fieldTestValueFromChange(value : any) {
		var vm = this;

		if (!value)
			value="";

		var valueFrom : ValueFrom = {
			constant: value,
			parameter: null,
			absoluteUnit: "NUMERIC",
			relativeUnit: null,
			operator: "GREATER_THAN_OR_EQUAL_TO"
		}

		var fieldTest : FieldTest = {
			field: vm.fieldTestValueField,
			valueFrom: valueFrom,
			valueTo: null,
			valueRange: null,
			valueEqualTo: null,
			codeSet: null,
			valueSet: null,
			codeSetLibraryItemUuid: null,
			negate: false
		};

		var foundEntry : boolean = false;

		for (var i = 0; i < vm.resultData.fieldTest.length; ++i) {
			var ftest = vm.resultData.fieldTest[i];

			if (ftest.field==vm.fieldTestValueField && ftest.valueFrom && value!="") {
				foundEntry = true;
				ftest.valueFrom = valueFrom;
				break;
			}
			else if (ftest.field==vm.fieldTestValueField && ftest.valueFrom && value=="")
				vm.resultData.fieldTest.splice(i, 1);
		}

		if (!foundEntry && value!="")
			vm.resultData.fieldTest.push(fieldTest);
	}

	fieldTestValueToChange(value : any) {
		var vm = this;

		if (!value)
			value="";

		var valueTo : ValueTo = {
			constant: value,
			parameter: null,
			absoluteUnit: "NUMERIC",
			relativeUnit: null,
			operator: "LESS_THAN_OR_EQUAL_TO"
		}

		var fieldTest : FieldTest = {
			field: vm.fieldTestValueField,
			valueFrom: null,
			valueTo: valueTo,
			valueRange: null,
			valueEqualTo: null,
			codeSet: null,
			valueSet: null,
			codeSetLibraryItemUuid: null,
			negate: false
		};

		var foundEntry : boolean = false;

		for (var i = 0; i < vm.resultData.fieldTest.length; ++i) {
			var ftest = vm.resultData.fieldTest[i];

			if (ftest.field==vm.fieldTestValueField && ftest.valueTo && value!="") {
				foundEntry = true;
				ftest.valueTo = valueTo;
				break;
			}
			else if (ftest.field==vm.fieldTestValueField && ftest.valueTo && value=="")
				vm.resultData.fieldTest.splice(i, 1);
		}

		if (!foundEntry && value!="")
			vm.resultData.fieldTest.push(fieldTest);

	}

	fieldTestValueSetChange(value : any) {
		var vm = this;

		if (!value)
			value="";

		var array = value.split(',');

		var valueSet : ValueSet = {
			value: array
		}

		var fieldTest : FieldTest = {
			field: vm.fieldTestValueSetField,
			valueFrom: null,
			valueTo: null,
			valueRange: null,
			valueEqualTo: null,
			codeSet: null,
			valueSet: valueSet,
			codeSetLibraryItemUuid: null,
			negate: false
		};

		var foundEntry : boolean = false;

		for (var i = 0; i < vm.resultData.fieldTest.length; ++i) {
			var ftest = vm.resultData.fieldTest[i];

			if (ftest.field==vm.fieldTestValueSetField && ftest.valueSet && value!="") {
				foundEntry = true;
				ftest.valueSet = valueSet;
				break;
			}
			else if (ftest.field==vm.fieldTestValueSetField && ftest.valueSet && value=="")
				vm.resultData.fieldTest.splice(i, 1);
		}

		if (!foundEntry && value!="")
			vm.resultData.fieldTest.push(fieldTest);
	}

	restrictionChange(value : any) {
		var vm = this;

		if (!value || vm.restrictionFieldName=="" || vm.restrictionOrderDirection=="") {
			vm.resultData.resource.restriction = null;
			return;
		}

		var restriction : Restriction = {
			fieldName: vm.restrictionFieldName,
			orderDirection: vm.restrictionOrderDirection,
			count: Number(vm.restrictionCount)
		};

		vm.resultData.resource.restriction = restriction;
	}

	toggleRestriction() {
		var vm = this;

		vm.showRestriction = !vm.showRestriction;
	};

	save() {
		var vm = this;

		if (!vm.resourceOnly) {
			for (var i = 0; i < vm.resultData.fieldTest.length; ++i) {
				var ft = vm.resultData.fieldTest[i];
				if (ft.field=="CODE") {
					if (ft.codeSet.codeSetValue.length==0) {
						vm.resultData.fieldTest.splice(i, 1);
					}
				}
			}

			if (vm.resultData.fieldTest.length>0)
				vm.resultData.isAny = null;
			else
				vm.resultData.isAny = {};
		}

		this.ok();
	}

	termShorten(term : string) {
		term = term.replace(' (disorder)','');
		term = term.replace(' (observable entity)','');
		term = term.replace(' (finding)','');
		return term;
	}

	getTerm(code : string) : string {
		var vm = this;
		var term = vm.termCache[code];
		if (term) { return term; }
		vm.termCache[code] = 'Loading...';

		vm.codingService.getPreferredTerm(code)
			.then(function(concept : Concept) {
				vm.termCache[code] = vm.termShorten(concept.preferredTerm);
			});

		return vm.termCache[code];
	}
}

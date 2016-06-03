/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />

module app.dialogs {
	import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
	import IModalService = angular.ui.bootstrap.IModalService;
	import CodePickerController = app.dialogs.CodePickerController;
	import ICodingService = app.core.ICodingService;
	import Code = app.models.Code;
	import Test = app.models.Test;
	import DataSource = app.models.DataSource;
	import FieldTest = app.models.FieldTest;
	import CodeSet = app.models.CodeSet;
	import CodeSetValue = app.models.CodeSetValue;
	import ValueFrom = app.models.ValueFrom;
	import ValueTo = app.models.ValueTo;
	import Value = app.models.Value;
	import ValueSet = app.models.ValueSet;
	import ValueAbsoluteUnit = app.models.ValueAbsoluteUnit;
	import ValueFromOperator = app.models.ValueFromOperator;
	import ValueToOperator = app.models.ValueToOperator;
	import IsAny = app.models.IsAny;
	import Concept = app.models.Concept;
	import Restriction = app.models.Restriction;

	'use strict';

	export class TestEditorController extends BaseDialogController {
		title : string;
		dataSourceOnly : boolean = false;
		viewFieldTest : boolean = false;
		codeEditor : boolean = false;
		dateEditor : boolean = false;
		dobEditor : boolean = false;
		valueEditor : boolean = false;
		sexEditor : boolean = false;
		valueField : string;
		dateLabel : string;
		dobLabel : string;
		sexLabel : string;
		fieldTestCodeEditor : boolean = false;
		fieldTestDateEditor : boolean = false;
		fieldTestDobEditor : boolean = false;
		fieldTestValueEditor : boolean = false;
		fieldTestSexEditor : boolean = false;
		fieldTestValueField : string;
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
		disableRestrictionCount : boolean = false;

		editMode : boolean = false;

		codeSelection : CodeSetValue[] = [];
		fieldTestCodeSelection : CodeSetValue[] = [];
		termCache : any;

		datasources = ['','PATIENT','OBSERVATION','MEDICATION_ISSUE'];
		sortorders = ['','ASCENDING','DESCENDING'];
		fields = ['','EFFECTIVE_DATE','TIMESTAMP','VALUE'];
		genders = ['','MALE','FEMALE','UNKNOWN'];
		periods = ['','WEEK','MONTH','YEAR'];

		public static open($modal : IModalService, test : Test, dataSourceOnly : boolean) : IModalServiceInstance {
			var options : IModalSettings = {
				templateUrl:'app/dialogs/testEditor/testEditor.html',
				controller:'TestEditorController',
				controllerAs:'testEditor',
				size:'lg',
				backdrop: 'static',
				resolve:{
					test : () => test,
					dataSourceOnly : () => dataSourceOnly
				}
			};

			var dialog = $modal.open(options);
			return dialog;
		}

		static $inject = ['$uibModalInstance', 'LoggerService', '$uibModal', 'test', 'CodingService', 'dataSourceOnly'];

		constructor(protected $uibModalInstance : IModalServiceInstance,
					private logger : app.blocks.ILoggerService,
					private $modal : IModalService,
					private test: Test,
					private codingService : ICodingService,
					dataSourceOnly : boolean) {

			super($uibModalInstance);

			var vm = this;

			this.termCache = {};
			this.resultData = test;
			this.dataSourceOnly = dataSourceOnly;

			var ds : DataSource = {
				entity: "",
				dataSourceUuid: null,
				calculation: null,
				filter: [],
				restriction: null
			};

			var isAny : IsAny = {}

			var newTest : Test = {
				dataSource: ds,
				dataSourceUuid: null,
				isAny: isAny,
				fieldTest: []
			};

			if (!this.resultData||!this.resultData.dataSource)
				this.resultData = newTest;
			else
				this.initialiseEditMode(this.resultData);

			if (!this.dataSourceOnly) {
				vm.viewFieldTest = true;
				vm.title = "Test Editor";
				vm.disableRestrictionCount = true;
			}
			else {
				vm.title = "Data Source Editor";
				vm.viewFieldTest = false;
				vm.disableRestrictionCount = false;
			}
		}

		initialiseEditMode(resultData : Test) {
			var vm = this;

			vm.ruleDatasource = resultData.dataSource.entity;
			this.dataSourceChange(resultData.dataSource.entity);

			vm.editMode = true;

			if (resultData.dataSource.filter === null) {
				resultData.dataSource.filter = [];
			}

			if (!vm.dataSourceOnly) {
				if (resultData.fieldTest === null) {
					resultData.fieldTest = [];
				}
			}

			for (var i = 0; i < resultData.dataSource.filter.length; ++i) {
				var filter = resultData.dataSource.filter[i];
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
					default:
				}
			}

			if (!vm.dataSourceOnly) {
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
						default:
					}
				}

			}

			if (resultData.dataSource.restriction) {
				vm.showRestriction = true;
				vm.restrictionFieldName = resultData.dataSource.restriction.fieldName;
				vm.restrictionOrderDirection = resultData.dataSource.restriction.orderDirection;
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
					for (var i = 0; i < vm.resultData.dataSource.filter.length; ++i) {
						var filter = vm.resultData.dataSource.filter[i];

						if (filter.field=="CODE")
							vm.resultData.dataSource.filter.splice(i, 1);
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

				vm.resultData.dataSource.filter.push(fieldTest);
			});
		}

		removeFilter(filter: any) {
			var vm = this;

			for (var i = vm.resultData.dataSource.filter.length-1; i >= 0; --i) {
				var f = vm.resultData.dataSource.filter[i];

				switch(filter) {
					case "code":
						if (f.field=="CODE") {
							vm.codeEditor = false;
							vm.resultData.dataSource.filter.splice(i, 1);
						}
						break;
					case "dob":
						if (f.field=="DOB") {
							vm.dobEditor = false;
							vm.resultData.dataSource.filter.splice(i, 1);
						}
						break;
					case "sex":
						if (f.field=="SEX") {
							vm.sexEditor = false;
							vm.resultData.dataSource.filter.splice(i, 1);
						}
						break;
					case "date":
						if (f.field=="EFFECTIVE_DATE"||f.field=="REGISTRATION_DATE") {
							vm.dateEditor = false;
							vm.resultData.dataSource.filter.splice(i, 1);
						}
						break;
					case "value":
						if (f.field=="VALUE"||f.field=="AGE") {
							vm.valueEditor = false;
							vm.resultData.dataSource.filter.splice(i, 1);
						}
						break;
					case "restriction":
						vm.showRestriction = false;
						vm.resultData.dataSource.restriction = null;
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

		dataSourceChange(value : any) {
			var vm = this;

			this.resultData.dataSource.entity = value;

			vm.codeEditor = false;
			vm.dateEditor = false;
			vm.dobEditor = false;
			vm.valueEditor = false;
			vm.sexEditor = false;
			vm.fieldTestCodeEditor = false;
			vm.fieldTestDateEditor = false;
			vm.fieldTestDobEditor = false;
			vm.fieldTestValueEditor = false;
			vm.fieldTestSexEditor = false;
			vm.addRestriction = true;
			vm.showRestriction = false;
			vm.addFilter = true;
			vm.codeFilter = false;
			vm.dateFilter = false;
			vm.valueFilter = false;
			vm.dobFilter = false;
			vm.sexFilter = false;
			vm.ageFilter = false;
			vm.regFilter = false;
			vm.viewFieldTest = true;

			switch(value) {
				case "OBSERVATION":
					vm.codeFilter = true;
					vm.dateFilter = true;
					vm.valueFilter = true;
					break;
				case "MEDICATION_ISSUE":
					vm.codeFilter = true;
					vm.dateFilter = true;
					break;
				case "PATIENT":
					vm.dobFilter = true;
					vm.sexFilter = true;
					vm.ageFilter = true;
					vm.regFilter = true;
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

			for (var i = 0; i < vm.resultData.dataSource.filter.length; ++i) {
				var filter = vm.resultData.dataSource.filter[i];

				if (filter.field==dateField && filter.valueFrom && value!="" && value!=null) {
					foundEntry = true;
					filter.valueFrom = valueFrom;
					break;
				}
				else if (filter.field==dateField && filter.valueFrom && (value=="" || value==null))
					vm.resultData.dataSource.filter.splice(i, 1);
			}

			if (!foundEntry && value!="" && value!=null)
				vm.resultData.dataSource.filter.push(fieldTest);
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

			for (var i = 0; i < vm.resultData.dataSource.filter.length; ++i) {
				var filter = vm.resultData.dataSource.filter[i];

				if (filter.field==dateField && filter.valueFrom && value!="" && value!=null) {
					foundEntry = true;
					filter.valueFrom = valueFrom;
					break;
				}
				else if (filter.field==dateField && filter.valueFrom && (value=="" || value==null))
					vm.resultData.dataSource.filter.splice(i, 1);
			}

			if (!foundEntry && value!="" && value!=null)
				vm.resultData.dataSource.filter.push(fieldTest);
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

			for (var i = 0; i < vm.resultData.dataSource.filter.length; ++i) {
				var filter = vm.resultData.dataSource.filter[i];

				if (filter.field==dateField && filter.valueTo && value!="" && value!=null) {
					foundEntry = true;
					filter.valueTo = valueTo;
					break;
				}
				else if (filter.field==dateField && filter.valueTo && (value=="" || value==null))
					vm.resultData.dataSource.filter.splice(i, 1);
			}

			if (!foundEntry && value!="" && value!=null)
				vm.resultData.dataSource.filter.push(fieldTest);
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

			for (var i = 0; i < vm.resultData.dataSource.filter.length; ++i) {
				var filter = vm.resultData.dataSource.filter[i];

				if (filter.field==dateField && filter.valueTo && value!="" && value!=null) {
					foundEntry = true;
					filter.valueTo = valueTo;
					break;
				}
				else if (filter.field==dateField && filter.valueTo && (value=="" || value==null))
					vm.resultData.dataSource.filter.splice(i, 1);
			}

			if (!foundEntry && value!="" && value!=null)
				vm.resultData.dataSource.filter.push(fieldTest);
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

			for (var i = 0; i < vm.resultData.dataSource.filter.length; ++i) {
				var filter = vm.resultData.dataSource.filter[i];

				if (filter.field==valueField && filter.valueSet && value!="") {
					foundEntry = true;
					filter.valueSet = valueSet;
					break;
				}
				else if (filter.field==valueField && filter.valueSet && value=="")
					vm.resultData.dataSource.filter.splice(i, 1);
			}

			if (!foundEntry && value!="")
				vm.resultData.dataSource.filter.push(fieldTest);
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

			for (var i = 0; i < vm.resultData.dataSource.filter.length; ++i) {
				var filter = vm.resultData.dataSource.filter[i];

				if (filter.field==vm.valueField && filter.valueFrom && value!="") {
					foundEntry = true;
					filter.valueFrom = valueFrom;
					break;
				}
				else if (filter.field==vm.valueField && filter.valueFrom && value=="")
					vm.resultData.dataSource.filter.splice(i, 1);
			}

			if (!foundEntry && value!="")
				vm.resultData.dataSource.filter.push(fieldTest);
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

			for (var i = 0; i < vm.resultData.dataSource.filter.length; ++i) {
				var filter = vm.resultData.dataSource.filter[i];

				if (filter.field==vm.valueField && filter.valueTo && value!="") {
					foundEntry = true;
					filter.valueTo = valueTo;
					break;
				}
				else if (filter.field==vm.valueField && filter.valueTo && value=="")
					vm.resultData.dataSource.filter.splice(i, 1);
			}

			if (!foundEntry && value!="")
				vm.resultData.dataSource.filter.push(fieldTest);

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

		restrictionChange(value : any) {
			var vm = this;

			if (!value || vm.restrictionFieldName=="" || vm.restrictionOrderDirection=="") {
				vm.resultData.dataSource.restriction = null;
				return;
			}

			var restriction : Restriction = {
				fieldName: vm.restrictionFieldName,
				orderDirection: vm.restrictionOrderDirection,
				count: Number(vm.restrictionCount)
			};

			vm.resultData.dataSource.restriction = restriction;
		}

		toggleRestriction() {
			var vm = this;

			vm.showRestriction = !vm.showRestriction;
		};

		save() {
			var vm = this;

			if (!vm.dataSourceOnly) {
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

	angular
		.module('app.dialogs')
		.controller('TestEditorController', TestEditorController);
}

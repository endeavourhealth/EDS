/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />

module app.dialogs {
	import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
	import IModalService = angular.ui.bootstrap.IModalService;
	import ExclusionTreeNode = app.models.ExclusionTreeNode;
	import ICodingService = app.core.ICodingService;
	import CodeSetValue = app.models.CodeSetValue;
	import Concept = app.models.Concept;

	'use strict';

	export class CodePickerController extends BaseDialogController {
		highlightedMatch : CodeSetValue;
		previousSelection : CodeSetValue;
		highlightedSelection : CodeSetValue;

		searchData : string;
		searchResults : CodeSetValue[];
		parents : CodeSetValue[];
		children : CodeSetValue[];

		termCache : any;

		exclusionTreeData : ExclusionTreeNode[];

		public static open($modal : IModalService, selection : CodeSetValue[]) : IModalServiceInstance {
			var options : IModalSettings = {
				templateUrl:'app/dialogs/codePicker/codePicker.html',
				controller:'CodePickerController',
				controllerAs:'codePicker',
				size:'lg',
				backdrop: 'static',
				resolve:{
					selection : () => selection
				}
			};

			var dialog = $modal.open(options);
			return dialog;
		}

		static $inject = ['$uibModalInstance', 'LoggerService', 'CodingService', 'selection'];

		constructor(protected $uibModalInstance : IModalServiceInstance,
								private logger:app.blocks.ILoggerService,
								private codingService : ICodingService,
								private selection : CodeSetValue[]) {
			super($uibModalInstance);
			this.termCache = {};
			this.resultData = jQuery.extend(true, [], selection);
		}

		search() {
			var vm = this;
			//vm.searchResults = vm.termlexSearch.getFindings(vm.searchData, vm.searchOptions);
			vm.codingService.searchCodes(vm.searchData)
				.then(function(result:CodeSetValue[]) {
					vm.searchResults = result;
					vm.parents = [];
					vm.children = [];
				});
		}

		displayCode(itemToDisplay : CodeSetValue, replace : boolean) {
			var vm = this;

			if (vm.highlightedMatch) {
				vm.previousSelection = vm.highlightedMatch;
			}

			if (replace) {
				vm.searchResults = [itemToDisplay];
			}

			vm.codingService.getCodeChildren(itemToDisplay.code)
				.then(function(result:CodeSetValue[]) {
					vm.children = result;
				});

			vm.codingService.getCodeParents(itemToDisplay.code)
				.then(function(result:CodeSetValue[]) {
					vm.parents = result;
				});

			vm.highlightedMatch = itemToDisplay;
		}

		addToSelection(match : CodeSetValue) {
			var item : CodeSetValue = {
				code : match.code,
				includeChildren : true,
				exclusion : []
			};
			this.resultData.push(item);
		}

		removeFromSelection(item : CodeSetValue) {
			var i = this.resultData.indexOf(item);
			if (i !== -1) {
				this.resultData.splice(i, 1);
			}
		}

		displayExclusionTree(selection : CodeSetValue) {
			var vm = this;
			vm.highlightedSelection = selection;

			vm.codingService.getCodeChildren(selection.code)
				.then(function(result:CodeSetValue[]) {
					var rootNode : ExclusionTreeNode = {
						codeSetValue : selection,
						children : []
					} as ExclusionTreeNode;

					result.forEach((child) => {
						// If "includeChildren" is ticked
						if (selection.includeChildren) {
							// and no "excludes" then tick
							if ((!selection.exclusion) || selection.exclusion.length === 0) {
								child.includeChildren = true;
							} else {
								// else if this is not excluded then tick
								child.includeChildren = selection.exclusion.every((exclusion) => {
									return exclusion.code !== child.code;
								});
							}
						}

						var childNode : ExclusionTreeNode = {
							codeSetValue : child
						} as ExclusionTreeNode;

						rootNode.children.push(childNode);
					});

					vm.exclusionTreeData = [ rootNode ];
				});
		}

		tickNode(node : ExclusionTreeNode) {
			if (node.codeSetValue.code === this.highlightedSelection.code) {
				// Ticking root so empty exclusions and tick all children
				this.highlightedSelection.exclusion = [];
				this.highlightedSelection.includeChildren = true;
				node.children.forEach((item) => { item.codeSetValue.includeChildren = true; });
			} else {
				if (this.highlightedSelection.includeChildren) {
					// Ticking an excluded child so find the exclusion...
					var index = this.findWithAttr(this.highlightedSelection.exclusion, 'code', node.codeSetValue.code);
					if (index > -1) {
						// ...remove it...
						this.highlightedSelection.exclusion.splice(index, 1);
						// ...tick it...
						node.codeSetValue.includeChildren = true;
						// ...and if no exclusions are left then set as "include all" at root
						if (this.highlightedSelection.exclusion.length === 0) {
							this.highlightedSelection.includeChildren = true;
						}
					}
				} else {
					// Ticking a child on "DONT include children" so tick root...
					this.highlightedSelection.includeChildren = true;
					// ...tick the node...
					node.codeSetValue.includeChildren = true;
					// ...and add the rest as exclusions
					this.highlightedSelection.exclusion = [];
					this.exclusionTreeData[0].children.forEach((childNode) => {
						if (childNode !== node) {
							this.highlightedSelection.exclusion.push(childNode.codeSetValue);
						}
					});
				}
			}
		}

		untickNode(node : ExclusionTreeNode) {
			if (node.codeSetValue.code === this.highlightedSelection.code) {
				// Unticking root so untick all children...
				node.children.forEach((item) => { item.codeSetValue.includeChildren = false; });
				// ... and clear exclusions list
				this.highlightedSelection.exclusion = [];
			} else {
				// Unticking a child so...
				if (this.highlightedSelection.exclusion == null) {
					// Initialize exclusion array if required
					this.highlightedSelection.exclusion = [];
				}
				// ...add exclusion
				this.highlightedSelection.exclusion.push(node.codeSetValue);
			}
			// Untick the node
			node.codeSetValue.includeChildren = false;
		}

		findWithAttr(array : any[], attr : string, value : string) : number {
			for (var i = 0; i < array.length; i += 1) {
				if (array[i][attr] === value) {
					return i;
				}
			}
			return -1;
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
		.controller('CodePickerController', CodePickerController);
}

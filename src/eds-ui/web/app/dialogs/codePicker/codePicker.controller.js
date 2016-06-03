/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var dialogs;
    (function (dialogs) {
        'use strict';
        var CodePickerController = (function (_super) {
            __extends(CodePickerController, _super);
            function CodePickerController($uibModalInstance, logger, codingService, selection) {
                _super.call(this, $uibModalInstance);
                this.$uibModalInstance = $uibModalInstance;
                this.logger = logger;
                this.codingService = codingService;
                this.selection = selection;
                this.termCache = {};
                this.resultData = jQuery.extend(true, [], selection);
            }
            CodePickerController.open = function ($modal, selection) {
                var options = {
                    templateUrl: 'app/dialogs/codePicker/codePicker.html',
                    controller: 'CodePickerController',
                    controllerAs: 'codePicker',
                    size: 'lg',
                    backdrop: 'static',
                    resolve: {
                        selection: function () { return selection; }
                    }
                };
                var dialog = $modal.open(options);
                return dialog;
            };
            CodePickerController.prototype.search = function () {
                var vm = this;
                //vm.searchResults = vm.termlexSearch.getFindings(vm.searchData, vm.searchOptions);
                vm.codingService.searchCodes(vm.searchData)
                    .then(function (result) {
                    vm.searchResults = result;
                    vm.parents = [];
                    vm.children = [];
                });
            };
            CodePickerController.prototype.displayCode = function (itemToDisplay, replace) {
                var vm = this;
                if (vm.highlightedMatch) {
                    vm.previousSelection = vm.highlightedMatch;
                }
                if (replace) {
                    vm.searchResults = [itemToDisplay];
                }
                vm.codingService.getCodeChildren(itemToDisplay.code)
                    .then(function (result) {
                    vm.children = result;
                });
                vm.codingService.getCodeParents(itemToDisplay.code)
                    .then(function (result) {
                    vm.parents = result;
                });
                vm.highlightedMatch = itemToDisplay;
            };
            CodePickerController.prototype.addToSelection = function (match) {
                var item = {
                    code: match.code,
                    includeChildren: true,
                    exclusion: []
                };
                this.resultData.push(item);
            };
            CodePickerController.prototype.removeFromSelection = function (item) {
                var i = this.resultData.indexOf(item);
                if (i !== -1) {
                    this.resultData.splice(i, 1);
                }
            };
            CodePickerController.prototype.displayExclusionTree = function (selection) {
                var vm = this;
                vm.highlightedSelection = selection;
                vm.codingService.getCodeChildren(selection.code)
                    .then(function (result) {
                    var rootNode = {
                        codeSetValue: selection,
                        children: []
                    };
                    result.forEach(function (child) {
                        // If "includeChildren" is ticked
                        if (selection.includeChildren) {
                            // and no "excludes" then tick
                            if ((!selection.exclusion) || selection.exclusion.length === 0) {
                                child.includeChildren = true;
                            }
                            else {
                                // else if this is not excluded then tick
                                child.includeChildren = selection.exclusion.every(function (exclusion) {
                                    return exclusion.code !== child.code;
                                });
                            }
                        }
                        var childNode = {
                            codeSetValue: child
                        };
                        rootNode.children.push(childNode);
                    });
                    vm.exclusionTreeData = [rootNode];
                });
            };
            CodePickerController.prototype.tickNode = function (node) {
                var _this = this;
                if (node.codeSetValue.code === this.highlightedSelection.code) {
                    // Ticking root so empty exclusions and tick all children
                    this.highlightedSelection.exclusion = [];
                    this.highlightedSelection.includeChildren = true;
                    node.children.forEach(function (item) { item.codeSetValue.includeChildren = true; });
                }
                else {
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
                    }
                    else {
                        // Ticking a child on "DONT include children" so tick root...
                        this.highlightedSelection.includeChildren = true;
                        // ...tick the node...
                        node.codeSetValue.includeChildren = true;
                        // ...and add the rest as exclusions
                        this.highlightedSelection.exclusion = [];
                        this.exclusionTreeData[0].children.forEach(function (childNode) {
                            if (childNode !== node) {
                                _this.highlightedSelection.exclusion.push(childNode.codeSetValue);
                            }
                        });
                    }
                }
            };
            CodePickerController.prototype.untickNode = function (node) {
                if (node.codeSetValue.code === this.highlightedSelection.code) {
                    // Unticking root so untick all children...
                    node.children.forEach(function (item) { item.codeSetValue.includeChildren = false; });
                    // ... and clear exclusions list
                    this.highlightedSelection.exclusion = [];
                }
                else {
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
            };
            CodePickerController.prototype.findWithAttr = function (array, attr, value) {
                for (var i = 0; i < array.length; i += 1) {
                    if (array[i][attr] === value) {
                        return i;
                    }
                }
                return -1;
            };
            CodePickerController.prototype.termShorten = function (term) {
                term = term.replace(' (disorder)', '');
                term = term.replace(' (observable entity)', '');
                term = term.replace(' (finding)', '');
                return term;
            };
            CodePickerController.prototype.getTerm = function (code) {
                var vm = this;
                var term = vm.termCache[code];
                if (term) {
                    return term;
                }
                vm.termCache[code] = 'Loading...';
                vm.codingService.getPreferredTerm(code)
                    .then(function (concept) {
                    vm.termCache[code] = vm.termShorten(concept.preferredTerm);
                });
                return vm.termCache[code];
            };
            CodePickerController.$inject = ['$uibModalInstance', 'LoggerService', 'CodingService', 'selection'];
            return CodePickerController;
        })(dialogs.BaseDialogController);
        dialogs.CodePickerController = CodePickerController;
        angular
            .module('app.dialogs')
            .controller('CodePickerController', CodePickerController);
    })(dialogs = app.dialogs || (app.dialogs = {}));
})(app || (app = {}));
//# sourceMappingURL=codePicker.controller.js.map
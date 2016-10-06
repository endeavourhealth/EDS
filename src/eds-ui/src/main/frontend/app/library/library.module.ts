import {LibraryController} from "./library.controller";
import {LibraryRoute} from "./library.route";

angular.module('app.library', ['ui.bootstrap', 'ui.tree'])
	.controller('LibraryController', LibraryController)
	.config(LibraryRoute)
	.run(["$templateCache",
	($templateCache: ng.ITemplateCacheService) => {
		$templateCache.put("libraryItemFolder.html", require("../common/libraryItemFolder.html"));
	}]);
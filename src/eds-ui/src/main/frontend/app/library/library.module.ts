import {LibraryController} from "./library.controller";
import {LibraryRoute} from "./library.route";
import {FolderComponent} from "./folderComponent/folderComponent.component";
import {FolderComponentController} from "./folderComponent/folderComponent.controller";

angular.module('app.library', ['ui.bootstrap', 'ui.tree'])
	.controller('LibraryController', LibraryController)
	.controller('FolderComponentController', FolderComponentController)
	.component('libraryFolder', new FolderComponent())
	.config(LibraryRoute);
import {itemTypeIdToString, itemTypeIdToIcon} from "./ItemType";

angular.module('app.filters', [])
	.filter('itemTypeIdToString', itemTypeIdToString)
	.filter('itemTypeIdToIcon', itemTypeIdToIcon);

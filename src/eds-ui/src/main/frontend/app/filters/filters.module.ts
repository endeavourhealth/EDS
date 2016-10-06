import {cuiGender, cuiSingleLineAddress, cuiName, cuiNhsNumber, cuiDateOfBirth, cuiDate} from "./cui";
import {itemTypeIdToString, itemTypeIdToIcon} from "./ItemType";
import {codeTerm} from "./coding";

angular.module('app.filters', ['angularMoment'])
	.filter('cuiDate', cuiDate)
	.filter('cuiDateOfBirth', cuiDateOfBirth)
	.filter('cuiNhsNumber', cuiNhsNumber)
	.filter('cuiName', cuiName)
	.filter('cuiSingleLineAddress', cuiSingleLineAddress)
	.filter('cuiGender', cuiGender)
	.filter('itemTypeIdToString', itemTypeIdToString)
	.filter('itemTypeIdToIcon', itemTypeIdToIcon)
	.filter('codeTerm', codeTerm);

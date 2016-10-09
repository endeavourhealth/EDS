import {RecordViewerController} from "./recordViewer.controller";
import {RecordViewerService} from "./recordViewer.service";
import {RecordViewerRoute} from "./recordViewer.route";
import {codeSignificance, codeTerm} from "./filters/coding";
import {cuiSingleLineAddress, cuiGender, cuiNhsNumber, cuiDateOfBirth, cuiDate, cuiName} from "./filters/cui";

angular.module('app.recordViewer', ['angularMoment'])
	.controller('RecordViewerController', RecordViewerController)
	.service('RecordViewerService', RecordViewerService)
	.config(RecordViewerRoute)
    .filter('codeTerm', codeTerm)
    .filter('codeSignificance', codeSignificance)
    .filter('cuiDate', cuiDate)
    .filter('cuiDateOfBirth', cuiDateOfBirth)
    .filter('cuiName', cuiName)
    .filter('cuiNhsNumber', cuiNhsNumber)
    .filter('cuiSingleLineAddress', cuiSingleLineAddress)
    .filter('cuiGender', cuiGender);
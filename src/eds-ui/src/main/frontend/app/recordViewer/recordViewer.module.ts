import {RecordViewerController} from "./recordViewer.controller";
import {RecordViewerService} from "./recordViewer.service";
import {RecordViewerRoute} from "./recordViewer.route";
import {codeSignificance, codeReadCode, codeReadTerm, codeSnomedCode, codeSnomedLink} from "./filters/coding";
import {cuiSingleLineAddress, cuiGender, cuiNhsNumber, cuiDateOfBirth, cuiDate, cuiName} from "./filters/cui";
import {parentheses} from "./filters/general";

angular.module('app.recordViewer', ['angularMoment'])
	.controller('RecordViewerController', RecordViewerController)
	.service('RecordViewerService', RecordViewerService)
	.config(RecordViewerRoute)
    .filter('codeReadTerm', codeReadTerm)
    .filter('codeReadCode', codeReadCode)
    .filter('codeSnomedCode', codeSnomedCode)
    .filter('codeSnomedLink', codeSnomedLink)
    .filter('codeSignificance', codeSignificance)
    .filter('cuiDate', cuiDate)
    .filter('cuiDateOfBirth', cuiDateOfBirth)
    .filter('cuiName', cuiName)
    .filter('cuiNhsNumber', cuiNhsNumber)
    .filter('cuiSingleLineAddress', cuiSingleLineAddress)
    .filter('cuiGender', cuiGender)
    .filter('parentheses', parentheses);
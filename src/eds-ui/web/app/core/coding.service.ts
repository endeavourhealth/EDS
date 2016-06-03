/// <reference path="../../typings/tsd.d.ts" />

module app.core {
	import Concept = app.models.Concept;
	import CodeSetValue = app.models.CodeSetValue;
	'use strict';

	export interface ICodingService {
		searchCodes(searchData : string):ng.IPromise<CodeSetValue[]>;
		getCodeChildren(code : string):ng.IPromise<CodeSetValue[]>;
		getCodeParents(code : string):ng.IPromise<CodeSetValue[]>;
		getPreferredTerm(id : string):ng.IPromise<Concept>;

	}
}
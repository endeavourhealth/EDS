module app.models {
	'use strict';

	export class ListReportGroup {
		heading:string;
		summary:ListReportSummaryType;
		fieldBased:ListReportFieldBasedType;
	}

	export function listReportGroupToIcon() {
		return function (listReportGroup:ListReportGroup) {
			if (listReportGroup.summary != null) {
				return 'fa-summary';
			}
			if (listReportGroup.fieldBased != null) {
				return 'fa-fieldBased';
			}
		};
	}

	export function listReportGroupToTypeName() {
		return function (listReportGroup:ListReportGroup) {
			if (listReportGroup.summary != null) {
				return 'Summary';
			}
			if (listReportGroup.fieldBased != null) {
				return 'Field based';
			}
		};
	}

	export function listReportGroupToDescription() {
		return function (listReportGroup:ListReportGroup) {
			if (listReportGroup.summary != null) {
				return 'Summary description';
			}
			if (listReportGroup.fieldBased != null) {
				return ' (' + listReportGroup.fieldBased.fieldOutput.length + ' fields)';
			}
		};
	}

	angular
		.module('app.models')
		.filter('listReportGroupToIcon', listReportGroupToIcon)
		.filter('listReportGroupToTypeName', listReportGroupToTypeName)
		.filter('listReportGroupToDescription', listReportGroupToDescription);

}
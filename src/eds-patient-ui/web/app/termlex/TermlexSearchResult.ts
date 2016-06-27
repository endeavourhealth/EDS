module app.models {
	'use strict';

	export class TermlexSearchResultCategory {
		count: number;
		type: string;
	}

	export class TermlexSearchResultResult extends TermlexCode {
		dt: string;
		lang: string;
		matches: string[];
		status: number;
		type: string;
	}

	export class TermlexSearchResult {
		categories: TermlexSearchResultCategory[];
		results: Code[];
		searchTime: number;
		showingSuggestions: boolean;
		totalHits:number;
	}
}
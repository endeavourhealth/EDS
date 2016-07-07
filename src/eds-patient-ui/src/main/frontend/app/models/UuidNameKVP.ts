module app.models {
	'use strict';

	export class UuidNameKVP {
		uuid : string;
		name : string;

		static toAssociativeArray(items : UuidNameKVP[]) {
			var associativeArray = {};
			for (var i = 0; i < items.length; i++) {
				associativeArray[items[i].uuid] = items[i].name;
			}

			return associativeArray;
		}

		static fromAssociativeArray(associativeArray : Object) {
			var array : UuidNameKVP[] = [];
			for (var key in associativeArray) {
				if (associativeArray.hasOwnProperty(key)) {
					array.push({uuid: key, name: associativeArray[key]});
				}
			}

			return array;
		}
	}
}
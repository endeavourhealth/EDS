/// <reference path="../../typings/index.d.ts" />

module app.filters {
    import Duration = moment.Duration;
    import UIHumanName = app.models.UIHumanName;

    'use strict';

    export function cuiDate() {
        return formatCuiDate;
    }

    export function cuiDateOfBirth() {
        return formatCuiDateOfBirth;
    }

    export function cuiNhsNumber() {
        return formatNhsNumber;
    }

    export function cuiName() {
        return formatName;
    }

    function formatCuiDate(date: Date): string {
        return moment(date).format("DD-MMM-YYYY");
    }

    function formatCuiDateOfBirth(dateOfBirth: Date): string {
        let age: Duration = getDurationFromNow(dateOfBirth);
        return formatCuiDate(dateOfBirth) + ' (' + age.years() + 'y ' + age.months() + 'm)';
    }

    function getDurationFromNow(date: Date): Duration {
        return moment.duration(moment().diff(date));
    }

    function formatNhsNumber(nhsNumber: string): string {
        if (nhsNumber == null)
            return null;

        let result: string = nhsNumber.replace(" ", "");

        if (result.length != 10)
            return result;

        return result.substring(0, 3) + " " + result.substring(3, 6) + " " + result.substring(6, 10);
    }

    function formatName(name: UIHumanName): string {
        let prefix: string;
        let firstGivenName: string;
        let familyName: string;

        if (name != null) {
            prefix = name.prefix;

            if (name.givenNames != null)
                if (name.givenNames.length > 0)
                    firstGivenName = name.givenNames[0];

            familyName = name.familyName;

            if (prefix == null)
                prefix = "";
            if (firstGivenName == null)
                firstGivenName = "";
            if (familyName == null)
                familyName = "";

            prefix = titleCase(prefix.trim());
            firstGivenName = titleCase(firstGivenName.trim());
            familyName = familyName.trim().toUpperCase();
        }

        if (familyName == "")
            familyName = "UNKNOWN";

        let result: string = familyName;

        if (firstGivenName != "")
            result += ", " + firstGivenName;

        if (prefix != null)
            result += " (" + prefix + ")";

        return result;
    }

    function titleCase(text: string) {
        return text;
    }

    angular
        .module('app.filters')
        .filter('cuiDate', cuiDate)
        .filter('cuiDateOfBirth', cuiDateOfBirth)
        .filter('cuiNhsNumber', cuiNhsNumber)
        .filter('cuiName', cuiName);
}
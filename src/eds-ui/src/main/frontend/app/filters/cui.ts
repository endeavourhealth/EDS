import Duration = moment.Duration;

import {UIHumanName} from "../recordViewer/models/UIHumanName";
import {UIAddress} from "../recordViewer/models/UIAddress";
import {UIDate} from "../recordViewer/models/UIDate";

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

export function cuiSingleLineAddress() {
    return formatSingleLineAddress;
}

export function cuiGender() {
    return formatCuiGender;
}

function formatCuiDate(date: UIDate): string {
    return moment(date.date).format("DD-MMM-YYYY");
}

function formatCuiDateOfBirth(dateOfBirth: UIDate): string {
    let age: Duration = getDurationFromNow(dateOfBirth.date);
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

function formatCuiGender(genderCode: string): string {
    if (isEmpty(genderCode))
        return "";

    if (genderCode == "male")
        return "Male";
    else if (genderCode == "female")
        return "Female";
    else if (genderCode == "other")
        return "Not known";
    else if (genderCode == "unknown")
        return "Not specified";

    return "";
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

    if (prefix != "")
        result += " (" + prefix + ")";

    return result;
}

function titleCase(text: string) {
    return text;
}

function formatSingleLineAddress(address: UIAddress): string {
    if (address == null)
        return "";

    let lines: string[] = new Array<string>();

    lines.push(address.line1);
    lines.push(address.line2);
    lines.push(address.line3);
    lines.push(address.city);
    lines.push(address.district);
    lines.push(formatPostalCode(address.postalCode));

    return lines
            .filter(t => !isEmpty(t))
            .join(", ");
}

function formatPostalCode(postalCode: string): string {
    if (postalCode == null)
        return null;

    postalCode = postalCode.replace(" ", "").toUpperCase().trim();

    let regExp: RegExp = new RegExp("^([A-Z]{1,2}[0-9]{1,2}[A-Z]?)([0-9][A-Z]{2})$");
    let result: RegExpExecArray = regExp.exec(postalCode);

    if ((result == null) || (result.length != 3))
        return result.length.toString();

    return result[1] + " " + result[2];
}

function isEmpty(str: string): boolean {
    if (str == null)
        return true;

    return (str.trim().length == 0);
}

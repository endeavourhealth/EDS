/// <reference path="../../typings/index.d.ts" />

module app.filters {
    import UICodeableConcept = app.models.UICodeableConcept;

    'use strict';

    export function codeTerm() {
        return getCodeTerm;
    }

    function getCodeTerm(codeableConcept: UICodeableConcept) {
        if (codeableConcept == null)
            return "";

        for (let code of codeableConcept.codes)
            if (code.system == "http://snomed.info/sct")
                return code.display;

        return "no snomed term";
    }

    angular
        .module('app.filters')
        .filter('codeTerm', codeTerm);
}
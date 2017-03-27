import moment = require("moment");
import {Component, Input, OnInit} from "@angular/core";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CodePickerDialog} from "../coding/codePicker.dialog";
import {CodeSetValue} from "../coding/models/CodeSetValue";
import {CodingService} from "../coding/coding.service";
import {Concept} from "../coding/models/Concept";
import {CountReportService} from "./countReport.service";
import {Practitioner} from "../practitioner/models/Practitioner";
import {PractitionerPickerDialog} from "../practitioner/practitionerPicker.dialog";
import {CountReport} from "./models/CountReport";
import {OrgRole} from "../layout/models/OrgRole";
import {SecurityService} from "../security/security.service";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./reportParams.html')
})
export class ReportParamsDialog implements OnInit {
    @Input() countReport : CountReport;

		organisation : OrgRole;
		userOrganisations : OrgRole[];
		encounterTypes : Concept[];
    referralTypes : Concept[];
    referralPriorities : Concept[];

    runDate : Date;
    includeDeceased : boolean = false;
    organisationUuid : string;
    effectiveDate : Date;
    regType : number;
    gender : number;
    dobMin : Date;
    dobMax : Date;
    originalCode : string;
    valueMax : number;
    valueMin : number;
    snomedCode : CodeSetValue;
    authType : number;
    practitioner : Practitioner;
    dmdCode : number;
    encounterType : number;
    referralSnomedCode : CodeSetValue;
    referralOriginalCode : string;
    referralType : number;
    referralPriority : number;


    public static open(modalService: NgbModal, countReport: CountReport) {
        const modalRef = modalService.open(ReportParamsDialog, {backdrop: "static", size: 'lg'});
        modalRef.componentInstance.countReport = countReport;
        return modalRef;
    }

    constructor(protected modalService : NgbModal,
                protected activeModal: NgbActiveModal,
                protected codingService : CodingService,
                protected securityService : SecurityService,
                protected countReportService : CountReportService) {
        this.loadEncounterTypes();
        this.loadReferralTypes();
        this.loadReferralPriorities();
        this.loadUserOrganisations();
    }

    ngOnInit(): void {
        // work out prompts from query text
        this.runDate = new Date();

        if (!this.countReport)
            return;

        // Check query for remaining prompts
        if (this.countReport.query.indexOf(':EffectiveDate') >= 0) this.effectiveDate = null;
        if (this.countReport.query.indexOf(':SnomedCode') >= 0) this.snomedCode = null;
        if (this.countReport.query.indexOf(':RegistrationType') >= 0) this.regType = null;
        if (this.countReport.query.indexOf(':Gender') >= 0) this.gender = null;
        if (this.countReport.query.indexOf(':DobMin') >= 0) this.dobMin = null;
        if (this.countReport.query.indexOf(':DobMax') >= 0) this.dobMax = null;
        if (this.countReport.query.indexOf(':OriginalCode') >= 0) this.originalCode = null;
        if (this.countReport.query.indexOf(':ValueMin') >= 0) this.valueMin = null;
        if (this.countReport.query.indexOf(':ValueMax') >= 0) this.valueMax = null;
        if (this.countReport.query.indexOf(':AuthType') >= 0) this.authType = null;
        if (this.countReport.query.indexOf(':Practitioner') >= 0) this.practitioner = null;
        // DM&D
        if (this.countReport.query.indexOf(':EncounterType') >= 0) this.encounterType = null;
        if (this.countReport.query.indexOf(':ReferralSnomedCode') >= 0) this.referralSnomedCode = null;
        if (this.countReport.query.indexOf(':ReferralOriginalCode') >= 0) this.referralOriginalCode = null;
        if (this.countReport.query.indexOf(':ReferralType') >= 0) this.referralType = null;
        if (this.countReport.query.indexOf(':ReferralPriority') >=0 ) this.referralPriority = null;
    }

    getDeceasedText() {
        return this.includeDeceased ? 'Include' : 'Exclude';
    }

    loadEncounterTypes() {
        let vm = this;
        vm.countReportService.getEncounterTypeCodes()
          .subscribe(
            (result) => vm.encounterTypes = result
          );
    }

    loadReferralTypes() {
        let vm = this;
        vm.countReportService.getReferralTypes()
					.subscribe(
            (result) => vm.referralTypes = result
          );
    }

    loadReferralPriorities() {
        let vm = this;
        vm.countReportService.getReferralPriorities()
					.subscribe(
            (result) => vm.referralPriorities = result
          );
    }

    selectSnomed() {
        var vm = this;
        CodePickerDialog.open(vm.modalService, [], true)
          .result.then(
          (result) => {
              if (result) {
                  vm.snomedCode = result[0];
                  vm.snomedCode.term = 'Loading...';
                  vm.codingService.getPreferredTerm(vm.snomedCode.code)
										.subscribe(
                      (term) => vm.snomedCode.term = term.preferredTerm
                    );
              }
          }
        )
    }

    clearSnomed() {
        this.snomedCode = null;
    }

    selectReferralSnomed() {
        var vm = this;
        CodePickerDialog.open(vm.modalService, [], true, {code : '3457005', term : 'Patient referral'} as CodeSetValue)
					.result.then(
          (result) => {
              vm.referralSnomedCode = result[0];
              vm.referralSnomedCode.term = 'Loading...';
              vm.codingService.getPreferredTerm(vm.referralSnomedCode.code)
								.subscribe(
                  (term) => vm.referralSnomedCode.term = term.preferredTerm
                );
          }
        )
    }

    clearReferralSnomed() {
        this.referralSnomedCode = null;
    }


    selectPractitioner() {
        var vm = this;
        PractitionerPickerDialog.open(vm.modalService)
          .result.then(
          (result) => vm.practitioner = result
        );
    }

    clearPractitioner() {
        this.practitioner = null;
    }

    setEffectiveDate($event) {
        if ($event)
            this.effectiveDate = $event;
    }

    setDobMin($event) {
        if ($event)
            this.dobMin = $event;
    }

    setDobMax($event) {
        if ($event)
            this.dobMax = $event;
    }

    hide(item : any) {
        return item === undefined;
    }

	loadUserOrganisations() {
		let vm = this;
		if (!vm.userOrganisations) {

			vm.userOrganisations = [];
			for(let orgGroup of vm.securityService.getCurrentUser().organisationGroups) {
				let orgRole = new OrgRole(orgGroup.organisationId, 'Loading...');
				vm.countReportService.getServiceName(orgRole.id)
					.subscribe(
						(result) => {
							if (result != null && result != '') {
								orgRole.name = result;
								vm.userOrganisations.push(orgRole);
							}
						}
					);
			}
		}
		return vm.userOrganisations;
	}

    ok() {
        let params : any = {};

        params.RunDate = "'" + moment(this.runDate).format('DD/MM/YYYY') + "'";
				params.OrganisationUuid = this.organisation;
				params.DateOfDeath = (this.includeDeceased) ? 'null' : "'" + moment(this.runDate).format('DD/MM/YYYY') + "'";
        params.EffectiveDate = (this.effectiveDate) ? "'" + moment(this.effectiveDate).format('DD/MM/YYYY') + "'" : 'null';
        params.RegistrationType = (this.regType) ? this.regType : 'null';
        params.Gender = (this.gender) ? this.gender : 'null';
        params.DobMin = (this.dobMin) ? "'" + moment(this.dobMin).format('DD/MM/YYYY') + "'" : 'null';
        params.DobMax = (this.dobMax) ? "'" + moment(this.dobMax).format('DD/MM/YYYY') + "'" : 'null';
        params.SnomedCode = (this.snomedCode) ? this.snomedCode.code : 'null';
        params.OriginalCode = (this.originalCode) ? "'" + this.originalCode + "'" : 'null';
        params.ValueMin = (this.valueMin) ? this.valueMin : 'null';
        params.ValueMax = (this.valueMax) ? this.valueMax  : 'null';
        params.AuthType = (this.authType) ? this.authType : 'null';
        params.Practitioner = (this.practitioner) ? this.practitioner.id : 'null';
        // DM & D
        params.EncounterType = (this.encounterType) ? this.encounterType : 'null';
        params.ReferralSnomedCode = (this.referralSnomedCode) ? this.referralSnomedCode.code : 'null';
        params.ReferralOriginalCode = (this.referralOriginalCode) ? "'" + this.referralOriginalCode + "'" : 'null';
        params.ReferralType = (this.referralType) ? this.referralType : 'null';
        params.ReferralPriority = (this.referralPriority) ? this.referralPriority : 'null';

        this.activeModal.close(params);
        console.log('OK Pressed');
    }

    cancel() {
        this.activeModal.close(null);
        console.log('Cancel Pressed');
    }
}

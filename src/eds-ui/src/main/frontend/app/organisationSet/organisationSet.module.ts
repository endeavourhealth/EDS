import {OrganisationSetController} from "./organisationSetController";
import {OrganisationSetRoute} from "./organisationSet.route";

angular.module('app.organisationSet', [])
	.controller('OrganisationSetController', OrganisationSetController)
	.config(OrganisationSetRoute);
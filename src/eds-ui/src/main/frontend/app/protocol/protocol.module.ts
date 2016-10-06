import {ProtocolController} from "./protocol.controller";
import {protocolRoute} from "./protocol.route";

angular.module('app.protocol', [])
	.controller('ProtocolController', ProtocolController)
	.config(protocolRoute);
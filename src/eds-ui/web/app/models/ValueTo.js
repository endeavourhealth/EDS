var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var models;
    (function (models) {
        'use strict';
        var ValueTo = (function (_super) {
            __extends(ValueTo, _super);
            function ValueTo() {
                _super.apply(this, arguments);
            }
            return ValueTo;
        })(models.Value);
        models.ValueTo = ValueTo;
    })(models = app.models || (app.models = {}));
})(app || (app = {}));
//# sourceMappingURL=ValueTo.js.map
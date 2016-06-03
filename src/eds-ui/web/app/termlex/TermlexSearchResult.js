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
        var TermlexSearchResultCategory = (function () {
            function TermlexSearchResultCategory() {
            }
            return TermlexSearchResultCategory;
        })();
        models.TermlexSearchResultCategory = TermlexSearchResultCategory;
        var TermlexSearchResultResult = (function (_super) {
            __extends(TermlexSearchResultResult, _super);
            function TermlexSearchResultResult() {
                _super.apply(this, arguments);
            }
            return TermlexSearchResultResult;
        })(models.TermlexCode);
        models.TermlexSearchResultResult = TermlexSearchResultResult;
        var TermlexSearchResult = (function () {
            function TermlexSearchResult() {
            }
            return TermlexSearchResult;
        })();
        models.TermlexSearchResult = TermlexSearchResult;
    })(models = app.models || (app.models = {}));
})(app || (app = {}));
//# sourceMappingURL=TermlexSearchResult.js.map
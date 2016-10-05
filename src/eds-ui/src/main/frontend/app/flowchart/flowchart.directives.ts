module app.flowchart {
	export class FlowChartDirective implements ng.IDirective {
		static factory() {
			return () => {
				{
					return {
						restrict: 'E',
						templateUrl: "app/flowchart/flowchart.template.html",
						replace: true,
						scope: {
							chart: "=chart",
						},
						controller: 'FlowChartController',
					};
				}
			}
		}
	}

	export class ChartJSonEdit implements ng.IDirective {
		static factory() {
			return () => {
				{
					return {
						restrict: 'A',
						scope: {
							viewModel: "="
						},
						link: function (scope: any, elem: any, attr: any) {

							//
							// Serialize the data model as json and update the textarea.
							//
							var updateJson = function () {
								if (scope.viewModel) {
									var obj = scope.viewModel.data;
									obj = removeAllNull(obj);

									var json = JSON.stringify(obj, null, 4);
									$(elem).val(json);
								}
							};

							function removeAllNull(JsonObj: any) {
								$.each(JsonObj, function (key, value) {
									if (value === null) {
										delete JsonObj[key];
									} else if (typeof(value) === "object") {
										//JsonObj[key] = removeAllNull(value);
									}
								});
								return JsonObj;
							}

							//
							// First up, set the initial value of the textarea.
							//
							updateJson();

							//
							// Watch for changes in the data model and update the textarea whenever necessary.
							//
							scope.$watch("viewModel.data", updateJson, true);

							//
							// Handle the change event from the textarea and update the data model
							// from the modified json.
							//
							$(elem).bind("input propertychange", function () {
								var json = $(elem).val();
								var dataModel = JSON.parse(json);
								scope.viewModel = new flowchart.ChartViewModel(dataModel);

								scope.$apply();
							});
						}
					}
				}
			}
		}
	}
}
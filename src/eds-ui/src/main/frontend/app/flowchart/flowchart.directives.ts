export class FlowChartDirective implements ng.IDirective {
	static factory() {
		return () => {
			{
				return {
					restrict: 'E',
					template: require("./flowchart.template.html"),
					replace: true,
					scope: {
						chart: "=chart",
					},
					controller: 'FlowChartController',
					controllerAs : 'ctrl'
				};
			}
		}
	}
}

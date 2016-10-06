export class MouseCaptureDirective implements ng.IDirective {
	static factory() {
		return () => {
			{
				return {
					restrict: 'A',

					controller: ['$scope', '$element', '$attrs', 'mouseCapture',
						function ($scope, $element, $attrs, mouseCapture) {

							//
							// Register the directives element as the mouse capture element.
							//
							mouseCapture.registerElement($element);

						}],

				};
			}
		}
	}
}

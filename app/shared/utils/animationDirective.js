app.directive('animateOnChange', function ($animate, $timeout) {
    return function ($scope, elem, attrs) {
        $scope.$watch(attrs.animateOnChange, function(nv, ov) {
            if (nv !== ov && nv !== undefined) {
                var c = attrs.animateClass;
                $animate.addClass(elem, c).then(function() {
                   $timeout(function() {
                       $animate.removeClass(elem, c);
                   });
                });
            }
        });
    };
});

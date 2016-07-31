app.controller('HomeController', function ($scope, Releases, Javadoc) {

    $scope.actual = 0;

    Releases.success(function(data) {
        $scope.releases = data;
        $scope.actual = $scope.releases.length-1;
    });

    $scope.hasNext = function() {
        return $scope.actual > 0;
    };

    $scope.next = function() {
        if ($scope.hasNext()) {
            $scope.actual -= 1;
        }
    };

    $scope.hasBefore = function() {
        if ($scope.releases !== undefined) {
            return $scope.actual < $scope.releases.length-1;
        }
    };

    $scope.before = function() {
        if ($scope.hasBefore()) {
            $scope.actual += 1;
        }
    };

    $scope.$watch('actual', function (newValue, oldValue) {
        if ($scope.releases !== undefined) {
            $scope.release = $scope.releases[$scope.actual];
        }
    });

    $scope.hasJavadoc = function(v) {
        return Javadoc.check(v);
    };

});

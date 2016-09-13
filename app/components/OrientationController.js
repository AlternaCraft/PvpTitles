app.controller('OrientationController', function ($scope, $window, matchmedia) {
    $scope.isLandscape = function () {
        return matchmedia.isLandscape();
    };

    $scope.isPhone = function () {
        return matchmedia.isPhone();
    };

    matchmedia.onLandscape(function () {
        $scope.valid = ($scope.isPhone()) ? $scope.isLandscape() : true;
    });
});

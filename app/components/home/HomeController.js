app.controller('HomeController', function ($scope, Releases, Javadoc) {
    $scope.releasesName = [];

    $scope.releases = undefined;
    $scope.actualR = -1;

    $scope.blocks = undefined;
    $scope.actualB = -1;

    Releases.releases().success(function (data) {
        $scope.releases = data;
        for (var i = 0; i < $scope.releases.length; i++) {
            $scope.releasesName.push($scope.releases[i].tag_name);
        }
        $scope.actualR = 0;

        $scope.show = 3;
    });

    $scope.hasJavadoc = function (v) {
        return Javadoc.check(v);
    };

    $scope.optionSelected = function (v) {
        for (var i = 0; i < $scope.releases.length; i++) {
            if ($scope.releases[i].tag_name === v) {
                $scope.actualR = i;
                $scope.actualB = Math.floor(i/$scope.show);
                break;
            }
        }
    };

    $scope.hasNext = function () {
        if ($scope.releases !== undefined) {
            return $scope.actualB < $scope.blocks.length - 1;
        }
    };

    $scope.hasBefore = function () {
        return $scope.actualB > 0;
    };

    $scope.next = function () {
        if ($scope.hasNext()) {
            $scope.actualB += 1;
        }
    };

    $scope.before = function () {
        if ($scope.hasBefore()) {
            $scope.actualB -= 1;
        }
    };

    $scope.$watch('show', function (newValue, oldValue) {
        if ($scope.releases !== undefined) {
            $scope.blocks = Releases.paginator($scope.releases, $scope.show);
            $scope.actualB = Math.floor($scope.actualR/$scope.show);
            $scope.block = $scope.blocks[$scope.actualB];
        }
    });

    $scope.$watch('actualR', function (newValue, oldValue) {
        if ($scope.releases !== undefined) {
            $scope.release = $scope.releases[$scope.actualR];
            $scope.release.class = "active";

            if (oldValue != newValue && $scope.releases[oldValue] !== undefined) {
                $scope.releases[oldValue].class = "";
            }
        }
    });

    $scope.$watch('actualB', function (newValue, oldValue) {
        if ($scope.blocks !== undefined) {
            $scope.block = $scope.blocks[$scope.actualB];
        }
    });
});

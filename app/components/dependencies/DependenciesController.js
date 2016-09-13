var entro = [];

app.controller('DependenciesController', function ($scope, Dependencies, $routeParams) {
    $scope.dependencies = undefined;
    $scope.animated = "item";

    if (entro[$routeParams.id] === undefined) {
        entro[$routeParams.id] = false;
    }

    Dependencies.getDependencies($routeParams.id).success(function (data) {
        $scope.dependencies = data;

        if (!entro[$routeParams.id]) {
            entro[$routeParams.id] = true;
        } else {
            $scope.animated = "";
        }
    });

    $scope.$watch('valid', function (nv, ov) {
        if (!ov && nv && $scope.animated !== "") {
            Dependencies.getDependencies($routeParams.id).success(function (data) {
                $scope.dependencies = data;
            });
        }
    });
});

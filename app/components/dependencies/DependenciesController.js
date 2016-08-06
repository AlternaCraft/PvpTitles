app.controller('DependenciesController', function ($scope, Dependencies, $routeParams) {
    $scope.dependencies = undefined;

    Dependencies.getDependencies($routeParams.id).success(function (data) {
        $scope.dependencies = data;
    });
});

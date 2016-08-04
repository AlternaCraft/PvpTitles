app.controller('DependenciesController', function ($scope, Dependencies) {

    $scope.dependencies = {};
    $scope.changes = {};

    Dependencies.getDependencies().success(function(data) {
        $scope.dependencies = data;
    });

    Dependencies.getUpdates().success(function(data) {
        $scope.changes = data;
    });

    $scope.hasNewVersion = function(dep) {
        for (var i = 0; i < $scope.changes.length; i++) {
            if ($scope.changes[i].repository === (dep.groupId + ':' + dep.artifactId)) {
                dep.class = "danger";
                return $scope.changes[i].newv;
            }
        }
        dep.class = "success";
        return "-";
    };
});

app.factory('Dependencies', function ($http) {
    var dep = {};

    dep.getDependencies = function () {
        return $http.get("./dependencies/dependencies.json")
            .success(function (data) {
                return data;
            })
            .error(function (error) {
                return error;
            })
    };

    dep.getUpdates = function () {
        return $http.get("./dependencies/changes.json")
            .success(function (data) {
                return data;
            })
            .error(function (error) {
                return error;
            })
    };

    return dep;
})

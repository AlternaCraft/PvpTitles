app.factory('Dependencies', function ($http) {
    var dep = {};

    dep.getDependencies = function (v) {
        return $http.get("/PvpTitles/dependencies/" + v + "/dependencies.json")
            .success(function (data) {
                return data;
            })
            .error(function (error) {
                return error;
            })
    };

    return dep;
})

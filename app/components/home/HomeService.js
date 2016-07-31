app.factory('Releases', function ($http) {
        return $http.get("https://api.github.com/repos/AlternaCraft/PvpTitles/releases")
            .success(function (data) {
                return data;
            })
            .error(function (error) {
                return error;
            });
    })
    .factory('Dependencies', function ($http) {
    return $http.get("./dependencies/dependencies-latest/dependencies.json")
            .success(function (data) {
                return data;
            })
            .error(function (error) {
                return error;
            });
    })
    .factory('Javadoc', function () {
        var javadoc = {};

        javadoc.check = function(v) {
            var http = new XMLHttpRequest();
            var url = '/javadoc/' + v;

            http.open('HEAD', url, false);
            http.send();

            return http.status != 404;
        };

        return javadoc;
    })

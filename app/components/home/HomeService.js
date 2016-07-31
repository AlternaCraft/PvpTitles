app.factory('Releases', function ($http) {
    return $http.get("https://api.github.com/repos/AlternaCraft/PvpTitles/releases")
        .success(function (data) {
            return data;
        })
        .error(function (error) {
            return error;
        });
});

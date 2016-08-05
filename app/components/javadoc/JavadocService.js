app.factory('Javadoc', function ($location) {
    var javadoc = {};

    javadoc.check = function(v) {
        var http = new XMLHttpRequest();
        var url = "/PvpTitles/javadoc/" + v + "/";

        http.open('HEAD', url, false);
        http.send();

        return http.status != 404;
    };

    return javadoc;
});

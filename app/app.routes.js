app.config(function ($routeProvider) {
    $routeProvider
        .when("/", {
            controller: "HomeController",
            templateUrl: "app/components/home/HomeView.html"
        }).when("/jdoc/:id", {
            controller: "JavadocController",
            templateUrl: "app/components/javadoc/JavadocView.html"
        }).when("/dependencies", {
            controller: "DependenciesController",
            templateUrl: "app/components/dependencies/DependenciesView.html"
        }).when("/404", {
            templateUrl: "app/components/404.html"
        })
        .otherwise("/404");
});

app.config(function ($routeProvider) {
    $routeProvider
        .when("/", {
            controller: "HomeController",
            templateUrl: "app/components/home/HomeView.html"
        }).when("/javadoc/:id", {
            controller: "JavadocController",
            templateUrl: "app/components/javadoc/JavadocView.html"
        }).when("/dependencies", {
            controller: "DependenciesController",
            templateUrl: "app/components/dependencies/DependenciesView.html"
        });
});

app.directive('jd', function() {
    return {
        restrict: 'E',
        link: function(scope, element, attrs) {
            element.replaceWith('<object class="javadoc" type="text/html" data="/javadoc/' + attrs.src + '/"></object>');
        }
    };
});

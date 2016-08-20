app.controller('HomeController', ['$scope', '$animate', '$timeout', 'Releases', 'Javadoc', 'Dependencies', function (s, a, t, R, J, D) {
    s.nshow = 3;
    s.releasesName = [];

    s.releases = undefined;
    s.release = undefined;
    s.actualR = -1;

    s.blocks = undefined;
    s.block = undefined;
    s.actualB = -1;

    R.releases().success(function (data) {
        s.releases = data;
        for (var i = 0; i < s.releases.length; i++) {
            s.releasesName.push(s.releases[i].tag_name);
        }
        s.actualR = 0;

        s.show = (s.releases.length > 3) ? s.nshow : s.releases.length;
    }).error(function (err) {
        s.noaccess = "there was an error loading the content";
    });

    s.hasJavadoc = function (v) {
        if (v === "") {
            return false;
        }
        return J.check(v);
    };

    s.hasDependencies = function (v) {
        if (v === "") {
            return false;
        }
        return D.getDependencies(v) === undefined;
    };

    s.optionSelected = function (v) {
        if (!s.disabled) {
            s.disabled = true;
            for (var i = 0; i < s.releases.length; i++) {
                if (s.releases[i].tag_name === v) {
                    if (s.actualR === i) {
                        s.disabled = false;
                    }
                    else {
                        s.actualR = i;
                        s.actualB = Math.floor(i / s.show);
                    }
                    break;
                }
            }
        }
    };

    s.hasNext = function () {
        if (s.blocks !== undefined) {
            return s.actualB < s.blocks.length - 1;
        }
    };

    s.hasBefore = function () {
        return s.actualB > 0;
    };

    s.next = function () {
        if (s.hasNext() && !s.disabled) {
            var e = angular.element(document.getElementsByClassName("pagination"));
            var c = "update";

            // Fade update
            s.animate(e, c, function () {
                s.actualB += 1;
            }, function () {});
        }
    };

    s.before = function () {
        if (s.hasBefore() && !s.disabled) {
            var e = angular.element(document.getElementsByClassName("pagination"));
            var c = "update";

            // Fade update
            s.animate(e, c, function () {
                s.actualB -= 1;
            }, function () {});
        }
    };

    s.$watch('show', function (nv, ov) {
        if (s.releases !== undefined && !(nv === undefined || nv === null || nv < 0)) {
            var e = angular.element(document.getElementsByClassName("pagination"));
            var c = "update";

            s.animate(e, c, function () {
                s.blocks = R.paginator(s.releases, s.show);
                s.actualB = Math.floor(s.actualR / s.show);
                s.block = s.blocks[s.actualB];
            }, function () {});
        }
    });

    s.$watch('actualR', function (nv, ov) {
        if (s.releases !== undefined) {
            var e = angular.element(document.getElementById("main-content"));
            var c = "before";

            // Change active class
            s.releases[nv].class = "active";
            if (ov != nv && s.releases[ov] !== undefined) {
                s.releases[ov].class = "";
            }

            // Animate the update
            s.animate(e, c, function () {
                s.release = s.releases[nv];
            }, function () {
                // Wait to the last animation
                s.disabled = false;
            });
        }
    });

    s.$watch('actualB', function (nv, ov) {
        if (s.blocks !== undefined) {
            s.block = s.blocks[nv];
        }
    });

    s.animate = function (e, c, f, f2) {
        a.addClass(e, c).then(function () {
            t(function () {
                f();
                return a.removeClass(e, c).then(function () {
                    f2();
                });
            });
        });
    };
}]);

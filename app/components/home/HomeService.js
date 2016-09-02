app.factory('Releases', function ($http) {
    var rs = {};

    rs.releases = function () {
        return $http.get("https://api.github.com/repos/AlternaCraft/PvpTitles/releases")
            .success(function (data) {
                for (var i = 0; i < data.length; i++) {
                    var release = data[i];
                    if (release.prerelease) {
                        data.splice(i, 1);
                        i--;
                        continue;
                    }
                    if (release.body !== null) {
                        release.body = filter(release.body);
                    }
                }

                return data;
            })
            .error(function (error) {
                return error;
            });
    };

    rs.paginator = function (data, show) {
        var blocks = [];

        // data length
        var q = data.length;
        // Number of blocks
        var p = Math.floor(q / show);
        if (q % show !== 0) p++;

        for (var i = 0; i < p; i++) {
            blocks[i] = [];
        }

        // Fill
        for (i = 0; i < blocks.length; i++) {
            var j = 0;

            while (j < show && j < q) {
                var k = j + (i * show);
                if (k > q - 1) break;
                blocks[i].push(data[k]);
                j++;
            }
        }

        return blocks;
    };

    return rs;
});

function filter(data) {
    var content = {
        title: "",
        list: [],
        versions: ""
    };

    if (data !== undefined) {
        var arr = data.split("\r\n");

        for (var j = 0; j < arr.length; j++) {
            var line = arr[j];

            // Caso título
            if (line.includes("###")) {
                content.title = title(line);
            }
            // Caso lista
            else if (line.charAt(0) == '*') {
                content.list.push(body(line));
            }
            // Caso negrita
            else if (line.match(/\*{2}.+\*{2}/g)) {
                content.versions = bold(line);
            }
        }
    }

    return content;
}

function title(str) {
    return str.substring(3, str.length);
}

function body(str) {
    return str.substring(1, str.length);
}

function bold(str) {
    return str.replace("**", "<b>").replace("**", "</b>");
}

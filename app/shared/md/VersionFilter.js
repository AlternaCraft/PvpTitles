app.filter("version", function () {
    return function (input) {
        if (input == undefined) {
            input = "";
        }

        if (input.charAt(0) == "v") {
            input = input.substr(1, input.indexOf("-") - 1);
        } else {
            input = input.substr(0, input.indexOf("-"));
        }

        return input;
    };
})

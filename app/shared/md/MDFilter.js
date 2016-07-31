app.filter("markdown", function(){
    return function(data) {
        if (data == undefined) {
            return null;
        }

        var result = "";

        var arr = data.body.split("\r\n");
        var lista = false;

        for (var j = 0; j < arr.length; j++) {
            var line = arr[j];

            // Caso lista
            if (line.charAt(0) == '*') {
                if (!lista) {
                    lista = true;
                    result += "<ul>";
                }
                line = list(line);
            } else {
                // Caso titulo
                if (line.includes("###")) {
                    data.name = title(line);
                    continue;
                }
                // Caso negrita
                else if (line.match(/\*{2}.+\*{2}/g)) {
                    if (lista) {
                        lista = false;
                        result += "</ul>";
                    }
                    line = bold(line);
                }
            }

            result += line;
        }

        return result;
    };
})

function title(str) {
    return str.substring(3, str.length);
}

function list(str) {
    return "<li>" + str.substring(1, str.length) + "</li>";
}

function bold(str) {
    return "<b>" + str.replace("**", "").replace("**", "") + "</b>";
}

$(document).ready(function () {
    var currentMousePos = {
        x: -1,
        y: -1
    };

    $(document).mousemove(function (event) {
        currentMousePos.x = event.pageX;
        currentMousePos.y = event.pageY;
    });

    var input = $(".search-form .form-group > input");
    var reset = $(".search-form .form-group > span.reset");

    input.keyup(function () {
        if (input.val() === "") {
            reset.hide();
        } else {
            reset.show();
        }
    });

    input.change(function (ev) {
        var x = currentMousePos.x,
            y = currentMousePos.y,
            elementMouseIsOver = document.elementFromPoint(x, y);

        if ($(elementMouseIsOver).is("span")) {
            clear();
            this.focus();
        }
    });

    reset.click(function () {
        clear();
    });

    reset.mouseleave(function() {
        reset.hide();
    });

    reset.mouseenter(function() {
        if (input.val() !== "") {
            reset.show();
        }
    });

    function clear() {
        input.val("");
        reset.hide();
    }
});

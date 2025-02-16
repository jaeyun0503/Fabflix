// add-movie.js
$(document).ready(function() {
    $("#submit-btn").click(function(event) {
        event.preventDefault();
        var formData = $("#add_form").serialize();

        var title = $("input[name='title']").val();
        var year = $("input[name='year']").val();
        var director = $("input[name='director']").val();
        var star = $("input[name='star']").val();
        var genre = $("input[name='genre']").val();

        if (title.trim() === "" || year.trim() === "" || director.trim() === "" || star.trim() === "" || genre.trim() === "") {
            alert("One or more fields are empty.");
            return;
        }

        $.ajax({
            type: "POST",
            url: "api/add-record", // Make sure this URL matches your servlet mapping
            data: formData,
            success: function(response) {
                alert(response);
                if (! response.toString().includes("Error")) {
                    document.getElementById('add_form').reset();
                }
            }
        });
    });
});
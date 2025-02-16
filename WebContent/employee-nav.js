$(document).ready(function() {
    $("#employee-navbar-placeholder").load("employee-nav.html", function(response, status, xhr) {
        if (status === "error") {
            console.error("Error loading employee nav: " + xhr.status + " " + xhr.statusText);
        }
    });
});
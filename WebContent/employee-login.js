let employee_form = $("#employee_login");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    try {
        console.log("result: ", resultDataString);
        let resultDataJson = JSON.parse(resultDataString);

        console.log("handle login response");
        console.log(resultDataJson);
        console.log(resultDataJson["status"]);

        if (resultDataJson["status"] === "success") {
            window.location.replace("metadata.html");
        } else {
            console.log("Cannot login");
            console.log(resultDataJson["message"]);
            $("#employee_login_error").text(resultDataJson["message"]);
        }
    } catch (error) {
        console.log("result: ", resultDataString);
        console.error("Error handling login result:", error);
        $("#employee_login_error").text("An error occurred. Please try again later.");
    }
}


function submitLoginForm(formSubmitEvent) {
    console.log("submit employee login form");
    formSubmitEvent.preventDefault();

    var recaptchaResponse = grecaptcha.getResponse();

    if (!recaptchaResponse) {
        console.log("Empty reCAPTCHA response");
        alert("Please complete the reCAPTCHA.");
        return;
    }

    $.ajax({
        url: "api/dashboard_login",
        method: "POST",
        data: employee_form.serialize(),
        success: handleLoginResult,
        error: function(xhr, status, error) {
            console.error("AJAX Error:", error);
            $("#employee_login_error").text("An error occurred while processing your request. Please try again later.");
        }
    });

}

// Bind the submit action of the form to a handler function
employee_form.submit(submitLoginForm);
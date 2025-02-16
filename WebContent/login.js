let login_form = $("#login_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultData) {
    // print raw data
    console.log("Raw response: ", resultData);

    if (resultData["status"] === "success") {
        window.location.replace("index.html");
    } else {
        $("#login_error_message").text(resultData["message"]);
    }
}



/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    var recaptchaResponse = grecaptcha.getResponse();

    if (!recaptchaResponse) {
        console.log("Empty reCAPTCHA response");
        alert("Please complete the reCAPTCHA.")
        return;
    }

    $.ajax(
        "api/login", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: login_form.serialize(),
            success: handleLoginResult
        }
    );
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);


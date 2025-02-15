/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */

/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    console.log("Returned shopping cart data: ", resultData);

    let cartTableBodyElement = jQuery("#cart_table_body");
    cartTableBodyElement.empty();

    let items = resultData.items || [];

    for (let i = 0; i < items.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += `<td><a href="single-movie.html?id=${items[i]["movie_id"]}">${items[i]["title"]}</a></td>`;
        rowHTML += `<td>${items[i]["quantity"]}</td>`;
        rowHTML += `<td>$${parseFloat(items[i]["price"]).toFixed(2)}</td>`;
        let itemTotal = items[i]["price"] * items[i]["quantity"];
        rowHTML += `<td>$${itemTotal.toFixed(2)}</td>`;
        rowHTML += `<td>
                      <button class="increase" data-id="${items[i]["movie_id"]}">+</button>
                      <button class="decrease" data-id="${items[i]["movie_id"]}">-</button>
                      <button class="delete" data-id="${items[i]["movie_id"]}">Delete</button>
                    </td>`;
        rowHTML += "</tr>";
        cartTableBodyElement.append(rowHTML);
    }

    jQuery("#totalPrice").text(`Total Price: $${parseFloat(resultData.totalPrice).toFixed(2)}`);
}


function fetchCartData() {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: `api/cart`,
        success: (resultData) => {
            handleResult(resultData);
        },
    });
}

function updateCart(movieId, action) {
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/updatecart",
        data: { movieId: movieId, action: action },
        success: function(resultData) {
            fetchCartData();
        },
    });
}

jQuery(document).ready(() => {
    fetchCartData();

    jQuery("#cart_table_body").on("click", ".increase", function() {
        let movieId = jQuery(this).data("id");
        updateCart(movieId, "increase");
    });

    jQuery("#cart_table_body").on("click", ".decrease", function() {
        let movieId = jQuery(this).data("id");
        updateCart(movieId, "decrease");
    });

    jQuery("#cart_table_body").on("click", ".delete", function() {
        let movieId = jQuery(this).data("id");
        updateCart(movieId, "delete");
    });

    jQuery("#proceedPayment").click(function() {
        window.location.href = "payment.html";
    });
});
function getParameterByName(target) {
    let url = window.location.href;
    target = target.replace(/[\[\]]/g, "\\$&");
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the shopping cart JSON data returned by the API, populating the cart summary table
 * and updating the total price.
 * @param resultData jsonObject
 */
function handleResult(resultData) {
    console.log("Returned shopping cart data: ", resultData);

    // Get the table body element where cart items will be shown
    let cartTableBodyElement = jQuery("#cart_table_body");
    cartTableBodyElement.empty();

    // Get the array of cart items from the response
    let items = resultData.items || [];

    // Loop through the items and build each table row
    for (let i = 0; i < items.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        // Movie Title (with link to single movie page)
        rowHTML += `<td>${items[i]["title"]}</td>`;
        // Quantity column
        rowHTML += `<td>${items[i]["quantity"]}</td>`;
        // Price per movie
        rowHTML += `<td>$${parseFloat(items[i]["price"]).toFixed(2)}</td>`;
        // Total price for that item (price * quantity)
        let itemTotal = items[i]["price"] * items[i]["quantity"];
        rowHTML += `<td>$${itemTotal.toFixed(2)}</td>`;
        rowHTML += "</tr>";
        cartTableBodyElement.append(rowHTML);
    }

    // Update the overall total price display
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
        error: function(xhr, status, error) {
            console.error("Error fetching cart data / Empty Cart:", error);
        }
    });
}

jQuery(document).on('click', '.submit', function(e) {
    e.preventDefault();

    let firstName = jQuery('input[name="firstName"]').val();
    let lastName = jQuery('input[name="lastName"]').val();
    let cardNumber = jQuery('input[name="cardNumber"]').val();
    let expirationDate = jQuery('input[name="expirationDate"]').val();
    $.ajax({
        url: 'api/purchase',
        type: 'POST',
        data: {
            firstName: firstName,
            lastName: lastName,
            cardNumber: cardNumber,
            expirationDate: expirationDate
        },
        dataType: 'html',
        success: function(response) {
            alert("Order placed successfully!");
            jQuery('body').html(response);
        },
        error: function(xhr, status, error) {
            console.error("Error processing purchase:", error);
            alert("Error processing purchase: " + error);
        }
    });
});

jQuery(document).ready(() => {
    // Initially load the shopping cart data.
    fetchCartData();
});
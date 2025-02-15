/*
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


let currentMovie = null;
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
    console.log("handleResult: populating movie information from resultData");
    console.log("resultData:", resultData);

    const movieInfoElement = jQuery("#movie_info");
    const movie = resultData[0];
    currentMovie = movie;

    movieInfoElement.append(`<h3>Movie Title: ${movie.movie_title}</h3>`);
    movieInfoElement.append(`<h3>Release Year: ${movie.movie_year}</h3>`);
    movieInfoElement.append(`<h3>Director: ${movie.movie_director}</h3>`);



    let genresArray = movie["movie_genres"]["genres"] || [];
    let genresHTML = "";

    for (let i = 0; i < genresArray.length; i += 2) {
        const gName = genresArray[i];
        const gId   = genresArray[i + 1];
        genresHTML += `<a href="genre.html?id=${gId}">${gName}</a>`;
        if (i + 2 < genresArray.length) {
            genresHTML += ", ";
        }
    }
    // rowHTML += `<td>${genresHTML}</td>`;
    movieInfoElement.append(`<h3>Genres: ${genresHTML}</h3>`);

    let starsHTML = `<h3>Stars:</h3><div style="text-align: center;">`;
    const starsArray = movie.movie_stars.stars || [];
    for (let i = 0; i < starsArray.length; i++) {
        starsHTML += `<a href="single-star.html?id=${starsArray[i + 1]}" class="star-link">${starsArray[i]}</a>`;
        if ((i + 1) % 3 === 0 || i === starsArray.length - 1)
            starsHTML += "<br>";
        else starsHTML += ", ";
        i++;
    }
    starsHTML += `</div>`;
    movieInfoElement.append(starsHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

jQuery(document).on('click', '.add-to-cart', function() {
    const price = currentMovie.movie_price;
    $.ajax({
        url: 'api/addcart',  // Must match the servlet mapping (@WebServlet urlPatterns)
        type: 'POST',
        data: {
            movieId: currentMovie.movie_id,
            title: currentMovie.movie_title,
            price: price
        },
        success: function(response) {
            alert("Item added to cart.");
        },
    });
});
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
const genreMap = {
    "Action": 1,
    "Adult": 2,
    "Adventure": 3,
    "Animation": 4,
    "Biography": 5,
    "Comedy": 6,
    "Crime": 7,
    "Documentary": 8,
    "Drama": 9,
    "Family": 10,
    "Fantasy": 11,
    "History": 12,
    "Horror": 13,
    "Music": 14,
    "Musical": 15,
    "Mystery": 16,
    "Reality-TV": 17,
    "Romance": 18,
    "Sci-Fi": 19,
    "Sport": 20,
    "Thriller": 21,
    "War": 22,
    "Western": 23
};
let currentPage = 1;
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
    console.log("Returned resultData: ", resultData);

    if (resultData.genre_name) {
        jQuery("h1").text(`Genre: ${resultData.genre_name}`);
        document.title = `Genre: ${resultData.genre_name}`;
    }

    let movies = resultData.movies;
    let movieTableBodyElement = jQuery("#movielist_table_body");
    movieTableBodyElement.empty();

    for (let i = 0; i < movies.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";

        // 电影标题加超链接
        rowHTML += `<td><a href="single-movie.html?id=${movies[i]["movie_id"]}">${movies[i]["movie_title"]}</a></td>`;
        rowHTML += `<td>${movies[i]["movie_year"]}</td>`;
        rowHTML += `<td>${movies[i]["movie_director"]}</td>`;
        rowHTML += `<td>${movies[i]["movie_rating"]}</td>`;

        // 处理Genres
        let genresArray = movies[i]["movie_genres"]["genres"] || [];
        let genresHTML = "";

        // Loop through in steps of 2: [name, id, name, id, ...]
        for (let i = 0; i < genresArray.length; i += 2) {
            const gName = genresArray[i];
            const gId   = genresArray[i + 1];

            genresHTML += `<a href="genre.html?id=${gId}">${gName}</a>`;
            // Add comma only if not the last pair
            if (i + 2 < genresArray.length) {
                genresHTML += ", ";
            }
        }
        rowHTML += `<td>${genresHTML}</td>`;

        // 处理Stars Array
        let starsArray = movies[i]["movie_stars"]["stars"] || [];
        let starsHTML = "";
        for (let j = 0; j < starsArray.length; j += 2) {
            let starName = starsArray[j];
            let starId = starsArray[j + 1];
            starsHTML += `<a href="single-star.html?id=${starId}">${starName}</a>`;
            if (j + 2 < starsArray.length) {
                starsHTML += ", "; // 添加逗号分隔符
            }
        }
        rowHTML += `<td>${starsHTML}</td>`;
        rowHTML += `<td>${movies[i]["movie_price"]}</td>`;
        rowHTML += `<td><button class="add-to-cart" data-movie-id="${movies[i]["movie_id"]}" data-title="${movies[i]["movie_title"]}" data-price="${movies[i]["movie_rating"]}">Add to Cart</button></td>`;

        rowHTML += "</tr>";
        movieTableBodyElement.append(rowHTML);
    }
    if (resultData.total_pages) {
        let totalPages = resultData.total_pages;
        let pageInfo = jQuery("#pageInfo");
        pageInfo.text(`Page ${currentPage} of ${totalPages}`);

        // Disable "Previous" if on page 1
        if (currentPage <= 1) {
            jQuery("#prevPage").prop("disabled", true);
        } else {
            jQuery("#prevPage").prop("disabled", false);
        }

        // Disable "Next" if on the last page
        if (currentPage >= totalPages) {
            jQuery("#nextPage").prop("disabled", true);
        } else {
            jQuery("#nextPage").prop("disabled", false);
        }
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL


function fetchData(pageNumber) {
    let genreId = getParameterByName('id');
    let resultsPerPage = jQuery("#resultsPerPage").val() || 25;
    let sortBy = jQuery("#sortBy").val() || null;
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: `api/genre?id=${genreId}&page=${pageNumber}&resultsPerPage=${resultsPerPage}&sortBy=${sortBy}`,
        success: (resultData) => {
            currentPage = pageNumber;
            handleResult(resultData)
        },
    });
}

jQuery(document).ready(() => {
    // Initial fetch with page=1
    fetchData(1);

    jQuery("#resultsPerPage").on("change", function () {
        fetchData(1);
    });

    jQuery("#sortBy").on("change", function () {
        fetchData(1);
    });

    jQuery("#prevPage").click(function() {
        if (currentPage > 1) {
            fetchData(currentPage - 1);
        }
    });

    jQuery("#nextPage").click(function() {
        fetchData(currentPage + 1);
    });
});

$(document).on('click', '.add-to-cart', function() {
    const movieId = $(this).data('movie-id');
    const title = $(this).data('title');
    const price = $(this).data('price');
    $.ajax({
        url: 'api/addcart',
        type: 'POST',
        data: { movieId: movieId, title: title, price: price },
        success: function(response) {
            alert(response);
        },
    });
});
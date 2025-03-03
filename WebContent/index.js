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

let currentPage = 1;
let currentSortBy = "ratinghltitlelh";
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

function handleLookup(query, doneCallback) {
    if (query.length < 3) return;

    console.log("autocomplete initiated")
    console.log("sending AJAX request to backend Java Servlet")
    const data = localStorage.getItem(query)

    if (data) {
        console.log("cached results found in local storage")
        handleLookupAjaxSuccess(JSON.parse(data), query, doneCallback)
        return;
    }

    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    jQuery.ajax({
        method: "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        url: "api/autocomplete?query=" + escape(query),
        success: function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        error: function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}


function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")

    // parse the string into JSON
    var jsonData = $.parseJSON(JSON.stringify(data));
    console.log(jsonData)

    localStorage.setItem(query, JSON.stringify(data))

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    console.log("Returned resultData: ", resultData);
    if(resultData.currentPage){
        currentPage = resultData.currentPage;
        localStorage.setItem("currentPage", currentPage);
    }
    if(resultData.currentSortBy){
        currentSortBy = resultData.currentSortBy;
        jQuery("#sortBy").val(resultData.currentSortBy);
        localStorage.setItem("currentSortBy", currentSortBy);
    }

    let movieTableBodyElement = jQuery("#movielist_table_body");
    movieTableBodyElement.empty();
    let movies = resultData.movies || [];

    for (let i = 0; i < movies.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";

        rowHTML += `<td><a href="single-movie.html?id=${movies[i]["movie_id"]}">${movies[i]["movie_title"]}</a></td>`;
        rowHTML += `<td>${movies[i]["movie_year"]}</td>`;
        rowHTML += `<td>${movies[i]["movie_director"]}</td>`;
        rowHTML += `<td>${movies[i]["movie_rating"]}</td>`;

        // 处理Genres
        let genresArray = movies[i]["movie_genres"]["genres"] || [];
        let genresHTML = "";

        // Loop through in steps of 2: [name, id, name, id, ...]
        for (let j = 0; j < genresArray.length; j += 2) {
            const gName = genresArray[j];
            const gId   = genresArray[j + 1];

            genresHTML += `<a href="genre.html?id=${gId}">${gName}</a>`;
            // Add comma only if not the last pair
            if (j + 2 < genresArray.length) {
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
                starsHTML += ", ";
            }
        }
        rowHTML += `<td>${starsHTML}</td>`;
        rowHTML += `<td>${movies[i]["movie_price"]}</td>`;
        rowHTML += `<td><button class="add-to-cart" data-movie-id="${movies[i]["movie_id"]}" data-title="${movies[i]["movie_title"]}" data-price="${movies[i]["movie_price"]}">Add to Cart</button></td>`;

        rowHTML += "</tr>";
        movieTableBodyElement.append(rowHTML);
    }

    if (resultData.total_pages) {
        let totalPages = resultData.total_pages;
        let pageInfo = jQuery("#pageInfo");
        pageInfo.text(`Page ${currentPage} of ${totalPages}`);

        if (currentPage <= 1) {
            jQuery("#prevPage").prop("disabled", true);
        } else {
            jQuery("#prevPage").prop("disabled", false);
        }

        if (currentPage >= totalPages) {
            jQuery("#nextPage").prop("disabled", true);
        } else {
            jQuery("#nextPage").prop("disabled", false);
        }
    }
}


function handleSelectSuggestion(suggestion) {
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["id"])
    window.location.href = `single-movie.html?id=${suggestion["data"]["id"]}`
}


function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

function fetchData(pageNumber) {
    let resultsPerPage = jQuery("#resultsPerPage").val() || 25;
    let sortBy = jQuery("#sortBy").val() || null;

    let title = getParameterByName("title");
    let year = getParameterByName("year");
    let director = getParameterByName("director");
    let star = getParameterByName("star");
    let genre = getParameterByName("genre");
    let letter = getParameterByName("letter");

    let search = "";
    if (title !== null) {
        search += `&title=${encodeURIComponent(title)}`;
    }
    if (year !== null) {
        search += `&year=${encodeURIComponent(year)}`;
    }
    if (director !== null) {
        search += `&director=${encodeURIComponent(director)}`;
    }
    if (star !== null) {
        search += `&star=${encodeURIComponent(star)}`;
    }
    if (genre !== null) {
        search += `&genre=${encodeURIComponent(genre)}`;
    }
    if (letter !== null) {
        search += `&letter=${encodeURIComponent(letter)}`;
    }
    console.log(search);
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: `api/movielist?page=${pageNumber}&resultsPerPage=${resultsPerPage}&sortBy=${sortBy}${search}`,
        success: (resultData) => {
            currentPage = pageNumber;
            handleResult(resultData);
        },
    });
}


jQuery(document).ready(() => {
    let savedPage = localStorage.getItem("currentPage");
    let savedSortBy = localStorage.getItem("currentSortBy");

    if (!savedPage) {
        savedPage = currentPage;
    }
    if (!savedSortBy) {
        savedSortBy = currentSortBy;
    }
    jQuery("#sortBy").val(savedSortBy);
    fetchData(savedPage);

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

    jQuery("#nextPage").click(function () {
        fetchData(currentPage + 1);
    });

    jQuery("#search_form").submit(function(e) {
        e.preventDefault();
        let title = jQuery('input[name="title"]').val();
        let year = jQuery('input[name="year"]').val();
        let director = jQuery('input[name="director"]').val();
        let star = jQuery('input[name="star"]').val();

        let newQuery = `?title=${encodeURIComponent(title)}&year=${encodeURIComponent(year)}&director=${encodeURIComponent(director)}&star=${encodeURIComponent(star)}`;
        history.pushState(null, "", newQuery);
        fetchData(1);
    });

    if ($('#autocomplete').length) {
        $('#autocomplete').autocomplete({
            lookup: function(query, doneCallback) {
                handleLookup(query, doneCallback);
            },
            onSelect: function(suggestion) {
                handleSelectSuggestion(suggestion);
            },
            deferRequestBy: 300 // Set a slight delay for performance
        });

        $('#autocomplete').keypress(function(event) {
            if (event.keyCode == 13) { // enter key
                handleNormalSearch($('#autocomplete').val());
            }
        });
    }

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
// Function to fetch metadata from the server
function fetchMetadata() {
    $.ajax({
        url: "api/metadata", // Replace with your actual endpoint URL
        type: 'GET',
        success: function(data) {
            Object.keys(data).forEach(function(tableName) {
                populateTable(tableName + '_table_body', data[tableName]);
            });
        },
        error: function(xhr, status, error) {
            console.error('Error fetching metadata:', error);
        }
    });
}

// Function to populate table with metadata including a header row
function populateTable(tableId, metadata) {
    var tbody = $('#' + tableId);
    tbody.empty(); // Clear existing content

    var headerRow = '<tr class="table-header-row"><th>Attribute</th><th>Type</th></tr>';
    tbody.append(headerRow);
    console.log(metadata);
    // Iterate over metadata and generate HTML rows
    metadata.forEach(function(column) {
        var row = '<tr><td>' + column.attr + '</td><td>' + column.type + '</td></tr>';
        tbody.append(row);
    });
}



// Call the fetchMetadata function when the page is loaded
$(document).ready(function() {
    fetchMetadata();
});

// Log message to confirm that the JavaScript file has been correctly loaded
console.log("queryConnection.js caricato correttamente");

// Register an event listener that executes when the DOM is fully loaded
document.addEventListener("DOMContentLoaded", function () {
    // Log message to confirm that the DOM is ready
    console.log("DOM completamente caricato");

    // Retrieve references to key DOM elements used for the search functionality
    const form = document.getElementById("searchForm");
    const input = document.getElementById("searchInput");
    const resultsDiv = document.getElementById("results");

    // Validate that all required DOM elements exist
    // If any element is missing, log an error and stop execution
    if (!form || !input || !resultsDiv) {
        console.error("Elementi mancanti nel DOM");
        return;
    }

    // Attach a submit event listener to the search form
    form.addEventListener("submit", function (event) {
        // Prevent the default form submission behavior (page refresh)
        event.preventDefault();
        console.log("Submit intercettato");

        // Retrieve and sanitize the user input
        const query = input.value.trim();

        // Check for empty search queries
        // If empty, display a warning message and stop execution
        if (!query) {
            console.warn("Query vuota");
            resultsDiv.innerHTML = "<p>Inserisci una query da cercare.</p>";
            return;
        }

        // Build the API endpoint URL, encoding the query to ensure safe transmission
        const url = `/api/v1/searcher/searchDocuments?query=${encodeURIComponent(query)}`;
        console.log("Chiamata API:", url);

        // Display a loading message while the request is being processed
        resultsDiv.innerHTML = "<p>Sto cercando...</p>";

        // Perform an asynchronous HTTP GET request using the Fetch API
        fetch(url)
            .then(response => {
                // Log the HTTP status code returned by the server
                console.log("HTTP status:", response.status);

                // Handle non-successful HTTP responses
                if (!response.ok) {
                    return response.text().then(text => {
                        console.error("Response non OK, testo:", text);
                        throw new Error("Errore HTTP: " + response.status);
                    });
                }

                // Parse the response body as JSON
                return response.json();
            })
            .then(data => {
                // Log the parsed JSON response
                console.log("Risposta JSON:", data);

                // Clear previous search results
                resultsDiv.innerHTML = "";

                // Handle the case where no results are returned
                if (!data || data.length === 0) {
                    resultsDiv.innerHTML = "<p>Nessun risultato trovato.</p>";
                    return;
                }

                // Iterate over the returned documents and render them dynamically
                data.forEach(doc => {
                    const card = document.createElement("div");
                    card.className = "document-result card mb-2 p-2";

                    const title = document.createElement("h5");
                    title.textContent = doc.title ?? "(titolo mancante)";

                    const topic = document.createElement("p");
                    topic.textContent = doc.topic ?? "(argomento mancante)";

                    card.appendChild(title);
                    card.appendChild(topic);

                    if (doc.url) {
                        const link = document.createElement("a");
                        link.href = doc.url;
                        link.textContent = doc.url;
                        link.target = "_blank";
                        link.rel = "noopener noreferrer";
                        card.appendChild(link);
                    }

                    resultsDiv.appendChild(card);
                });
            })
            .catch(error => {
                // Handle network or processing errors
                console.error("Errore fetch:", error);

                // Display a user-friendly error message
                resultsDiv.innerHTML = "<p>Si è verificato un errore nella ricerca. Controlla la console per dettagli.</p>";
            });
    });
});

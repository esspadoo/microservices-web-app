// ===============================
// queryConnection.js
// ===============================

console.log("queryConnection.js caricato correttamente");

document.addEventListener("DOMContentLoaded", function () {
    console.log("DOM completamente caricato");

    const form = document.getElementById("searchForm");
    const input = document.getElementById("searchInput");
    const resultsDiv = document.getElementById("results");

    if (!form || !input || !resultsDiv) {
        console.error("Elementi mancanti nel DOM");
        return;
    }

    form.addEventListener("submit", function (event) {
        event.preventDefault(); // blocca il refresh della pagina
        console.log("Submit intercettato");

        const query = input.value.trim();
        if (!query) {
            console.warn("Query vuota");
            resultsDiv.innerHTML = "<p>Inserisci una query da cercare.</p>";
            return;
        }

        const url = `/api/v1/searcher/searchDocuments?query=${encodeURIComponent(query)}`;
        console.log("Chiamata API:", url);

        // Mostra messaggio di caricamento
        resultsDiv.innerHTML = "<p>Sto cercando...</p>";

        fetch(url)
            .then(response => {
                console.log("HTTP status:", response.status);
                if (!response.ok) {
                    return response.text().then(text => {
                        console.error("Response non OK, testo:", text);
                        throw new Error("Errore HTTP: " + response.status);
                    });
                }
                return response.json();
            })
            .then(data => {
                console.log("Risposta JSON:", data);

                resultsDiv.innerHTML = ""; // pulisci i risultati precedenti

                if (!data || data.length === 0) {
                    resultsDiv.innerHTML = "<p>Nessun risultato trovato.</p>";
                    return;
                }

                data.forEach(doc => {
                    const div = document.createElement("div");
                    div.className = "document-result card mb-2 p-2";
                    div.innerHTML = `
                        <h5>${doc.title || "(titolo mancante)"}</h5>
                        <p>${doc.content || "(contenuto mancante)"}</p>
                    `;
                    resultsDiv.appendChild(div);
                });
            })
            .catch(error => {
                console.error("Errore fetch:", error);
                resultsDiv.innerHTML = "<p>Si è verificato un errore nella ricerca. Controlla la console per dettagli.</p>";
            });
    });
});


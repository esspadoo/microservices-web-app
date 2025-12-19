// ===============================
// queryConnection.js
// ===============================

// LOG DI CARICAMENTO (OBBLIGATORIO)
console.log("queryConnection.js caricato correttamente");

// Attendi che il DOM sia pronto
document.addEventListener("DOMContentLoaded", function () {
    console.log("DOM completamente caricato");

    const form = document.getElementById("searchForm");
    const input = document.getElementById("searchInput");

    if (!form) {
        console.error("ERRORE: form #searchForm non trovato");
        return;
    }

    if (!input) {
        console.error("ERRORE: input #searchInput non trovato");
        return;
    }

    // Intercetta il submit del form
    form.addEventListener("submit", function (event) {
        event.preventDefault(); // 🔴 BLOCCA il submit HTML
        console.log("Submit intercettato");

        const query = input.value.trim();

        if (query.length === 0) {
            console.warn("Query vuota");
            return;
        }

        const url = `/api/v1/searcher/searchDocuments?query=${encodeURIComponent(query)}`;
        console.log("Chiamata API:", url);

        fetch(url)
            .then(response => {
                console.log("HTTP status:", response.status);

                if (!response.ok) {
                    throw new Error("Errore HTTP: " + response.status);
                }

                return response.json();
            })
            .then(data => {
                console.log("Risposta JSON:", data);
            })
            .catch(error => {
                console.error("Errore fetch:", error);
            });
    });
});


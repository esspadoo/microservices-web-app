// Seleziona il form
const form = document.getElementById("searchForm");

// Aggiungi listener al submit
form.addEventListener("submit", function(event) {
    event.preventDefault(); // evita il refresh della pagina

    // Prendi il valore del campo di input
    const query = document.getElementById("searchInput").value;

    // Invia la richiesta GET al server
    fetch(`/api/v1/searcher/searchDocuments?query=${encodeURIComponent(query)}`, {
    method: "GET"
})
    .then(response => {
        if (!response.ok) {
            throw new Error("Errore HTTP: " + response.status);
        }
        return response.json(); // se il server restituisce JSON
    })
    .then(data => {
        console.log("Risposta dal server:", data);

        // Qui puoi aggiornare la pagina con i risultati
        // ad esempio, creare un div sotto il form e inserire i dati
    })
    .catch(error => {
        console.error("Errore nella richiesta:", error);
    });
});


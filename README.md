# Mobile Computing Project
Android application written in Kotlin.

# Progetto "Mangia e Basta" del corso di Mobile Computing
L'obiettivo del progetto riguarda la creazione di un'applicazione mobile android simile ad applicazioni di delivery come Globo e  Deliveroo. 
A richiesta del docente l'app deve gestire un utente, il quale può scegliere tra diversi menù nelle sue vicinanze, visualizzare i dettagli dei menù, ordinare e tenere traccia di dove si trova l'ordine e visualizzare il proprio profilo più la cronologia degli ordini. 

La progettazione dello schema di navigazione e delle singole schermate è a scelta libera dello studente.

Nello specifico sono state richieste le seguenti funzionalità:
- **Registrazione implicita**. Ogni utente dispone di un numero di sessione (SID) che lo identifica rispetto al server. Al primo avvio l'applicazione richiede un numero di sessione al server e poi lo memorizza in modo persistente. In tutte le comunicazioni tra client e server, il cliente indicherà il proprio numero di sessione.
- **Profilo**. Nella schermata di profilo l'utente imposta i propri dati: nome e cognome, nominativo nella carta di credito, numero carta, data scadenza, cvv. Inoltre, dalla schermata di profilo l'utente può vedere l'ultimo ordine effettuato.
- **Lista dei menù**. L'utente può vedere una lista di menuù offerti dai ristoranti nei paraggi. Per ciascuno menù viene visualizzato il nome, un'immagine, il costo, una breve descrizione e il tempo previsto per la consegna.
- **Dettagli menù**. Dopo aver selezionato il menù, l'utente ne legge i dettagli in una apposita schemata. In tale schermata vi visualizzano gli stessi dati della "lista menù", con un'immagine più grande, una descrizione lunga e la possibilità di acquistare il menù. Non è possibile acquistare un menù se l'utente non ha ancora completato il proprio profilo o se ha un ordine in corso.
- **Stato consegna**. Dopo aver acquistato un menù, l'utente può visualizzare llo stato di consegna. Questa schermata riposta il menù acquistato, l'orario di consegna e lo stato di consegna (consegnato/in consegna). Se lo stato è "in consegna", l'utente vede su una mappa la posizione finale di consegna, il punto di partenza, e il punto attuale del drone. Se lo stato è "consegnato" l'utente visualizza, su una mappa, solo il punto di partenza e la destinazione. Questa schermata si deve aggiornare ogni 5 secondi.
- **Salvataggio pagina**. L'applicazione deve ricordarsi quale pagina è stata visualizzata, in caso l'app venga terminata. In questo modo, al riavvio ricarica l'ultima pagina.

# Struttura e progettazione del codice 
La progettazione interna dell'applicazione è stata svolta e scritta seguendo il pattern MVVM (Model, View, ViewModel).

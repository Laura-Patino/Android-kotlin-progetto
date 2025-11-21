# Mobile Computing second Project
Android application written in Kotlin.

# Progetto "Mangia e Basta" del corso di Mobile Computing
L'obiettivo del progetto riguarda la creazione di un'applicazione mobile android simile ad applicazioni di delivery come Globo e  Deliveroo. 
A richiesta del docente l'app deve gestire un utente, il quale può scegliere tra diversi menù nelle sue vicinanze, visualizzare i dettagli dei menù, ordinare e tenere traccia di dove si trova l'ordine e visualizzare il proprio profilo più la cronologia degli ordini. 

La progettazione dello schema di navigazione e delle singole schermate è a scelta libera dello studente. Nel mio caso ho scelto di creare tre sezioni/schermate principali (Home, LastOrder, e Profile). All'interno di *Home* è possibile navigare sequenzialmente per visualizzare i dettagli di ciascun menù. Nella schermata *Profilo* è possibile passare ad una schermata sequenziale per la modifica dei dati. 

Nello specifico sono state richieste le seguenti funzionalità:
- **Registrazione implicita**. Ogni utente dispone di un numero di sessione (SID) che lo identifica rispetto al server. Al primo avvio l'applicazione richiede un numero di sessione al server e poi lo memorizza in modo persistente. In tutte le comunicazioni tra client e server, il cliente indicherà il proprio numero di sessione.
- **Profilo**. Nella schermata di profilo l'utente imposta i propri dati: nome e cognome, nominativo nella carta di credito, numero carta, data scadenza, cvv. Inoltre, dalla schermata di profilo l'utente può vedere l'ultimo ordine effettuato.
- **Lista dei menù**. L'utente può vedere una lista di menuù offerti dai ristoranti nei paraggi. Per ciascuno menù viene visualizzato il nome, un'immagine, il costo, una breve descrizione e il tempo previsto per la consegna.
- **Dettagli menù**. Dopo aver selezionato il menù, l'utente ne legge i dettagli in una apposita schemata. In tale schermata vi visualizzano gli stessi dati della "lista menù", con un'immagine più grande, una descrizione lunga e la possibilità di acquistare il menù. *Ci sono delle condizioni da rispettare per poter fare un'ordine: non è possibile acquistare un menù se l'utente non ha ancora completato il proprio profilo (carta di credito), se ha un ordine in corso, se non ha autorizzato la lettura della sua posizione.*
- **Stato consegna**. Dopo aver acquistato un menù, l'utente può visualizzare llo stato di consegna. Questa schermata riposta il menù acquistato, l'orario di consegna e lo stato di consegna (consegnato/in consegna). Se lo stato è "in consegna", l'utente vede su una mappa la posizione finale di consegna, il punto di partenza, e il punto attuale del drone. Se lo stato è "consegnato" l'utente visualizza, su una mappa, solo il punto di partenza e la destinazione. Questa schermata si deve aggiornare ogni 5 secondi.
- **Salvataggio pagina**. L'applicazione deve ricordarsi quale pagina è stata visualizzata, in caso l'app venga terminata. In questo modo, al riavvio ricarica l'ultima pagina.

## Struttura e progettazione del codice 
La progettazione interna dell'applicazione è stata svolta e scritta seguendo il pattern MVVM (Model, View, ViewModel).
Il codice è visualizzabile nella cartella app/src/main/java/com/example/progetto, suddiviso in quattro cartelle principali: 
- Model. Contiene le data classes, data sources, e i repositories.
- Viewmodel. Contiene due viewmodel, uno per la gestione dell'intera applicazione e uno per la gestione della verifica della correttezza dei dati dell'utente.
- View. Sono presenti tutte le schermate in /screens, e alcune componenti (elementi grafici) comuni nella cartella /commons.
- Ui/theme. sono presenti tre file per la personalizzazione dello stile, colori e temi dell'applicazione.

## Screenshot dell'applicazione 
**Home Screen** lista dei menù nelle vicinanze.

<img width="412" height="879" alt="Screenshot 2025-11-21 215820" src="https://github.com/user-attachments/assets/b17ea020-2a69-40c2-8dde-73579c5ca4e3" />

**Details screen** dettagli di un menù e sezione per effettuare un ordine

<img width="434" height="877" alt="Screenshot 2025-11-21 215831" src="https://github.com/user-attachments/assets/0cefb54b-f032-4a0d-84cc-fe237796bea2" />

**Order Screen** ordine già consegnato

<img width="431" height="867" alt="Screenshot 2025-11-21 215754" src="https://github.com/user-attachments/assets/fe6fabc3-a83f-470b-95df-91c03a24321e" />


**Order Screen** ordine in fase di consegna (tre icone)

<img width="425" height="874" alt="Screenshot 2025-11-21 220010" src="https://github.com/user-attachments/assets/f7b5d9cf-7f5e-4d93-a7d4-19365d292873" />


**Profile Screen** dati utente e dati del ultimo ordine

<img width="421" height="872" alt="Screenshot 2025-11-21 215805" src="https://github.com/user-attachments/assets/8ed5f740-c3dc-4389-892b-203c09292406" />


**Update User info screen** aggiornamento dei dati dell'utente

<img width="412" height="870" alt="Screenshot 2025-11-21 215907" src="https://github.com/user-attachments/assets/d20e00df-d22c-4c78-bcde-5bd8a2033e2e" />
 

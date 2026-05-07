# WhereTheDuck - Analisi Tecnica e Architettura

## 1. Obiettivo del Progetto
Realizzare un'applicazione Android che permetta a membri di un gruppo (es. "Famiglia") di inviare notifiche sonore ad alta priorità ("starnazzi") in grado di bypassare la modalità Silenzioso e "Non Disturbare" (DND) sui dispositivi degli altri membri, con un'esperienza simile a una chiamata in arrivo e feedback in tempo reale tra mittente e ricevente.

## 2. Fattibilità Tecnica (Android)
L'applicazione è fattibile utilizzando le API native di Android e i servizi Firebase.

### Funzionalità Core:
*   **Bypass DND (Do Not Disturb):**
    *   Richiesta del permesso `ACCESS_NOTIFICATION_POLICY`.
    *   Utilizzo di `NotificationManager` per creare canali di notifica con importanza `IMPORTANCE_HIGH` o `IMPORTANCE_URGENT`.
    *   Possibilità di modificare temporaneamente il volume di sistema (`AudioManager`) all'arrivo della notifica.
*   **Schermata Full-Screen su Lockscreen (stile chiamata in arrivo):**
    *   Utilizzo di `setFullScreenIntent()` sulla notifica per lanciare un'Activity a schermo intero.
    *   L'Activity si mostra sopra la schermata di blocco tramite `setShowWhenLocked(true)` e accende lo schermo con `setTurnScreenOn(true)` (API 27+).
    *   Permesso `USE_FULL_SCREEN_INTENT` richiesto. Da Android 14 (API 34) questo permesso è ristretto: concesso automaticamente solo ad app di tipo chiamata/sveglia. Per le altre app, l'utente deve abilitarlo manualmente dalle impostazioni. L'app dovrà guidare l'utente in questo processo.
*   **Risposte Rapide dalla Schermata di Allarme (Ricevente):**
    *   La schermata full-screen mostrerà il nome del mittente e tre opzioni di risposta:
        *   **Pollice Su (Conferma):** "Ho ricevuto, tutto ok!" — invia una conferma positiva al mittente.
        *   **Pollice Giu (Non disturbare):** "Ho ricevuto, ma non disturbarmi." — invia un feedback negativo al mittente.
        *   **Silenzia Mittente:** Permette di silenziare temporaneamente il mittente per un periodo prefissato:
            *   30 minuti
            *   1 ora
            *   2 ore
            *   Fino a domani mattina
    *   La risposta viene salvata su Firestore e notificata al mittente in tempo reale tramite FCM.
    *   Il silenziamento è gestito localmente sul dispositivo del ricevente: durante il periodo di mute, gli allarmi da quel mittente vengono ricevuti come notifiche normali (senza bypass DND e senza schermata full-screen).
*   **Gestione Identità:**
    *   **Firebase Authentication** con Google Sign-In per l'associazione degli utenti.
*   **Sincronizzazione Real-time:**
    *   **Cloud Firestore** per la gestione dei gruppi, dei permessi e delle risposte.
    *   **Firebase Cloud Messaging (FCM)** per l'invio istantaneo dei segnali di allarme (Data Messages ad alta priorità).

---

## 3. Tipi di Starnazzo

Da qui in avanti il termine ufficiale per le notifiche sonore è **"starnazzo"**.

L'app offre 3 livelli di intensità dello starnazzo. **Tutti e 3 i livelli:**
*   Bypassano il DND (Do Not Disturb)
*   Alzano il volume della suoneria se è basso
*   Mostrano la schermata full-screen su lockscreen
*   Seguono la stessa progressione sonora (vedi sotto)

Ciò che cambia tra i livelli è la **tipologia di rumore** (timbro dell'animale), l'**intensità** del suono e l'**insistenza** (velocità di escalation e durata).

### Progressione Sonora (comune a tutti i livelli)
Tutti gli starnazzi seguono la stessa escalation in 4 fasi:
```
FASE 1 — Vibrazione semplice (0-5 sec)
   Vibrazioni brevi e distanziate: buzz...buzz...buzz
   Nessun suono. Solo feedback tattile.

FASE 2 — Vibrazione crescente (5-15 sec)
   Le vibrazioni aumentano di frequenza: buzz..buzz.buzz.buzzbuzz
   Ancora nessun suono.

FASE 3 — Starnazzo! (15-30 sec)
   Inizia il verso dell'animale associato al livello.
   Volume crescente. Vibrazione continua.
   Il suono dell'animale si ripete a intervalli regolari.

FASE 4 — Insistenza massima (30-60 sec)
   Il verso dell'animale diventa più rapido e forte.
   Volume al massimo. Vibrazione continua intensa.
   L'intervallo tra i versi si riduce fino a diventare quasi continuo.
```
La differenza tra i livelli sta in:
*   **Leggero:** escalation lenta, verso dell'animale delicato, fase 4 meno aggressiva.
*   **Medio:** escalation normale, verso dell'anatra (quack), fase 4 moderatamente insistente.
*   **Pesante:** escalation rapida (fasi più corte), verso dell'animale potente, fase 4 molto aggressiva e rumorosa.

### I 3 Animali

| Livello | Animale | Verso/Suono | Carattere |
|---|---|---|---|
| **Leggero** | Grillo (Cricket) | Cri-cri delicato e ritmico | Gentile ma persistente. "Ehi, quando puoi..." |
| **Medio** | Anatra (Duck) | Quack classico, starnazzo | L'iconico. "Where the Duck are you?!" |
| **Pesante** | Oca Arrabbiata (Angry Goose) | HONK aggressivo e potente | Impossibile da ignorare. Panico puro. |

**Perché questi animali:**
*   **Grillo:** suono notturno, familiare, non allarmante ma impossibile da ignorare col tempo. Perfetto per il livello leggero.
*   **Anatra:** il cuore dell'app. Lo starnazzo del quack è riconoscibile, divertente e abbastanza forte. "Where the Duck" è il motto.
*   **Oca Arrabbiata:** chiunque abbia incontrato un'oca sa che non c'è verso più terrificante e insistente. L'HONK dell'oca è puro terrore comico. Perfetto per l'urgenza massima.

### Personalizzazione Animali

La personalizzazione è **lato mittente**: il mittente sceglie quale animale "scatenare" sul ricevente. Il ricevente vede l'animale scelto dal mittente (se la sua versione dell'app lo supporta).

**Principi:**
*   Tutti gli animali (illustrazioni, suoni, animazioni) sono **bundled nell'APK**. Nessun download runtime.
*   Nuovi animali vengono introdotti con **nuove versioni dell'app**.
*   Ogni animale appartiene a un livello (leggero/medio/pesante) e ne eredita il comportamento sonoro (escalation, intensità).
*   Il mittente può scegliere un animale alternativo per il livello selezionato (es. per il livello medio: anatra OPPURE pappagallo).

**Gestione version mismatch (fallback):**
*   L'alert salva `animalType: "parrot"` (scelto dal mittente).
*   Il ricevente controlla se ha l'asset "parrot" nella sua versione dell'app.
*   Se **sì** → mostra il pappagallo con il suo suono.
*   Se **no** (versione più vecchia) → **fallback all'animale default del livello** (es. anatra per il medio).
*   Il fallback è trasparente: il ricevente vede comunque uno starnazzo del livello corretto, solo con l'animale default.

**Implementazione:**
```kotlin
// Registry degli animali disponibili nella versione corrente dell'app
object AnimalRegistry {
    private val animals: Map<String, Animal> = mapOf(
        "cricket" to Animal("cricket", StarnazzoLevel.LIGHT, R.raw.cricket, R.drawable.cricket, ...),
        "duck"    to Animal("duck",    StarnazzoLevel.MEDIUM, R.raw.duck, R.drawable.duck, ...),
        "goose"   to Animal("goose",   StarnazzoLevel.HEAVY, R.raw.goose, R.drawable.goose, ...),
        // Aggiunti in v2:
        // "owl"     to Animal("owl",     StarnazzoLevel.LIGHT, ...),
        // "parrot"  to Animal("parrot",  StarnazzoLevel.MEDIUM, ...),
    )

    private val defaults = mapOf(
        StarnazzoLevel.LIGHT  to "cricket",
        StarnazzoLevel.MEDIUM to "duck",
        StarnazzoLevel.HEAVY  to "goose",
    )

    fun resolve(animalType: String, level: StarnazzoLevel): Animal {
        return animals[animalType] ?: animals[defaults[level]]!!
    }

    fun availableForLevel(level: StarnazzoLevel): List<Animal> {
        return animals.values.filter { it.level == level }
    }
}
```

**Dati nell'alert:**
*   `animalType: string` — ID dell'animale scelto dal mittente (es. "duck", "parrot")
*   `starnazzoLevel: string` — livello dello starnazzo (usato per il fallback)

**Animali previsti (v1):**

| Livello | Default | Alternativi (future versioni) |
|---|---|---|
| Leggero | Grillo (cricket) | Gufo, Gatto che fa le fusa, ... |
| Medio | Anatra (duck) | Pappagallo, Gallo, ... |
| Pesante | Oca Arrabbiata (goose) | Leone, T-Rex, Allarme antiaereo, ... |

### File Audio degli Animali
I file audio dei versi degli animali devono essere **forniti manualmente** e posizionati in `res/raw/` prima della Fase 5:
*   `res/raw/cricket.mp3` — verso del grillo (cri-cri ritmico)
*   `res/raw/duck.mp3` — verso dell'anatra (quack/starnazzo)
*   `res/raw/goose.mp3` — verso dell'oca arrabbiata (HONK aggressivo)

**Fonti possibili:**
*   Generazione AI (es. modelli text-to-audio)
*   Librerie royalty-free (freesound.org, pixabay.com/sound-effects)
*   I file devono essere brevi (1-3 secondi), in loop. L'app li ripete con intervalli variabili durante le fasi 3 e 4.

**Formato consigliato:** MP3 o OGG, mono, 44.1kHz, < 100KB per file.

### Schermata di Ricezione (IncomingAlertActivity)
La schermata full-screen cambia aspetto in base al livello ma è sempre full-screen sopra il lockscreen:
```
LEGGERO (Grillo):                MEDIO (Anatra):                  PESANTE (Oca):
┌─────────────────────┐          ┌─────────────────────┐          ┌─────────────────────┐
│     tema verde      │          │    tema giallo       │          │  tema rosso/arancio  │
│                     │          │                     │          │                     │
│   🦗               │          │   🦆               │          │   🪿 HONK!!!       │
│   cri-cri...        │          │                     │          │                     │
│                     │          │  Where the Duck     │          │  Where the Goose    │
│   Anna ti sta       │          │    are you?!        │          │    are you?!        │
│   cercando          │          │                     │          │                     │
│                     │          │   Anna ti sta       │          │   Anna ti sta       │
│  [👍] [👎] [🔇]   │          │   cercando!         │          │   cercando!!!       │
│                     │          │                     │          │                     │
│                     │          │  [👍] [👎] [🔇]    │          │  [👍] [👎] [🔇]    │
│                     │          │                     │          │                     │
└─────────────────────┘          └─────────────────────┘          └─────────────────────┘
(sfondo calmo)                   (sfondo vivace)                  (sfondo pulsante rosso)
```
*   L'animazione dell'animale si intensifica in sincrono con la progressione sonora (fase 1→4).
*   Il colore dello sfondo pulsa/si intensifica nelle fasi avanzate.
*   Il testo principale è sempre "Where the [Animal] are you?!" come motto scherzoso.

---

## 4. Sistema Inviti e Gruppi

### 4.1 Creazione Gruppo
```
1. L'utente crea un nuovo gruppo dalla GroupManagementScreen
2. Inserisce un nome (es. "Famiglia", "Coinquilini")
3. Il sistema genera un groupId univoco
4. Il creatore diventa automaticamente admin del gruppo
```

### 4.2 Invito via Email (admin invita → destinatario accetta)

L'unica modalità di invito è tramite **email**. L'admin inserisce l'email dell'utente che vuole invitare. L'utente deve essere già registrato su WhereTheDuck (Google Sign-In).

```
ADMIN:
1. Apre il gruppo → preme "Invita membro"
2. Inserisce l'email dell'utente da invitare
3. Cloud Function verifica:
   a. L'email corrisponde a un utente registrato? → continua
   b. L'utente è già membro del gruppo? → errore "Già membro"
   c. Esiste già un invito pendente? → errore "Invito già inviato"
4. Crea invito su Firestore con stato "pending"
5. Invia FCM al destinatario: "Mario ti ha invitato in Famiglia"
6. L'admin vede conferma: "Invito inviato a anna@gmail.com"

DESTINATARIO:
1. Riceve notifica: "Mario ti ha invitato in Famiglia"
2. Apre l'app → vede l'invito pendente (nella sezione inviti o come banner)
3. Vede: nome gruppo + chi lo ha invitato
4. Preme "Accetta" → diventa membro, appare nella HomeScreen del gruppo
   Preme "Rifiuta" → l'admin riceve notifica "Anna ha rifiutato l'invito"

UTENTE NON REGISTRATO:
- Se l'email non corrisponde a nessun utente → l'admin vede:
  "Utente non trovato. L'utente deve prima registrarsi su WhereTheDuck."
```

### 4.3 Gestione Membri
*   **Admin** può: invitare membri via email, rimuovere membri, eliminare il gruppo.
*   **Membro** può: uscire dal gruppo, inviare starnazzi agli altri membri.
*   Un utente può appartenere a più gruppi contemporaneamente.

### 4.4 Struttura Dati Inviti (Firestore)
```
groups/{groupId}
    - name
    - createdBy
    - createdAt

    members/{userId}
        - displayName
        - photoUrl
        - role: "admin" | "member"
        - joinedAt

    invites/{inviteId}
        - invitedEmail
        - invitedUserId           (risolto dalla Cloud Function cercando l'email in users/)
        - invitedDisplayName
        - invitedBy               (userId dell'admin)
        - invitedByDisplayName
        - status: "pending" | "accepted" | "rejected"
        - createdAt
        - respondedAt: timestamp | null
```

---

## 5. Starnazzo di Gruppo (Broadcast)

Oltre allo starnazzo individuale, il mittente può starnazzare tutti i membri del gruppo contemporaneamente.

### Flusso
```
1. Dalla HomeScreen, il mittente preme "Starnazza Tutti" (pulsante dedicato)
2. Seleziona il livello di starnazzo (leggero/medio/pesante)
3. Si apre la StarnazzoStatusScreen in modalità broadcast:

   ┌─────────────────────────────────┐
   │                                 │
   │  🦆 Starnazzo a: Famiglia (4) │
   │                                 │
   │  Mario      ◉ Invio...         │
   │  Anna       ◉ Invio...         │
   │  Luca       ◉ Invio...         │
   │  Sara       ◉ Invio...         │
   │                                 │
   │  [Annulla]                      │
   │                                 │
   └─────────────────────────────────┘

4. Ogni membro viene aggiornato indipendentemente:

   ┌─────────────────────────────────┐
   │                                 │
   │  🦆 Starnazzo a: Famiglia (4) │
   │                                 │
   │  Mario      👍 Confermato      │
   │  Anna       ◉ Sta squillando.. │
   │  Luca       👎 Non disturbare  │
   │  Sara       ✗ Nessuna risposta │
   │                                 │
   │  [Chiudi]                       │
   │                                 │
   └─────────────────────────────────┘
```

---

## 6. Anti-Spam

Per prevenire abusi e proteggere i riceventi dal bombardamento di starnazzi.

### Limiti Rate
| Regola | Limite | Scope |
|---|---|---|
| Starnazzi per utente per ora | Max 10 | Per coppia mittente→ricevente |
| Starnazzi per utente per giorno | Max 30 | Per coppia mittente→ricevente |
| Starnazzi broadcast per ora | Max 3 | Per mittente per gruppo |
| Starnazzi pesanti per ora | Max 3 | Per mittente globale |
| Cooldown dopo starnazzo | 30 secondi | Per coppia mittente→ricevente |

### Implementazione
*   I limiti vengono verificati **lato server** nella Cloud Function prima di inviare il FCM.
*   Se il limite viene superato, la Cloud Function ritorna un errore e il mittente vede un messaggio: "Hai starnazzato troppo verso Mario. Riprova tra X minuti."
*   I contatori vengono salvati in un documento contatore con sliding window.
*   L'utente ricevente può anche segnalare un mittente come "spam" → l'admin del gruppo riceve una notifica.

---

## 7. Gestione Coda Starnazzi (Queue)

### Scenario: Ricezione di più starnazzi contemporanei
```
Starnazzo A arriva → IncomingAlertActivity si apre, suona
   Starnazzo B arriva durante A → B viene messo in coda
      L'utente risponde ad A → A si chiude
      → B si apre automaticamente (con un breve delay di 1 secondo)
```

### Scenario: Ricezione starnazzo mentre se ne sta inviando uno
```
L'utente sta sulla StarnazzoStatusScreen (ha inviato uno starnazzo a Mario)
   Arriva uno starnazzo da Anna →
   OPZIONE: Lo starnazzo in arrivo viene mostrato come overlay/dialog sopra la StarnazzoStatusScreen
      → L'utente può rispondere allo starnazzo in arrivo
      → La StarnazzoStatusScreen resta attiva sotto e continua ad aggiornarsi
      → Dopo la risposta, l'overlay si chiude e l'utente torna alla StarnazzoStatusScreen
```

### Implementazione
*   Un `StarnazzoQueueManager` (singleton) gestisce la coda FIFO degli starnazzi in arrivo.
*   Il `FirebaseMessagingService` aggiunge gli starnazzi alla coda anziché lanciarli direttamente.
*   Il QueueManager verifica se c'è già uno starnazzo attivo:
    *   Se no → lancia immediatamente l'`IncomingAlertActivity`.
    *   Se sì → accoda e mostra un badge/contatore: "Hai anche 2 altri starnazzi in attesa".
*   Quando uno starnazzo viene chiuso (risposta o timeout), il QueueManager lancia il successivo.

---

## 8. Caching e Gestione Foto Profilo

### Strategia
Le foto profilo vengono gestite con un approccio **cache-first con refresh in background**:

```
1. PRIMO ACCESSO (accettazione invito / ingresso nel gruppo):
   - L'app scarica le foto profilo di tutti i membri del gruppo
   - Le salva nella cache interna dell'app (Coil disk cache)
   - Associa a ogni foto un hash/timestamp dell'ultima modifica

2. VISUALIZZAZIONE (HomeScreen, IncomingAlertActivity, etc.):
   - Mostra SEMPRE la versione in cache (istantaneo, nessun ritardo)
   - In background: controlla se la foto è stata aggiornata (confronto hash/lastModified su Firestore)
   - Se aggiornata → scarica la nuova versione, aggiorna la cache, aggiorna la UI

3. RICEZIONE STARNAZZO (IncomingAlertActivity su lockscreen):
   - Usa SOLO la cache locale → la foto appare istantaneamente
   - Non fa richieste di rete per la foto (lo starnazzo deve essere veloce)
   - Se la foto non è in cache (caso raro) → mostra un avatar con le iniziali del nome
```

### Implementazione Tecnica
*   **Libreria immagini:** Coil (nativo Kotlin, ottima integrazione con Compose) con disco cache configurato.
*   **Firebase Storage:** Le foto profilo vengono salvate su Firebase Storage in `profilePhotos/{userId}.jpg`.
*   **Firestore metadata:** Il campo `photoUrl` in `users/{userId}` contiene l'URL; un campo `photoUpdatedAt` permette di sapere se la cache è stale.
*   **Dimensione cache:** Le foto vengono salvate in due risoluzioni:
    *   **Thumbnail (100x100):** Per le liste (HomeScreen, membri gruppo).
    *   **Full (300x300):** Per la schermata di ricezione starnazzo.
*   **Pulizia cache:** Quando un utente esce da un gruppo, le foto dei membri di quel gruppo (non condivisi con altri gruppi) vengono rimosse dalla cache.

---

## 9. Dashboard e Statistiche

Una schermata dedicata che mostra statistiche aggregate sull'uso degli starnazzi nel gruppo, con un tono divertente.

### Statistiche Previste

#### Statistiche Serie
| Statistica | Descrizione |
|---|---|
| Starnazzi inviati oggi/settimana/mese | Contatore personale |
| Starnazzi ricevuti oggi/settimana/mese | Contatore personale |
| Tempo medio di risposta | Quanto veloce rispondi agli starnazzi |
| Tasso di conferma vs rifiuto | % di pollici su vs giu |

#### Statistiche Divertenti
| Statistica | Titolo Scherzoso | Descrizione |
|---|---|---|
| Chi starnazza di più | "L'Oca del Villaggio" | Il membro che ha inviato più starnazzi nel gruppo |
| Chi viene starnazzato di più | "Il Ricercato" | Il membro che riceve più starnazzi |
| Chi risponde più veloce | "Flash" | Tempo medio di risposta più basso |
| Chi risponde più lento | "La Tartaruga" | Tempo medio di risposta più alto |
| Chi silenzia di più | "Il Fantasma" | Il membro che usa di più il mute |
| Chi manda più starnazzi pesanti | "L'Oca Furiosa" | Il membro più aggressivo coi HONK |
| Coppia più attiva | "I Piccioncini" | La coppia che si starnazza di più a vicenda |
| Ora di punta | "L'Ora del Caos" | L'ora del giorno con più starnazzi nel gruppo |
| Striscia più lunga di conferme | "Mr. Affidabile" | Più risposte consecutive con pollice su |

---

## 10. Monetizzazione (2 Tier: Free + Premium)

### Modello
L'app è **freemium**: Free generoso con ads, Premium senza ads e con tutto sbloccato.

| | **Free** | **Premium** (€2.99/mese o €19.99/anno) |
|---|---|---|
| **Starnazzi/giorno** | 10 per persona | Illimitati |
| **Gruppi** | 2 | Illimitati |
| **Livelli starnazzo** | Tutti e 3 (Grillo, Anatra, Oca) | Tutti e 3 |
| **Broadcast (Starnazza Tutti)** | 1 al giorno | Illimitato |
| **Dashboard statistiche** | Solo statistiche base | Tutte + statistiche divertenti |
| **Animali personalizzati** | No (solo i 3 default) | Si |
| **Ads** | Banner su HomeScreen | Nessuna |
| **Video Reward** | Si: +3 starnazzi extra guardando un video | Non necessario |

### Ads Policy
*   **Banner:** solo sulla HomeScreen, nella parte bassa. Mai durante ricezione starnazzo, mai sulla StarnazzoStatusScreen.
*   **Video Reward:** opzionale, l'utente sceglie di guardare un video per ottenere +3 starnazzi extra per la giornata. Disponibile solo quando si raggiunge il limite giornaliero.
*   **Nessun interstitial** — manteniamo l'esperienza fluida.

### Trigger di Conversione a Premium
*   "Ho 3+ gruppi (famiglia, lavoro, amici)" → serve Premium
*   "Sono stufo del banner" → Premium
*   "Voglio le statistiche divertenti (Oca del Villaggio, Flash...)" → Premium
*   "Voglio personalizzare gli animali" → Premium
*   "Mi servono più di 10 starnazzi/giorno" → Premium (o video reward nel Free)
*   "Voglio fare broadcast illimitati" → Premium

### Limiti Verificati Lato Server
I limiti del piano Free vengono verificati **sia lato client** (per UX istantanea) **sia nella Cloud Function** `sendStarnazzo` (per sicurezza):
*   La Cloud Function legge `users/{userId}.plan` e controlla i limiti prima di inviare.
*   Se il limite è raggiunto → errore con messaggio contestuale (es. "Hai finito gli starnazzi di oggi. Guarda un video per +3 oppure passa a Premium!")

### Implementazione Tecnica
*   **Google Play Billing Library** per gestire subscription (Premium mensile/annuale).
*   **Google AdMob SDK** per banner e video reward.
*   Campo `plan` in `users/{userId}`: `"free"` | `"premium"`.
*   Campo `planExpiresAt` in `users/{userId}`: timestamp di scadenza (per Premium).
*   Contatore giornaliero `dailyStarnazzoCount` e `dailyBroadcastCount` nei `rateLimits/`.
*   La Cloud Function `sendStarnazzo` verifica il piano e i limiti giornalieri.

### Stima Revenue (10.000 utenti attivi)

| Fonte | Stima/mese |
|---|---|
| Premium (2-3% conversione) | €600-900 |
| Ads banner (CPM ~€1-2) | €150-300 |
| Video Reward (~2 video/utente free/giorno) | €150-300 |
| **Totale ricorrente** | **€900-1.500** |
| **Costi Firebase** | **€30-50** |

---

## 11. Menu Utente (Profilo)

Accessibile cliccando sulla **foto profilo** nell'angolo in alto della HomeScreen. Si apre un bottom sheet o una schermata dedicata.

### Contenuto del Menu

```
┌─────────────────────────────────┐
│                                 │
│   [foto profilo grande]         │
│   Mario Rossi                   │
│   mario.rossi@gmail.com         │
│                                 │
│   ─────────────────────────     │
│                                 │
│   📷 Cambia foto profilo       │
│   ✏️ Cambia nome visualizzato  │
│                                 │
│   ─────────────────────────     │
│                                 │
│   ⭐ Piano: Free               │
│   [Passa a Premium →]          │
│                                 │
│   ─────────────────────────     │
│                                 │
│   ⚙️ Impostazioni app         │  → naviga a SettingsScreen
│   📊 Dashboard                 │  → naviga a DashboardScreen
│                                 │
│   ─────────────────────────     │
│                                 │
│   🚪 Logout                    │
│                                 │
└─────────────────────────────────┘
```

### Funzionalità

**Cambia foto profilo:**
*   Apre il picker immagini di sistema (nessun permesso CAMERA necessario, usa `ActivityResultContracts.PickVisualMedia`).
*   La foto selezionata viene **compressa automaticamente** prima dell'upload: ridimensionata a **300x300px** max e compressa in **JPEG qualità 80%** (~20-30KB).
*   Upload su Firebase Storage in `profilePhotos/{userId}.jpg`.
*   Il campo `photoUrl` e `photoUpdatedAt` in `users/{userId}` vengono aggiornati.
*   La foto si propaga ai gruppi tramite il campo `photoUrl` in `members/` (aggiornato dalla Cloud Function o dal client).

**Cambia nome visualizzato:**
*   Dialog con campo testo, precompilato col nome attuale.
*   Aggiorna `displayName` in `users/{userId}`.
*   Il nome si propaga ai gruppi (come per la foto).

**Piano e Premium:**
*   Mostra il piano attuale (Free/Premium).
*   Se Free: pulsante "Passa a Premium" → apre il flusso Google Play Billing.
*   Se Premium: mostra data scadenza, opzione per gestire abbonamento (link a Play Store).

**Logout:**
*   Conferma dialog: "Sei sicuro di voler uscire?"
*   Firebase Auth sign out → torna alla LoginScreen.

**Elimina account** (obbligatorio per policy Google Play Store):
*   Pulsante rosso in fondo al menu: "Elimina il mio account"
*   Dialog di conferma doppio: "Questa azione è irreversibile. Tutti i tuoi dati verranno cancellati."
*   Azione: Cloud Function `deleteUserAccount` che cancella:
    *   Documento `users/{userId}`
    *   Foto profilo da Storage
    *   Membership da tutti i gruppi
    *   MuteRules associate
    *   Firebase Auth account
*   Al completamento → torna alla LoginScreen.

---

## 12. Architettura Dettagliata

### 10.1 Decisioni Tecnologiche

| Area | Scelta | Motivazione |
|---|---|---|
| Linguaggio | Kotlin | Standard Android moderno |
| UI | Jetpack Compose | UI dichiarativa, reattiva, meno boilerplate |
| DI | Hilt | Compile-time safety, standard Google, già conosciuto |
| Architettura | MVVM + Repository Pattern | Separazione chiara UI/logica/dati |
| Navigation | Compose Navigation | Integrazione nativa con Compose |
| Image Loading | Coil | Nativo Kotlin, integrazione Compose |
| Backend | Firebase (Auth, Firestore, FCM, Storage) | Serverless, real-time, free tier generoso |
| Cloud Functions | TypeScript | Tipizzazione, consigliato da Firebase |
| Min SDK | 26 (Android 8.0) | Copre ~95% dispositivi, supporta notification channels |
| Target SDK | 34 (Android 14) | Ultimo stabile |

### 10.2 Package Structure (Android)

```
com.whereduck.app/
│
├── WhereTheDuckApp.kt                  ← Application class (@HiltAndroidApp)
├── MainActivity.kt                      ← Single Activity host per Compose Navigation
│
├── di/                                  ← Moduli Hilt
│   ├── AppModule.kt                     ← Singleton: Firebase instances, QueueManager
│   ├── RepositoryModule.kt             ← Binds dei Repository
│   └── ServiceModule.kt                ← AudioManager, NotificationManager, Vibrator
│
├── data/                                ← Layer dati
│   ├── model/                           ← Data classes (entità Firestore)
│   │   ├── User.kt
│   │   ├── Group.kt
│   │   ├── Member.kt
│   │   ├── GroupInvite.kt
│   │   ├── Alert.kt
│   │   ├── MuteRule.kt
│   │   ├── StarnazzoLevel.kt           ← Enum: LIGHT, MEDIUM, HEAVY
│   │   ├── Animal.kt                   ← Data class: id, level, soundRes, iconRes, motto
│   │   └── UserPlan.kt                 ← Enum: FREE, PREMIUM
│   │
│   ├── remote/                          ← Data sources Firebase
│   │   ├── FirestoreDataSource.kt       ← CRUD Firestore, snapshot listeners
│   │   ├── CloudFunctionsDataSource.kt  ← Chiamate alle Cloud Functions callable
│   │   ├── FcmTokenManager.kt          ← Gestione registrazione/aggiornamento token FCM
│   │   └── StorageDataSource.kt         ← Upload/download foto su Firebase Storage
│   │
│   └── repository/                      ← Repository (mediano tra remote e domain)
│       ├── AuthRepository.kt            ← Login/logout, current user
│       ├── GroupRepository.kt           ← CRUD gruppi, membri, inviti via email
│       ├── AlertRepository.kt           ← Invio starnazzi, listener stato, risposte
│       ├── MuteRepository.kt            ← CRUD mute rules
│       ├── StatsRepository.kt           ← Lettura statistiche
│       └── BillingRepository.kt         ← Google Play Billing, stato piano utente
│
├── domain/                              ← Logica di business pura (no Android deps)
│   ├── usecase/
│   │   ├── SendStarnazzoUseCase.kt      ← Valida input → chiama Cloud Function
│   │   ├── RespondToStarnazzoUseCase.kt ← Aggiorna response su Firestore
│   │   ├── MuteUserUseCase.kt           ← Crea mute rule
│   │   ├── InviteToGroupUseCase.kt      ← Invito membro via email
│   │   ├── RespondToInviteUseCase.kt    ← Accetta/rifiuta invito ricevuto
│   │   ├── CheckMuteStatusUseCase.kt    ← Verifica se un utente è silenziato
│   │   └── CheckPlanLimitsUseCase.kt    ← Verifica limiti piano (starnazzi/giorno, gruppi, broadcast)
│   │
│   └── manager/
│       └── StarnazzoQueueManager.kt     ← Coda FIFO starnazzi in arrivo (@Singleton)
│
├── service/                             ← Android Services
│   ├── StarnazzoFcmService.kt           ← FirebaseMessagingService: riceve FCM, accoda starnazzi
│   └── StarnazzoSoundService.kt         ← Foreground Service: gestisce progressione sonora 4 fasi
│
├── receiver/                            ← BroadcastReceivers
│   └── BootReceiver.kt                 ← Riavvia servizi necessari dopo reboot
│
├── notification/                        ← Gestione notifiche
│   ├── NotificationChannels.kt          ← Creazione canali (starnazzo, gruppi, sistema)
│   └── StarnazzoNotificationBuilder.kt  ← Costruisce notifica con fullScreenIntent
│
├── audio/                               ← Gestione audio e vibrazione
│   ├── DndManager.kt                   ← Bypass DND, controllo policy
│   ├── VolumeManager.kt                ← Alza volume suoneria se basso
│   └── VibrationPatterns.kt            ← Pattern vibrazione per le 4 fasi
│
├── ui/                                  ← Layer presentazione
│   ├── navigation/
│   │   └── AppNavGraph.kt              ← NavHost con tutte le rotte
│   │
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── StarnazzoThemes.kt          ← Temi per livello (verde/giallo/rosso)
│   │
│   ├── components/                      ← Composable riutilizzabili
│   │   ├── MemberCard.kt
│   │   ├── StarnazzoLevelSelector.kt
│   │   ├── StatusProgressIndicator.kt
│   │   └── AnimalAnimation.kt
│   │
│   ├── login/
│   │   ├── LoginScreen.kt
│   │   └── LoginViewModel.kt
│   │
│   ├── permissions/
│   │   ├── PermissionSetupScreen.kt
│   │   └── PermissionSetupViewModel.kt
│   │
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   │
│   ├── group/
│   │   ├── GroupManagementScreen.kt
│   │   └── GroupManagementViewModel.kt
│   │
│   ├── starnazzo/
│   │   ├── StarnazzoStatusScreen.kt
│   │   └── StarnazzoStatusViewModel.kt
│   │
│   ├── incoming/
│   │   ├── IncomingAlertActivity.kt     ← Activity separata (showWhenLocked)
│   │   ├── IncomingAlertScreen.kt       ← Composable contenuto
│   │   └── IncomingAlertViewModel.kt
│   │
│   ├── profile/
│   │   ├── ProfileMenuSheet.kt          ← Bottom sheet menu utente
│   │   └── ProfileViewModel.kt         ← Cambio foto/nome, logout, stato piano
│   │
│   ├── premium/
│   │   ├── PremiumScreen.kt            ← Schermata upsell Premium con features
│   │   └── PremiumViewModel.kt         ← Gestisce flusso Google Play Billing
│   │
│   ├── dashboard/
│   │   ├── DashboardScreen.kt
│   │   └── DashboardViewModel.kt
│   │
│   └── settings/
│       ├── SettingsScreen.kt
│       └── SettingsViewModel.kt
│
├── animal/
│   └── AnimalRegistry.kt               ← Registry animali: resolve con fallback, lista per livello
│
├── util/
│   ├── TimeUtils.kt                     ← "Fino a domani mattina" → timestamp
│   ├── OemBatteryHelper.kt              ← Rileva OEM, istruzioni specifiche per disabilitare ottimizzazione batteria
│   └── NetworkMonitor.kt                ← Monitora stato connessione con ConnectivityManager/NetworkCallback
│
└── ui/offline/
    └── OfflineOverlay.kt                ← Composable: anatra triste + messaggio "Sei offline"
```

### 10.3 Package Structure (Cloud Functions — TypeScript)

```
functions/
├── package.json
├── tsconfig.json
├── src/
│   ├── index.ts                         ← Export di tutte le functions
│   │
│   ├── starnazzo/
│   │   ├── sendStarnazzo.ts             ← Callable: invio singolo
│   │   ├── sendBroadcast.ts            ← Callable: invio broadcast
│   │   └── onAlertResponse.ts          ← Firestore trigger: notifica mittente
│   │
│   ├── groups/
│   │   ├── sendGroupInvite.ts          ← Callable: admin invita via email
│   │   └── respondGroupInvite.ts       ← Callable: destinatario accetta/rifiuta
│   │
│   ├── billing/
│   │   └── verifyPurchase.ts           ← Callable: verifica acquisto Play Store e aggiorna piano
│   │
│   ├── account/
│   │   └── deleteUserAccount.ts        ← Callable: cancellazione account e tutti i dati associati
│   │
│   ├── scheduled/
│   │   ├── expireStaleAlerts.ts        ← Scheduler: marca alert scaduti
│   │   ├── cleanExpiredMuteRules.ts    ← Scheduler: pulizia mute scadute
│   │   └── aggregateStats.ts           ← Scheduler: calcolo statistiche
│   │
│   ├── rateLimit/
│   │   └── rateLimiter.ts              ← Utility: verifica e aggiorna limiti + plan limits
│   │
│   └── util/
│       ├── fcmSender.ts                ← Wrapper invio FCM Data Message
│       ├── validators.ts               ← Validazione input delle callable
│       └── types.ts                    ← Interfacce TypeScript condivise
```

### 10.4 Navigation Map

```
                          ┌──────────────┐
                          │ App Launch   │
                          └──────┬───────┘
                                 │
                          ┌──────▼───────┐
                     ┌────│ LoginScreen  │
                     │    └──────┬───────┘
                     │           │ (auth ok)
                     │    ┌──────▼────────────┐
                     │    │ PermissionSetup   │──── (permessi già ok) ───┐
                     │    │ Screen            │                          │
                     │    └──────┬────────────┘                          │
                     │           │ (tutti i permessi concessi)           │
                     │    ┌──────▼───────┐◄──────────────────────────────┘
                     │    │              │
                     │    │  HomeScreen  │◄─────────────────────────────────┐
                     │    │              │                                   │
                     │    └──┬───┬───┬───┘                                   │
                     │       │   │   │                                      │
          ┌──────────┘  ┌────┘   │   └────┐                                │
          │             │        │        │                                │
   ┌──────▼──────┐ ┌────▼─────┐ │  ┌─────▼──────┐                        │
   │ Settings    │ │ Group    │ │  │ Dashboard  │                        │
   │ Screen      │ │ Manage   │ │  │ Screen     │                        │
   └─────────────┘ │ Screen   │ │  └────────────┘                        │
                   └──────────┘ │                                         │
                                │ (seleziona membro + livello)            │
                         ┌──────▼──────────────┐                          │
                         │ StarnazzoStatus     │──────────────────────────┘
                         │ Screen              │  (chiudi/completato)
                         └─────────────────────┘

   === FUORI DAL NAVIGATION GRAPH (Activity separata) ===

   ┌────────────────────────────┐
   │ IncomingAlertActivity      │  ← Lanciata da FCM Service via fullScreenIntent
   │ (sopra lockscreen)         │  ← Si chiude dopo risposta/timeout
   └────────────────────────────┘
```

**Rotte Navigation Compose:**
```kotlin
sealed class Route(val path: String) {
    object Login : Route("login")
    object PermissionSetup : Route("permissions")
    object Home : Route("home")
    object GroupManagement : Route("group/{groupId}")
    object StarnazzoStatus : Route("starnazzo/{alertId}")
    object StarnazzoBroadcastStatus : Route("starnazzo/broadcast/{broadcastId}")
    object Dashboard : Route("dashboard/{groupId}")
    object Settings : Route("settings")
    object Premium : Route("premium")
}
```

### 10.5 Data Flow

```
┌─────────────────────────────────────────────────────────────┐
│                        UI LAYER                             │
│  Screen (Composable) ◄──── ViewModel (StateFlow/UiState)   │
└──────────────────────────────┬──────────────────────────────┘
                               │ chiama UseCase / Repository
┌──────────────────────────────▼──────────────────────────────┐
│                      DOMAIN LAYER                           │
│  UseCase (logica di business, validazione)                  │
└──────────────────────────────┬──────────────────────────────┘
                               │ chiama Repository
┌──────────────────────────────▼──────────────────────────────┐
│                       DATA LAYER                            │
│  Repository ◄──── DataSource (Firestore, Functions, FCM)   │
└─────────────────────────────────────────────────────────────┘
```

**Pattern UiState per ogni schermata:**
```kotlin
// Ogni ViewModel espone un singolo StateFlow<UiState>
data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val groups: List<Group> = emptyList(),
    val selectedGroup: Group? = null,
    val members: List<Member> = emptyList(),
    val pendingInvites: List<GroupInvite> = emptyList()  // inviti ricevuti da accettare
)
```

---

## 13. Cloud Functions — Contratti Dettagliati

### Decisioni architetturali
*   **Token FCM:** vive **solo** in `users/{userId}.fcmToken`. Non viene duplicato in `members/`. Le Cloud Functions leggono il token da `users/` al momento dell'invio FCM.
*   **Piano utente:** il campo `plan` in `users/{userId}` è la fonte di verità. Le Cloud Functions lo leggono prima di ogni operazione limitata.

### 13.1 `sendStarnazzo` (HTTPS Callable)

```typescript
// INPUT
interface SendStarnazzoRequest {
  toUserId: string;
  groupId: string;
  level: "light" | "medium" | "heavy";
  animalType?: string;  // opzionale, default per il livello se omesso
}

// OUTPUT
interface SendStarnazzoResponse {
  alertId: string;
  status: "sent" | "rate_limited" | "plan_limited" | "error";
  retryAfterSeconds?: number;
  upgradeReason?: string;  // es. "daily_limit_reached", "broadcast_limit"
}

// LOGICA
1. Verifica auth (context.auth.uid esiste)
2. Legge users/{senderId} → ottiene plan, displayName
3. Verifica che mittente e destinatario appartengano a groupId
4. Controlla limiti del piano:
   - Free: max 10 starnazzi/giorno → se superato, return { status: "plan_limited", upgradeReason: "daily_limit_reached" }
5. Controlla rate limits anti-spam (rateLimiter.check(senderId, receiverId))
6. Se rate limited → return { status: "rate_limited", retryAfterSeconds }
5. Crea documento alerts/{alertId}:
   - fromUserId: context.auth.uid
   - fromDisplayName: (letto da users/)
   - toUserId, toDisplayName, groupId
   - starnazzoLevel: level
   - animalType: levelToAnimal(level)
   - status: "sending"
   - broadcastId: null
   - response: null
   - createdAt: serverTimestamp
6. Legge fcmToken da users/{toUserId}
7. Invia FCM Data Message:
   {
     data: { alertId, fromDisplayName, level, animalType, groupId },
     token: fcmToken,
     android: { priority: "high" }
   }
8. Aggiorna status → "delivered"
9. Aggiorna rate limit counters
10. Return { alertId, status: "sent" }
```

### 13.2 `sendBroadcastStarnazzo` (HTTPS Callable)

```typescript
// INPUT
interface SendBroadcastRequest {
  groupId: string;
  level: "light" | "medium" | "heavy";
  animalType?: string;
}

// OUTPUT
interface SendBroadcastResponse {
  broadcastId: string;
  alertIds: string[];        // un alertId per ogni membro
  failedMembers: string[];   // userId di chi non ha ricevuto (rate limited, no token, ecc.)
  status: "sent" | "partial" | "rate_limited";
}

// LOGICA
1. Verifica auth
2. Legge users/{senderId} → ottiene plan
3. Controlla limiti piano:
   - Free: max 1 broadcast/giorno → se superato, return { status: "plan_limited" }
4. Controlla rate limit broadcast anti-spam (max 3/ora per mittente per gruppo)
5. Legge tutti i members/ del gruppo (escluso il mittente)
4. Genera broadcastId univoco
5. Per ogni membro:
   a. Controlla rate limit individuale
   b. Crea alerts/{alertId} con broadcastId
   c. Legge fcmToken da users/{memberId}
   d. Invia FCM (o accoda in failedMembers se rate limited/no token)
6. Return { broadcastId, alertIds, failedMembers, status }
```

### 13.3 `onAlertResponse` (Firestore onUpdate trigger)

```typescript
// TRIGGER: alerts/{alertId} — campo response cambia da null a un valore

// LOGICA
1. Legge il documento alert aggiornato
2. Se response === null (non è una risposta) → skip
3. Se oldData.response !== null (già risposto) → skip
4. Aggiorna campo respondedAt → serverTimestamp
5. Aggiorna campo status → "responded"
6. Se response === "muted":
   a. Legge muteDuration dall'alert
   b. Crea/aggiorna muteRules/{toUserId_fromUserId}:
      - ownerId: toUserId (chi riceve e silenzia)
      - mutedUserId: fromUserId (chi viene silenziato)
      - muteUntil: now + muteDuration
7. Legge fcmToken da users/{fromUserId} (il mittente originale)
8. Invia FCM notification al mittente:
   {
     data: { alertId, response, respondedByName, muteDuration? },
     token: senderFcmToken
   }
```

### 13.4 `sendGroupInvite` (HTTPS Callable)

```typescript
// INPUT
interface SendGroupInviteRequest {
  groupId: string;
  email: string;
}

// OUTPUT
interface SendGroupInviteResponse {
  inviteId: string;
  status: "sent" | "user_not_found" | "already_member" | "already_invited";
}

// LOGICA
1. Verifica auth
2. Verifica che l'utente chiamante sia admin del gruppo
3. Cerca in users/ un documento con email === input.email
   - Se non trovato → return { status: "user_not_found" }
4. Controlla se l'utente è già membro del gruppo → "already_member"
5. Controlla se esiste già un invito pending per questo utente → "already_invited"
6. Crea groups/{groupId}/invites/{inviteId}:
   - invitedEmail, invitedUserId, invitedDisplayName
   - invitedBy: context.auth.uid
   - invitedByDisplayName: (letto da users/)
   - status: "pending"
   - createdAt: serverTimestamp
7. Legge fcmToken da users/{invitedUserId}
8. Invia FCM al destinatario: "Mario ti ha invitato in Famiglia"
9. Return { inviteId, status: "sent" }
```

### 13.5 `respondGroupInvite` (HTTPS Callable)

```typescript
// INPUT
interface RespondGroupInviteRequest {
  groupId: string;
  inviteId: string;
  accepted: boolean;
}

// OUTPUT
interface RespondGroupInviteResponse {
  status: "accepted" | "rejected" | "error";
}

// LOGICA
1. Verifica auth
2. Legge l'invito, verifica che invitedUserId === context.auth.uid
3. Verifica status === "pending"
4. Se accepted:
   a. Aggiorna invito status → "accepted", respondedAt → serverTimestamp
   b. Crea members/{invitedUserId} con role "member"
   c. Legge fcmToken da users/{invitedBy}
   d. Invia FCM all'admin: "Anna ha accettato l'invito a Famiglia!"
5. Se rifiutato:
   a. Aggiorna invito status → "rejected", respondedAt → serverTimestamp
   b. Invia FCM all'admin: "Anna ha rifiutato l'invito"
6. Return { status }
```

### 13.6 `expireStaleAlerts` (Cloud Scheduler — ogni minuto)

```typescript
// LOGICA:
1. Query: alerts WHERE status == "ringing" AND createdAt < (now - 60 sec)
2. Per ogni alert trovato:
   a. Aggiorna status → "expired"
   b. (Opzionale) invia FCM al mittente: "Nessuna risposta"
```

### 13.7 `cleanExpiredMuteRules` (Cloud Scheduler — ogni ora)

```typescript
// LOGICA:
1. Query: muteRules WHERE muteUntil < now
2. Batch delete di tutti i documenti trovati
// NOTA: function "nice to have". L'app funziona anche senza,
// perché il client controlla muteUntil > now.
```

### 13.8 `aggregateStats` (Cloud Scheduler — ogni notte, 3:00 AM)

```typescript
// LOGICA:
1. Per ogni gruppo attivo:
   a. Query tutti gli alerts del gruppo degli ultimi 7/30 giorni e all-time
   b. Calcola le classifiche:
      - mostSent (L'Oca del Villaggio)
      - mostReceived (Il Ricercato)
      - fastestResponder (Flash) — media di (respondedAt - createdAt)
      - slowestResponder (La Tartaruga)
      - mostMuted (Il Fantasma)
      - heaviestStarnazzer (L'Oca Furiosa) — count WHERE level == "heavy"
      - busiestPair (I Piccioncini) — coppia con più starnazzi bidirezionali
      - peakHour (L'Ora del Caos) — ora con più starnazzi
      - longestConfirmStreak (Mr. Affidabile)
   c. Salva in stats/{groupId}/rankings/
```

### 13.9 `verifyPurchase` (HTTPS Callable)

```typescript
// INPUT
interface VerifyPurchaseRequest {
  purchaseToken: string;
  productId: string;     // es. "premium_monthly" | "premium_yearly"
}

// OUTPUT
interface VerifyPurchaseResponse {
  status: "verified" | "invalid" | "already_used";
  plan: "premium" | null;
  expiresAt: string | null;
}

// LOGICA
1. Verifica auth
2. Valida il purchaseToken con Google Play Developer API (androidpublisher)
3. Se valido:
   a. Aggiorna users/{userId}.plan → "premium"
   b. Aggiorna users/{userId}.planExpiresAt → data scadenza dalla risposta Google
   c. Return { status: "verified", plan: "premium", expiresAt }
4. Se non valido → return { status: "invalid" }
// NOTA: serve configurare Google Play Developer API credentials nelle Cloud Functions
```

### 13.10 `deleteUserAccount` (HTTPS Callable)

```typescript
// INPUT: nessuno (usa context.auth.uid)

// OUTPUT
interface DeleteAccountResponse {
  status: "deleted" | "error";
}

// LOGICA
1. Verifica auth (context.auth.uid esiste)
2. Legge users/{userId} → ottiene lista gruppi
3. Per ogni gruppo:
   a. Rimuove membro da groups/{groupId}/members/{userId}
   b. Se era l'unico admin → promuove il membro più anziano, o cancella il gruppo se vuoto
4. Cancella tutti i documenti muteRules/ dove ownerId o mutedUserId == userId
5. Cancella foto profilo da Storage (profilePhotos/{userId}.jpg)
6. Cancella documento users/{userId}
7. Cancella account Firebase Auth (admin.auth().deleteUser(uid))
8. Return { status: "deleted" }
// NOTA: operazione in batch/transaction per evitare stati parziali
```

---

## 14. Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // === USERS ===
    match /users/{userId} {
      // Chiunque autenticato può leggere profili (per vedere nomi e foto dei membri)
      allow read: if request.auth != null;
      // Solo il proprietario può scrivere il proprio profilo
      allow write: if request.auth.uid == userId;
    }

    // === GROUPS ===
    match /groups/{groupId} {
      // Solo i membri del gruppo possono leggere i dati del gruppo
      allow read: if request.auth != null
        && exists(/databases/$(database)/documents/groups/$(groupId)/members/$(request.auth.uid));

      // Solo gli admin possono aggiornare il gruppo (nome, inviteCode)
      allow update: if request.auth != null
        && get(/databases/$(database)/documents/groups/$(groupId)/members/$(request.auth.uid)).data.role == "admin";

      // Chiunque autenticato può creare un gruppo (diventa admin tramite Cloud Function)
      allow create: if request.auth != null;

      // Solo gli admin possono eliminare il gruppo
      allow delete: if request.auth != null
        && get(/databases/$(database)/documents/groups/$(groupId)/members/$(request.auth.uid)).data.role == "admin";

      // --- MEMBERS subcollection ---
      match /members/{memberId} {
        // I membri del gruppo possono leggere la lista membri
        allow read: if request.auth != null
          && exists(/databases/$(database)/documents/groups/$(groupId)/members/$(request.auth.uid));

        // Scrittura gestita solo dalle Cloud Functions (admin operations)
        allow write: if false;
      }

      // --- INVITES subcollection ---
      match /invites/{inviteId} {
        // Admin possono leggere tutti gli inviti; l'invitato può leggere i propri
        allow read: if request.auth != null
          && (get(/databases/$(database)/documents/groups/$(groupId)/members/$(request.auth.uid)).data.role == "admin"
              || resource.data.invitedUserId == request.auth.uid);

        // Scrittura gestita solo dalle Cloud Functions
        allow write: if false;
      }
    }

    // Le seguenti collection sono leggibili in modo selettivo
    // ma la SCRITTURA è sempre gestita da Cloud Functions (server-side)

    // === ALERTS ===
    match /alerts/{alertId} {
      // Leggibile solo da mittente o destinatario
      allow read: if request.auth != null
        && (resource.data.fromUserId == request.auth.uid
            || resource.data.toUserId == request.auth.uid);

      // Solo il destinatario può aggiornare (per scrivere la risposta)
      allow update: if request.auth != null
        && resource.data.toUserId == request.auth.uid
        && request.resource.data.diff(resource.data).affectedKeys().hasOnly(['response', 'muteDuration']);

      // Creazione solo da Cloud Functions
      allow create: if false;
      allow delete: if false;
    }

    // === MUTE RULES ===
    match /muteRules/{ruleId} {
      // Leggibile solo dal proprietario della regola
      allow read: if request.auth != null
        && resource.data.ownerId == request.auth.uid;

      // Scrittura gestita da Cloud Functions (creazione via onAlertResponse)
      // Il proprietario può eliminare le proprie regole (unsilenzia manuale)
      allow delete: if request.auth != null
        && resource.data.ownerId == request.auth.uid;

      allow create, update: if false;
    }

    // === RATE LIMITS ===
    match /rateLimits/{limitId} {
      // Solo Cloud Functions
      allow read, write: if false;
    }

    // === STATS ===
    match /stats/{groupId} {
      // Leggibile dai membri del gruppo
      allow read: if request.auth != null
        && exists(/databases/$(database)/documents/groups/$(groupId)/members/$(request.auth.uid));

      // Scrittura solo da Cloud Functions
      allow write: if false;

      match /rankings/{rankingType} {
        allow read: if request.auth != null
          && exists(/databases/$(database)/documents/groups/$(groupId)/members/$(request.auth.uid));
        allow write: if false;
      }
    }
  }
}
```

---

## 15. Struttura Dati Firestore (Completa e Definitiva)

```
users/{userId}
    - displayName: string
    - email: string
    - photoUrl: string
    - photoUpdatedAt: timestamp
    - fcmToken: string              ← UNICA fonte del token FCM (no duplicazione)
    - plan: "free" | "premium"      ← Piano attivo dell'utente
    - planExpiresAt: timestamp | null  ← Scadenza Premium (null se free)
    - createdAt: timestamp

groups/{groupId}
    - name: string
    - createdBy: string (userId)
    - createdAt: timestamp

    members/{userId}                 ← NO fcmToken qui (letto da users/)
        - displayName: string
        - photoUrl: string
        - role: "admin" | "member"
        - joinedAt: timestamp

    invites/{inviteId}
        - invitedEmail: string
        - invitedUserId: string
        - invitedDisplayName: string
        - invitedBy: string (userId admin)
        - invitedByDisplayName: string
        - status: "pending" | "accepted" | "rejected"
        - createdAt: timestamp
        - respondedAt: timestamp | null

alerts/{alertId}
    - fromUserId: string
    - fromDisplayName: string
    - toUserId: string
    - toDisplayName: string
    - groupId: string
    - broadcastId: string | null
    - starnazzoLevel: "light" | "medium" | "heavy"
    - animalType: "cricket" | "duck" | "goose"
    - status: "sending" | "delivered" | "ringing" | "responded" | "expired" | "failed"
    - response: "confirmed" | "rejected" | "muted" | null
    - muteDuration: number | null    (minuti)
    - createdAt: timestamp
    - deliveredAt: timestamp | null
    - respondedAt: timestamp | null

muteRules/{ownerId_mutedUserId}
    - ownerId: string
    - mutedUserId: string
    - mutedDisplayName: string
    - muteUntil: timestamp
    - createdAt: timestamp

rateLimits/{senderId_receiverId}
    - hourlyCount: number
    - dailyCount: number
    - lastStarnazzoAt: timestamp
    - windowStart: timestamp

stats/{groupId}
    - updatedAt: timestamp
    rankings/{rankingType}
        - entries: [{ userId: string, displayName: string, value: number }]
```

---

## 16. Flussi dell'Applicazione

### 14.1 Flusso Lato Mittente (Invio Starnazzo Singolo)
```
1. Dalla HomeScreen, il mittente seleziona un membro del gruppo
2. Seleziona il livello di starnazzo (leggero/medio/pesante) → appare l'animale corrispondente
3. Preme il pulsante "Starnazza!"
4. Si apre la StarnazzoStatusScreen — schermata di monitoraggio in tempo reale:

   ┌─────────────────────────────────┐
   │                                 │
   │     🦆 Starnazzo a: Mario      │
   │                                 │
   │     ◉ Invio in corso...         │  ← stato: "sending"
   │     ○ Consegnato                │
   │     ○ In attesa di risposta     │
   │                                 │
   │     [Annulla]                   │
   │                                 │
   └─────────────────────────────────┘

5. Cloud Function valida (rate limit check) → invia FCM Data Message
6. Firestore aggiorna lo stato → listener real-time aggiorna la UI:

   ┌─────────────────────────────────┐
   │                                 │
   │     🦆 Starnazzo a: Mario      │
   │                                 │
   │     ✓ Inviato                   │
   │     ✓ Consegnato                │
   │     ◉ Sta starnazzando...       │  ← stato: "ringing"
   │                                 │
   │     [Annulla]                   │
   │                                 │
   └─────────────────────────────────┘

7. Il ricevente risponde → la UI mostra il risultato (4 casi: conferma, rifiuto, mute, timeout)
8. Il mittente può chiudere la schermata o riprovare (in caso di timeout).
```
**Implementazione tecnica:** La StarnazzoStatusScreen utilizza un listener real-time su Firestore (`snapshotListener` sul documento `alerts/{alertId}`). Il timeout di 60 secondi è gestito lato client + Cloud Function schedulata che marca come "expired".

### 14.2 Flusso Lato Ricevente (Ricezione Starnazzo)
```
1. FCM Data Message → StarnazzoFcmService
2. StarnazzoQueueManager verifica:
   a. C'è uno starnazzo attivo? → accoda
   b. Il mittente è silenziato? → notifica normale, aggiorna stato a "delivered"
   c. Altrimenti → continua
3. Aggiorna stato alert su Firestore: "delivered" → "ringing"
4. Crea notifica con setFullScreenIntent(), lancia IncomingAlertActivity
5. Avvia StarnazzoSoundService (Foreground Service) per la progressione sonora a 4 fasi
6. Bypass DND + volume alzato tramite DndManager e VolumeManager
7. L'utente vede la schermata full-screen con l'animale, "Where the [Animal] are you?!" e pulsanti di risposta
8. La risposta viene salvata su Firestore → trigger onAlertResponse → FCM al mittente
9. Il suono si ferma e la schermata si chiude
10. Se ci sono starnazzi in coda → il successivo parte dopo 1 secondo
```

### 14.3 Flusso Timeout e Casi Limite
*   **Timeout starnazzo (60 secondi):** Suono si ferma, schermata si chiude, alert marcato "expired".
*   **Starnazzo durante starnazzo (ricevente):** Coda FIFO gestita dal StarnazzoQueueManager, badge "2 starnazzi in attesa".
*   **Starnazzo in arrivo mentre se ne invia uno (ricevente è anche mittente):** Overlay/dialog sopra la StarnazzoStatusScreen.
*   **App chiusa/processo killato:** FCM Data Messages ad alta priorità risvegliano il service.
*   **Connessione assente:** FCM ritenta la consegna. Mittente vede stato fermo su "Invio in corso...".
*   **Rate limit superato:** Cloud Function blocca l'invio. Mittente vede messaggio di errore con countdown.

---

## 17. Schermate dell'App (Overview)

| Schermata | Descrizione |
|---|---|
| **LoginScreen** | Google Sign-In |
| **PermissionSetupScreen** | Onboarding guidato per abilitare tutti i permessi necessari |
| **HomeScreen** | Lista dei membri del gruppo con pulsante starnazzo per ciascuno + pulsante "Starnazza Tutti" + banner ad (Free) |
| **ProfileMenuSheet** | Bottom sheet: foto, nome, piano, settings, logout. Si apre cliccando sulla foto profilo |
| **PremiumScreen** | Upsell Premium con lista features, prezzi e flusso acquisto Google Play |
| **GroupManagementScreen** | Crea gruppo, invita membri via email, gestisci inviti pendenti, gestisci membri |
| **StarnazzoStatusScreen** | Monitoraggio real-time dello starnazzo inviato — singolo o broadcast (lato mittente) |
| **IncomingAlertActivity** | Schermata full-screen sopra lockscreen con animale dello starnazzo (lato ricevente) |
| **DashboardScreen** | Statistiche serie e divertenti del gruppo (complete solo per Premium) |
| **SettingsScreen** | Personalizzazione animali/suoni, gestione silenziamenti attivi, permessi, link Privacy Policy |
| **OfflineOverlay** | Overlay con anatra triste e messaggio "Sei offline" — appare automaticamente quando manca la connessione |

---

## 18. Requisiti di Sistema e Permessi

### 18.1 Permessi Android
Per garantire il corretto funzionamento, l'app dovrà richiedere:
1.  **Accesso alle Notifiche:** `POST_NOTIFICATIONS` (Android 13+).
2.  **Accesso "Non Disturbare":** `ACCESS_NOTIFICATION_POLICY`.
3.  **Esclusione dall'Ottimizzazione Batteria:** `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.
4.  **Full-Screen Intent:** `USE_FULL_SCREEN_INTENT` (Android 14+ richiede abilitazione manuale).
5.  **Internet:** `INTERNET`, `ACCESS_NETWORK_STATE`.
6.  **Vibrazione:** `VIBRATE`.
7.  **Foreground Service:** `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MEDIA_PLAYBACK` (per StarnazzoSoundService).

### 18.2 OEM Battery Killers (Critico)
Molti produttori Android (Xiaomi/MIUI, Huawei/EMUI, Samsung OneUI, OPPO/ColorOS, Vivo/FuntouchOS) uccidono aggressivamente le app in background, impedendo la consegna delle notifiche FCM anche se high-priority.

**Soluzione:** La `PermissionSetupScreen` deve:
1.  **Rilevare il produttore** del dispositivo (`Build.MANUFACTURER`).
2.  **Mostrare istruzioni specifiche per OEM** con deep link alle impostazioni:
    *   **Xiaomi:** Impostazioni → App → WhereTheDuck → Risparmio energetico → "Nessuna restrizione" + Autostart abilitato
    *   **Huawei:** Impostazioni → Batteria → Avvio app → WhereTheDuck → Gestisci manualmente (tutto abilitato)
    *   **Samsung:** Impostazioni → Cura dispositivo → Batteria → Limiti utilizzo in background → rimuovere WhereTheDuck
    *   **OPPO/Vivo:** Impostazioni → Batteria → Risparmio energetico → WhereTheDuck → Non limitare
    *   **Altro:** Istruzione generica per disabilitare ottimizzazione batteria
3.  **Verificare periodicamente** se l'impostazione è stata rimossa (alcuni OEM la resettano dopo aggiornamenti).

**Implementazione:** Utility `OemBatteryHelper.kt` in `util/` che mappa `Build.MANUFACTURER` → istruzioni + intent per aprire la schermata corretta.

### 18.3 Comportamento Offline
L'app richiede connessione internet per funzionare (invio/ricezione starnazzi, gestione gruppi).

**Quando l'utente è offline:**
*   L'app mostra una schermata/overlay con un'**anatra triste** e il messaggio: *"Mi dispiace, sei offline! 🦆💔 Riconnettiti per starnazzare."*
*   L'app monitora lo stato della connessione con `ConnectivityManager` e un `NetworkCallback`.
*   Appena la connessione torna, l'overlay scompare automaticamente.
*   Nessuna azione viene messa in coda offline (gli starnazzi richiedono Cloud Functions).

### 18.4 Limitazione: App in "Forza Interruzione"
Se l'utente va in Impostazioni → App → WhereTheDuck → "Forza interruzione", Android uccide completamente l'app e **FCM non può più consegnare messaggi**. L'app torna a funzionare solo al prossimo avvio manuale.

**Non c'è soluzione tecnica** a questo — è una limitazione di Android. L'app documenterà questo limite:
*   Nella PermissionSetupScreen: avviso "Non forzare la chiusura dell'app dalle impostazioni"
*   Nelle eventuali FAQ / Help

---

## 19. Setup Firebase (Azioni Manuali)

Queste operazioni devono essere eseguite manualmente nella console Firebase **prima** di iniziare l'implementazione:

### 19.1 Creazione progetto
1. Creare progetto Firebase: "WhereTheDuck"
2. Aggiungere app Android con package name `com.whereduck.app`
3. Scaricare `google-services.json` e posizionarlo in `app/`

### 19.2 Abilitare servizi
1. **Authentication** → Abilitare provider "Google"
2. **Firestore** → Creare database in production mode (le rules verranno deployate dal codice)
3. **Cloud Storage** → Abilitare per foto profilo
4. **Cloud Messaging** → Abilitato di default

### 19.3 Cloud Functions
1. Abilitare piano **Blaze** (pay-as-you-go, richiesto per Cloud Functions e FCM server-side)
2. `firebase init functions` → selezionare TypeScript

### 19.4 Configurazione SHA per Google Sign-In
1. Aggiungere SHA-1 e SHA-256 del certificato di debug nella console Firebase (Impostazioni progetto → App Android)
2. Per produzione: aggiungere anche le SHA del keystore di rilascio

### 19.5 Crashlytics e Analytics
1. **Crashlytics** → Abilitare nella console Firebase (sezione "Crashlytics"). Raccoglie automaticamente crash e ANR, visibili nella dashboard Firebase.
2. **Analytics** → Abilitato di default. Traccia eventi come starnazzi inviati, login, acquisti.

### 19.6 Configurazione AdMob
1. Creare account AdMob su [admob.google.com](https://admob.google.com)
2. Registrare l'app Android con package name `com.whereduck.app`
3. Creare ad unit per **Banner** (home screen, dashboard)
4. Creare ad unit per **Rewarded Video** (starnazzi extra giornalieri)
5. Aggiungere l'App ID AdMob nel `AndroidManifest.xml`:
   ```xml
   <meta-data
       android:name="com.google.android.gms.ads.APPLICATION_ID"
       android:value="ca-app-pub-XXXXXXXX~YYYYYYYY"/>
   ```

---

## 20. Privacy Policy, ProGuard e Note di Rilascio

### 20.1 Privacy Policy e Terms of Service
Obbligatorie per la pubblicazione su Google Play Store, specialmente con Google Sign-In e raccolta dati utente.

*   **Hosting:** Firebase Hosting (oppure pagine aggiuntive sul sito esistente dell'altra app).
*   **Contenuto:** Descrizione dati raccolti (email, nome, foto profilo, dati di utilizzo), finalità, retention, diritti dell'utente (cancellazione account).
*   **URL:** Devono essere inseriti nella Google Play Console e nel footer dell'app (SettingsScreen).

### 20.2 ProGuard / R8
Per il build di rilascio (APK/AAB per il Play Store), R8 offusca e ottimizza il codice.

*   **Configurazione:** `proguard-rules.pro` con regole per:
    *   Firebase (classi usate via reflection)
    *   Hilt/Dagger (generazione codice)
    *   Coil (se necessario)
    *   Google Play Billing
*   Verrà configurato durante il setup progetto (Fase 1) e testato prima del rilascio.

### 20.3 Crashlytics e Analytics
*   **Firebase Crashlytics:** raccoglie automaticamente crash, ANR e eccezioni non gestite. Dashboard nella console Firebase.
*   **Firebase Analytics:** traccia eventi automatici (session, screen views) + eventi custom (starnazzo inviato, upgrade premium, ecc.).
*   Entrambi sono gratuiti e richiedono zero codice aggiuntivo per il funzionamento base (si attivano con la sola dipendenza).

---

## 21. Dipendenze Android (build.gradle)

```kotlin
// Project-level
plugins {
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    id("com.google.firebase.crashlytics")
}

// App-level dependencies
dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-functions-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Coil (image loading)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Google Play Billing (acquisti in-app / subscription)
    implementation("com.android.billingclient:billing-ktx:6.1.0")

    // Google AdMob (banner + video reward)
    implementation("com.google.android.gms:play-services-ads:23.0.0")
}
```

---

## 22. Contratto Fase per Fase

Ogni fase specifica: precondizioni, cosa produce, file da creare, criteri di "done".

---

### FASE 1 — Setup Progetto e Firebase

**Precondizioni:** Progetto Firebase creato, `google-services.json` scaricato.

**Produce:**
- Progetto Android compilabile con Compose + Hilt + Firebase
- Struttura package completa (cartelle vuote dove serve)
- App che si avvia e mostra una schermata placeholder
- Firebase inizializzato e connesso

**File da creare:**
- `build.gradle.kts` (project + app) con tutte le dipendenze
- `WhereTheDuckApp.kt` (@HiltAndroidApp)
- `MainActivity.kt` (ComponentActivity con setContent)
- `di/AppModule.kt` (Firebase instances)
- `ui/theme/` (Theme, Color, Type)
- `ui/navigation/AppNavGraph.kt` (placeholder routes)
- Cartelle vuote per tutta la package structure

**Criteri di Done:**
- [ ] `./gradlew assembleDebug` compila senza errori
- [ ] L'app si avvia sull'emulatore e mostra una schermata
- [ ] Firebase è inizializzato (nessun crash al boot)
- [ ] Hilt è configurato (@HiltAndroidApp + @AndroidEntryPoint)

---

### FASE 2 — Auth e Permessi

**Precondizioni:** Fase 1 completata. Google Sign-In abilitato su Firebase. SHA-1 configurato.

**Produce:**
- Flusso di login completo con Google Sign-In
- Creazione automatica documento `users/{userId}` al primo login
- Registrazione token FCM in `users/{userId}.fcmToken`
- Schermata onboarding permessi che guida l'utente ad abilitare tutti i permessi (incluse istruzioni OEM-specifiche)
- Monitoraggio connessione e overlay offline con anatra triste

**File da creare/modificare:**
- `data/model/User.kt`
- `data/remote/FirestoreDataSource.kt` (createUser, updateFcmToken)
- `data/remote/FcmTokenManager.kt`
- `data/repository/AuthRepository.kt`
- `ui/login/LoginScreen.kt` + `LoginViewModel.kt`
- `ui/permissions/PermissionSetupScreen.kt` + `PermissionSetupViewModel.kt`
- `util/OemBatteryHelper.kt` (rilevamento OEM + istruzioni specifiche per disabilitare ottimizzazione batteria)
- `util/NetworkMonitor.kt` (ConnectivityManager + NetworkCallback)
- `ui/offline/OfflineOverlay.kt` (anatra triste + messaggio "Sei offline")
- `service/StarnazzoFcmService.kt` (solo onNewToken per ora)
- `ui/navigation/AppNavGraph.kt` (aggiungere rotte login → permissions → home)

**Criteri di Done:**
- [ ] L'utente può fare login con Google
- [ ] Al primo login viene creato il documento su Firestore
- [ ] Il token FCM viene salvato su `users/{userId}`
- [ ] La PermissionSetupScreen mostra lo stato di ogni permesso
- [ ] La PermissionSetupScreen mostra istruzioni OEM-specifiche per battery optimization (Xiaomi, Huawei, Samsung, OPPO)
- [ ] Senza connessione internet → overlay con anatra triste e messaggio "Sei offline"
- [ ] Quando la connessione torna → overlay scompare automaticamente
- [ ] Dopo aver concesso tutti i permessi, navigazione verso HomeScreen (placeholder)
- [ ] Logout funzionante

---

### FASE 3 — Gruppi e Inviti via Email

**Precondizioni:** Fase 2 completata. L'utente è autenticato.

**Produce:**
- Creazione gruppi
- Invito membri via email (admin invita → destinatario accetta/rifiuta)
- Cloud Functions: `sendGroupInvite`, `respondGroupInvite`

**File da creare/modificare:**
- `data/model/Group.kt`, `Member.kt`, `GroupInvite.kt`
- `data/remote/FirestoreDataSource.kt` (CRUD gruppi)
- `data/remote/CloudFunctionsDataSource.kt`
- `data/repository/GroupRepository.kt`
- `domain/usecase/InviteToGroupUseCase.kt`
- `domain/usecase/RespondToInviteUseCase.kt`
- `ui/group/GroupManagementScreen.kt` + `GroupManagementViewModel.kt`
- `notification/NotificationChannels.kt` (canale "gruppi")
- **Cloud Functions:** `src/groups/sendGroupInvite.ts`, `respondGroupInvite.ts`
- **Firestore Rules:** deploy regole per `groups/`, `invites/`

**Criteri di Done:**
- [ ] Creazione gruppo con nome → appare su Firestore
- [ ] Admin inserisce email → Cloud Function invia invito
- [ ] Se email non registrata → errore "Utente non trovato"
- [ ] Se già membro → errore "Già membro"
- [ ] Il destinatario riceve notifica FCM
- [ ] Il destinatario vede l'invito pendente nell'app
- [ ] Accettazione aggiunge il membro alla subcollection `members/`
- [ ] Rifiuto notifica l'admin
- [ ] Admin vede lista inviti pendenti nella GroupManagementScreen

---

### FASE 4 — HomeScreen e Invio Starnazzo

**Precondizioni:** Fase 3 completata. Almeno un gruppo con 2+ membri.

**Produce:**
- HomeScreen con lista membri del gruppo e selezione livello starnazzo
- Cloud Functions: `sendStarnazzo`, `sendBroadcastStarnazzo` + rate limiter
- Invio starnazzo funzionante (FCM parte, ma il ricevente non fa ancora nulla)

**File da creare/modificare:**
- `data/model/Alert.kt`, `StarnazzoLevel.kt`
- `data/remote/CloudFunctionsDataSource.kt` (sendStarnazzo, sendBroadcast)
- `data/repository/AlertRepository.kt`
- `domain/usecase/SendStarnazzoUseCase.kt`
- `ui/home/HomeScreen.kt` + `HomeViewModel.kt`
- `ui/components/MemberCard.kt`, `StarnazzoLevelSelector.kt`
- **Cloud Functions:** `src/starnazzo/sendStarnazzo.ts`, `sendBroadcast.ts`, `src/rateLimit/rateLimiter.ts`
- **Cloud Functions:** `src/util/fcmSender.ts`, `validators.ts`, `types.ts`
- **Firestore Rules:** deploy regole per `alerts/`, `rateLimits/`

**Criteri di Done:**
- [ ] HomeScreen mostra lista membri del gruppo selezionato
- [ ] Selettore livello starnazzo (grillo/anatra/oca) funziona
- [ ] Pulsante "Starnazza!" chiama la Cloud Function
- [ ] Il documento `alerts/{alertId}` viene creato su Firestore
- [ ] FCM Data Message viene inviato (verificabile nei log Cloud Functions)
- [ ] Rate limiting funziona (errore dopo troppi starnazzi)
- [ ] Pulsante "Starnazza Tutti" per broadcast funziona

---

### FASE 5 — Ricezione Starnazzo (Lato Ricevente)

**Precondizioni:** Fase 4 completata. FCM Data Message arriva al dispositivo.

**Produce:**
- StarnazzoFcmService riceve il push e lancia IncomingAlertActivity
- IncomingAlertActivity si mostra sopra il lockscreen
- Progressione sonora a 4 fasi (vibrazione → suono → insistenza)
- Bypass DND e volume alzato
- Timeout 60 secondi

**File da creare/modificare:**
- `service/StarnazzoFcmService.kt` (onMessageReceived completo)
- `service/StarnazzoSoundService.kt` (Foreground Service, progressione 4 fasi)
- `ui/incoming/IncomingAlertActivity.kt` + `IncomingAlertScreen.kt` + `IncomingAlertViewModel.kt`
- `audio/DndManager.kt`
- `audio/VolumeManager.kt`
- `audio/VibrationPatterns.kt`
- `notification/NotificationChannels.kt` (canale starnazzo)
- `notification/StarnazzoNotificationBuilder.kt`
- `ui/theme/StarnazzoThemes.kt` (verde/giallo/rosso)
- `AndroidManifest.xml` (IncomingAlertActivity con flags, permissions, service declaration)
- File audio placeholder in `res/raw/` (cricket.mp3, duck.mp3, goose.mp3)

**Criteri di Done:**
- [ ] FCM arriva → IncomingAlertActivity appare (anche con telefono bloccato)
- [ ] Lo schermo si accende
- [ ] La progressione sonora parte (vibrazione → suono)
- [ ] Il DND viene bypassato
- [ ] Il volume viene alzato se basso
- [ ] Dopo 60 secondi la schermata si chiude automaticamente
- [ ] I 3 livelli mostrano animale/tema/suono diverso
- [ ] L'animazione cambia in sincrono con le fasi sonore

---

### FASE 6 — StarnazzoStatusScreen (Lato Mittente)

**Precondizioni:** Fase 5 completata. Lo starnazzo arriva al ricevente.

**Produce:**
- StarnazzoStatusScreen con tracking in tempo reale
- Listener Firestore sullo stato dell'alert
- Modalità broadcast (tracking multi-destinatario)

**File da creare/modificare:**
- `ui/starnazzo/StarnazzoStatusScreen.kt` + `StarnazzoStatusViewModel.kt`
- `data/repository/AlertRepository.kt` (aggiungere snapshotListener)
- `ui/components/StatusProgressIndicator.kt`
- `ui/navigation/AppNavGraph.kt` (rotta StarnazzoStatus)

**Criteri di Done:**
- [ ] Dopo l'invio, la StarnazzoStatusScreen si apre
- [ ] Lo stato si aggiorna in tempo reale (sending → delivered → ringing)
- [ ] In modalità broadcast, ogni membro ha il suo stato indipendente
- [ ] Il pulsante "Annulla" chiude la schermata
- [ ] Dopo timeout, mostra "Nessuna risposta" + pulsante "Riprova"

---

### FASE 7 — Risposte Rapide (Bidirezionale)

**Precondizioni:** Fase 6 completata. Mittente e ricevente vedono i rispettivi stati.

**Produce:**
- Pulsanti risposta (pollice su/giu/mute) sulla IncomingAlertActivity
- La risposta aggiorna Firestore → trigger → FCM al mittente
- Il mittente vede la risposta in tempo reale sulla StarnazzoStatusScreen
- Cloud Function: `onAlertResponse`
- Sistema mute con durate prefissate

**File da creare/modificare:**
- `ui/incoming/IncomingAlertScreen.kt` (aggiungere pulsanti risposta)
- `ui/incoming/IncomingAlertViewModel.kt` (logica risposta)
- `domain/usecase/RespondToStarnazzoUseCase.kt`
- `domain/usecase/MuteUserUseCase.kt`
- `data/model/MuteRule.kt`
- `data/repository/MuteRepository.kt`
- `domain/usecase/CheckMuteStatusUseCase.kt`
- `util/TimeUtils.kt`
- **Cloud Functions:** `src/starnazzo/onAlertResponse.ts`
- **Firestore Rules:** deploy regole per `muteRules/`

**Criteri di Done:**
- [ ] Pulsante 👍 → risposta "confirmed" su Firestore → mittente vede la conferma
- [ ] Pulsante 👎 → risposta "rejected" su Firestore → mittente vede il rifiuto
- [ ] Pulsante 🔇 → mostra selettore durata → crea muteRule
- [ ] Starnazzi da mittente silenziato → notifica normale (no full-screen, no suono)
- [ ] Il mute scade automaticamente dopo la durata scelta
- [ ] Il mittente vede la risposta "muted" + durata sulla StarnazzoStatusScreen

---

### FASE 8 — Coda Starnazzi

**Precondizioni:** Fase 7 completata. Starnazzi funzionano end-to-end.

**Produce:**
- StarnazzoQueueManager gestisce starnazzi concorrenti
- Badge "N starnazzi in attesa" sulla IncomingAlertActivity
- Gestione caso: starnazzo in arrivo durante invio

**File da creare/modificare:**
- `domain/manager/StarnazzoQueueManager.kt`
- `service/StarnazzoFcmService.kt` (usa QueueManager invece di lanciare direttamente)
- `ui/incoming/IncomingAlertScreen.kt` (badge contatore coda)

**Criteri di Done:**
- [ ] Se arriva starnazzo B mentre A è attivo → B in coda
- [ ] Dopo chiusura A → B si apre dopo 1 secondo
- [ ] Badge mostra quanti starnazzi in attesa
- [ ] Se l'utente è sulla StarnazzoStatusScreen e riceve uno starnazzo → overlay

---

### FASE 9 — Dashboard Statistiche

**Precondizioni:** Fase 7 completata. Ci sono dati sufficienti nella collection `alerts/`.

**Produce:**
- DashboardScreen con statistiche serie e divertenti
- Cloud Function: `aggregateStats`
- Cards animate con classifiche

**File da creare/modificare:**
- `data/repository/StatsRepository.kt`
- `ui/dashboard/DashboardScreen.kt` + `DashboardViewModel.kt`
- **Cloud Functions:** `src/scheduled/aggregateStats.ts`
- **Firestore Rules:** deploy regole per `stats/`

**Criteri di Done:**
- [ ] La Cloud Function calcola e salva le statistiche
- [ ] La DashboardScreen mostra le classifiche divertenti
- [ ] Filtro per periodo (settimana/mese/sempre) funziona
- [ ] Le cards mostrano l'animale/titolo scherzoso appropriato

---

### FASE 10 — Settings

**Precondizioni:** Fase 7 completata.

**Produce:**
- SettingsScreen con personalizzazione animali/suoni
- Lista silenziamenti attivi con possibilità di rimuoverli
- Gestione permessi (link alle impostazioni di sistema)

**File da creare/modificare:**
- `ui/settings/SettingsScreen.kt` + `SettingsViewModel.kt`
- `ui/components/AnimalAnimation.kt`

**Criteri di Done:**
- [ ] L'utente vede i silenziamenti attivi e può rimuoverli
- [ ] Link alle impostazioni di sistema per i permessi
- [ ] (Futuro) Selezione animali personalizzati per livello

---

### FASE 11 — Cloud Functions Schedulati e Polish

**Precondizioni:** Tutte le fasi precedenti completate.

**Produce:**
- Cloud Functions schedulati: `expireStaleAlerts`, `cleanExpiredMuteRules`
- Caching foto profilo ottimizzato con Coil
- Polish UI, animazioni, transizioni
- Testing end-to-end

**File da creare/modificare:**
- **Cloud Functions:** `src/scheduled/expireStaleAlerts.ts`, `cleanExpiredMuteRules.ts`
- Configurazione Coil disk cache
- `receiver/BootReceiver.kt`
- Testing e bug fixing

**Criteri di Done:**
- [ ] Alert scaduti vengono marcati automaticamente ogni minuto
- [ ] MuteRules scadute vengono pulite ogni ora
- [ ] Foto profilo caricate da cache locale senza ritardo
- [ ] L'app sopravvive a reboot del dispositivo
- [ ] Test end-to-end su 2+ dispositivi completato

---

### FASE 12 — Menu Utente (Profilo)

**Precondizioni:** Fase 2 completata (auth funzionante).

**Produce:**
- Bottom sheet del profilo utente (click su foto profilo)
- Cambio foto profilo (upload su Cloud Storage)
- Cambio nome visualizzato
- Visualizzazione piano corrente (Free/Premium)
- Logout

**File da creare/modificare:**
- `ui/profile/ProfileMenuSheet.kt`
- `data/remote/StorageDataSource.kt` (upload/download foto)
- `data/repository/AuthRepository.kt` (aggiornare nome/foto)
- `ui/home/HomeScreen.kt` (aggiungere click su avatar → bottom sheet)

**Criteri di Done:**
- [ ] Click su foto profilo → bottom sheet appare
- [ ] L'utente può cambiare foto profilo (scelta da galleria → upload → aggiornamento)
- [ ] L'utente può modificare il nome visualizzato
- [ ] Il piano corrente è mostrato (Free/Premium)
- [ ] Logout funziona e torna alla LoginScreen

---

### FASE 13 — Monetizzazione (Free + Premium)

**Precondizioni:** Fase 12 completata. Account AdMob configurato. Prodotto subscription creato su Google Play Console.

**Produce:**
- Integrazione Google Play Billing (subscription Premium €2.99/mese o €19.99/anno)
- PremiumScreen con confronto tier e bottone acquisto
- Banner AdMob su HomeScreen e DashboardScreen (solo utenti Free)
- Rewarded Video per starnazzi extra giornalieri (solo utenti Free)
- Cloud Function: `verifyPurchase`
- Limiti piano Free verificati server-side (10 starnazzi/giorno, 2 gruppi, 1 broadcast/giorno)

**File da creare/modificare:**
- `data/model/UserPlan.kt`
- `data/repository/BillingRepository.kt`
- `domain/usecase/CheckPlanLimitsUseCase.kt`
- `ui/premium/PremiumScreen.kt` + `PremiumViewModel.kt`
- `ui/home/HomeScreen.kt` (aggiungere banner AdMob per utenti Free)
- `ui/dashboard/DashboardScreen.kt` (aggiungere banner AdMob per utenti Free)
- `ui/components/RewardedVideoButton.kt`
- **Cloud Functions:** `src/billing/verifyPurchase.ts`
- **Cloud Functions:** aggiornare `sendStarnazzo.ts` e `sendBroadcastStarnazzo.ts` con check limiti piano

**Criteri di Done:**
- [ ] Utente Free vede banner AdMob su Home e Dashboard
- [ ] Utente Free può guardare video reward per ottenere starnazzi extra
- [ ] Utente Free riceve errore dopo 10 starnazzi/giorno
- [ ] Utente Free non può creare più di 2 gruppi
- [ ] PremiumScreen mostra confronto tier con prezzi
- [ ] Flusso acquisto subscription funziona end-to-end
- [ ] Cloud Function verifica il receipt e aggiorna `users/{userId}.plan`
- [ ] Utente Premium non vede ads e non ha limiti
- [ ] Il piano scaduto torna a Free automaticamente

---

### FASE 14 — Eliminazione Account e Privacy

**Precondizioni:** Fase 12 completata (menu profilo funzionante).

**Produce:**
- Funzionalità eliminazione account completa (obbligatoria per Google Play Store)
- Cloud Function `deleteUserAccount` che cancella tutti i dati utente
- Pagine Privacy Policy e Terms of Service (Firebase Hosting o sito esistente)

**File da creare/modificare:**
- `ui/profile/ProfileMenuSheet.kt` (aggiungere pulsante "Elimina account")
- **Cloud Functions:** `src/account/deleteUserAccount.ts`
- Privacy Policy e Terms of Service (pagine web statiche)
- `ui/settings/SettingsScreen.kt` (link a Privacy Policy e ToS)

**Criteri di Done:**
- [ ] Pulsante "Elimina account" nel menu profilo
- [ ] Dialog di conferma doppio prima della cancellazione
- [ ] Cloud Function cancella: documento utente, foto, membership, mute rules, account Auth
- [ ] Dopo eliminazione → redirect a LoginScreen
- [ ] Privacy Policy e ToS accessibili via URL
- [ ] Link a Privacy Policy e ToS nella SettingsScreen

# Spot

**Spot** è una piattaforma web basata sulla geolocalizzazione che permette di:
- creare e condividere **spot** (luoghi di interesse);
- organizzare e seguire **eventi** legati agli spot;
- interagire con altri utenti tramite **commenti, follow e inviti**.

## Architettura
- **Backend:** Spring Boot (API REST, logica business, accesso ai dati)  
- **Database:** PostgreSQL + PostGIS (query spaziali e gestione geolocalizzazione)  

## Funzionalità principali
- Registrazione e autenticazione con token di sessione  
- Gestione profili, follow/unfollow e amicizie  
- Creazione, ricerca e gestione di spot ed eventi con livelli di privacy  
- Commenti, feed personalizzato e notifiche in tempo reale  

## Collaudo
È disponibile una **raccolta Postman** per testare l’intero ciclo di vita dell’applicazione (registrazione, login, creazione spot, eventi, commenti, inviti).  
L’API è raggiungibile di default su `http://localhost:8080`.  

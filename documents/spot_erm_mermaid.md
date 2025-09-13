erDiagram

    %% ========== UTENTI ==========
    user {
        int id "PK"
        string username "UNIQUE"
        string email "UNIQUE"
        string password
    }

    user_follow {
        int user_id "FK → user.id"
        int follower_id "FK → user.id"
    }

    notification {
        int id "PK"
        int user_id "FK → user.id"
        enum type
        string message
        datetime createdAt
        boolean read
    }

    %% ========== SPOT ==========
    spot {
        int id "PK"
        int owner_id "FK → user.id"
        string name
        string description
        geography position
        enum privacy
    }

    spot_followers {
        int spot_id "FK → spot.id"
        int user_id "FK → user.id"
    }

    spot_invitations {
        int spot_id "FK → spot.id"
        int user_id "FK → user.id"
    }

    %% ========== EVENTI ==========
    event {
        int id "PK"
        int owner_id "FK → user.id"
        int spot_id "FK → spot.id"
        string title
        string description
        enum privacy
        datetime date
    }

    event_followers {
        int event_id "FK → event.id"
        int user_id "FK → user.id"
    }

    event_invitations {
        int event_id "FK → event.id"
        int user_id "FK → user.id"
    }

    %% ========== COMMENTI ==========
    comment {
        int id "PK"
        int owner_id "FK → user.id"
        int event_id "FK → event.id"
        boolean edited
        string text
        datetime date
    }

    %% === RELAZIONI SOCIALI ===
    user         ||--o{ user_follow       : "segue"
    user_follow  }o--|| user              : "è seguito da"

    notification }o--|| user              : "spedita a"

    %% === SPOT: proprietà, follower, inviti ===
    user         ||--o{ spot              : "possiede"
    user         ||--o{ spot_followers    : "segue spot"
    spot_followers }o--|| spot            : "ha follower"
    user         ||--o{ spot_invitations  : "riceve invito spot"
    spot_invitations }o--|| spot          : "manda invito"

    %% === EVENTI: proprietà, follower, inviti ===
    spot         ||--o{ event             : "ospita"
    user         ||--o{ event             : "crea"
    user         ||--o{ event_followers   : "segue evento"
    event_followers }o--|| event          : "ha follower"
    user         ||--o{ event_invitations : "riceve invito evento"
    event_invitations }o--|| event        : "invita"

    %% === COMMENTI ===
    event        ||--o{ comment           : "ha commenti"
    comment      ||--|| user              : "scritto da"

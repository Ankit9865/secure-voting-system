CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    has_voted     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE candidates (
    id     BIGINT       NOT NULL AUTO_INCREMENT,
    name   VARCHAR(100) NOT NULL,
    party  VARCHAR(100) NOT NULL,
    active BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_candidates_name UNIQUE (name)
) ENGINE=InnoDB;

-- Ballots are anonymous on purpose: no user_id here.
-- "One person, one vote" is enforced by users.has_voted (see VoteService).
CREATE TABLE votes (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    candidate_id BIGINT      NOT NULL,
    cast_at      DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_votes_candidate FOREIGN KEY (candidate_id) REFERENCES candidates (id),
    INDEX idx_votes_candidate (candidate_id)
) ENGINE=InnoDB;

CREATE TABLE user
(
    user_id           BIGINT    AUTO_INCREMENT       NOT NULL,
    user_nick         VARCHAR(100) NULL,
    user_id_login     VARCHAR(100) NULL,
    user_name         VARCHAR(100) NULL,
    pwd               VARCHAR(100) NULL,
    user_email        VARCHAR(100) NULL,
    user_phone        VARCHAR(100) NULL,
    sex               VARCHAR(100) NULL,
    main_language     VARCHAR(100) NULL,
    birthday          VARCHAR(100) NULL,
    profile_image_url VARCHAR(255) NULL,
    CONSTRAINT pk_user PRIMARY KEY (user_id)
);

ALTER TABLE user
    ADD CONSTRAINT uc_user_useridlogin UNIQUE (user_id_login);

ALTER TABLE user
    ADD CONSTRAINT uc_user_usernick UNIQUE (user_nick);

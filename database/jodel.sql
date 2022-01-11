-- -----------------------------------------------------
-- Schema jodel
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `jodel`;
CREATE SCHEMA IF NOT EXISTS `jodel` DEFAULT CHARACTER SET utf8mb4;
USE `jodel`;

-- -----------------------------------------------------
-- Table `jodel`.`users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `jodel`.`users`;

CREATE TABLE IF NOT EXISTS `jodel`.`users`
(
    `id`       INT UNSIGNED NOT NULL,
    `email`    VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `auto_distance` BOOLEAN,
    `karma` INT  NULL,
    `role` ENUM('admin', 'user') NOT NULL DEFAULT 'user',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE
);

-- -----------------------------------------------------
-- Table `jodel`.`channels`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `jodel`.`channels`;

CREATE TABLE IF NOT EXISTS `jodel`.`channels`
(
    `id`           INT UNSIGNED AUTO_INCREMENT,
    `name`         VARCHAR(45)    NOT NULL,
    `info`         VARCHAR(100)    NOT NULL,
    `symbol`       VARCHAR(20)    NULL,
    PRIMARY KEY (`id`)
);

-- -----------------------------------------------------
-- Table `jodel`.`posts`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `jodel`.`posts`;

CREATE TABLE IF NOT EXISTS `jodel`.`posts`
(
    `id`            INT UNSIGNED   NOT NULL,
    `type`          ENUM ('IMAGE', 'LINK', 'SURVEY') NULL,
    `text`          VARCHAR(255)    NULL,
    `hashtag`       VARCHAR(30)    NULL,
    `longitude`     DOUBLE NOT NULL,
    `latitude`      DOUBLE NOT NULL,
    `city`          VARCHAR(255) NOT NULL,
    `posted_at`     DATETIME       NULL DEFAULT NOW(),
    `color`         VARCHAR(7),
    `image`         MEDIUMBLOB NULL,
    `link`         VARCHAR(255) NULL,
    `author_id`     INT UNSIGNED   NOT NULL,
    `channel_id`     INT UNSIGNED   NOT NULL,
    `comment_number` INT          NULL,
    `voting_value`  INT            NULL,
    `survey_votes`  INT            NULL,
    PRIMARY KEY (`id`),
    INDEX `fk_POSTS_USERS_idx` (`author_id` ASC) VISIBLE,
    INDEX `fk_CHANNELS_idx` (`channel_id` ASC) VISIBLE,
    CONSTRAINT `fk_POSTS_USERS`
        FOREIGN KEY (`author_id`)
            REFERENCES `jodel`.`users` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_CHANNELS`
        FOREIGN KEY (`channel_id`)
            REFERENCES `jodel`.`channels` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION
);

-- -----------------------------------------------------
-- Table `jodel`.`comments`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `jodel`.`comments`;

CREATE TABLE IF NOT EXISTS `jodel`.`comments`
(
    `id`           INT UNSIGNED   NOT NULL,
    `type`          ENUM ('IMAGE') NULL,
    `text`         VARCHAR(45)     NULL,
    `longitude`    DOUBLE NOT NULL,
    `latitude`     DOUBLE NOT NULL,
    `city`          VARCHAR(45) NOT NULL,
    `posted_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `image`         MEDIUMBLOB NULL,
    `author_id`    INT UNSIGNED   NOT NULL,
    `post_id`      INT UNSIGNED   NOT NULL,
    `jodel_number`  INT UNSIGNED   NULL,
    `jodel_number_color` VARCHAR(7),
    `voting_value` INT            NULL,
    PRIMARY KEY (`id`),
    INDEX `fk_COMMENTS_USERS_idx` (`author_id` ASC) VISIBLE,
    INDEX `fk_COMMENTS_POSTS_idx` (`post_id` ASC) VISIBLE,
    CONSTRAINT `fk_COMMENTS_POSTS`
        FOREIGN KEY (`post_id`)
            REFERENCES `jodel`.`posts` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_COMMENTS_USERS`
        FOREIGN KEY (`author_id`)
            REFERENCES `jodel`.`users` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION
);


-- -----------------------------------------------------
-- Table `jodel`.`surveys`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `jodel`.`surveys`;
/*
CREATE TABLE IF NOT EXISTS `jodel`.`surveys`
(
    `id`         INT UNSIGNED        NOT NULL,
    `post_id`    INT UNSIGNED        NOT NULL,
    `number_votes` INT         NULL,
        PRIMARY KEY (`id`),
    INDEX `FK_SURVEYS_post_id` (`post_id` ASC) VISIBLE,
    CONSTRAINT `FK_SURVEYS_post_id`
        FOREIGN KEY (`post_id`)
            REFERENCES `jodel`.`posts` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION
);
*/

-- -----------------------------------------------------
-- Table `jodel`.`surveys_options`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `jodel`.`surveys_options`;

CREATE TABLE IF NOT EXISTS `jodel`.`surveys_options`
(
    `id`         INT UNSIGNED        NOT NULL,
    /*`survey_id`  INT UNSIGNED        NOT NULL,*/
    `post_id`    INT UNSIGNED        NOT NULL,
    `option`     VARCHAR(30)         NOT NULL,
    `number_votes` INT          NULL,
    PRIMARY KEY (`id`),
    INDEX `FK_SURVEYS_OPTIONS_post_id` (`post_id` ASC) VISIBLE,
    /*INDEX `FK_SURVEYS_OPTIONS_survey_id` (`survey_id` ASC) VISIBLE,*/
    CONSTRAINT `FK_SURVEYS_OPTIONS_post_id`
        FOREIGN KEY (`post_id`)
            REFERENCES `jodel`.`posts` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION
    /*CONSTRAINT `FK_SURVEYS_OPTIONS_survey_id`
        FOREIGN KEY (`survey_id`)
            REFERENCES `jodel`.`surveys` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION*/
);


-- -----------------------------------------------------
-- Table `jodel`.`surveys_votes`
-- -----------------------------------------------------

DROP TABLE IF EXISTS `jodel`.`surveys_votes`;


CREATE TABLE IF NOT EXISTS `jodel`.`surveys_votes`
(
    `id`         INT UNSIGNED        NOT NULL,
    `post_id`    INT UNSIGNED        NOT NULL,
    /*`survey_id`  INT UNSIGNED        NOT NULL,*/
    `survey_options_id`    INT UNSIGNED        NOT NULL,
    `user_id`    INT UNSIGNED        NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `FK_SURVEYS_VOTES_user_id` (`user_id` ASC) VISIBLE,
    INDEX `FK_SURVEYS_VOTES_post_id` (`post_id` ASC) VISIBLE,
    /*INDEX `FK_SURVEYS_VOTES_survey_id` (`survey_id` ASC) VISIBLE,*/
    CONSTRAINT `FK_SURVEYS_VOTES_post_id`
        FOREIGN KEY (`post_id`)
            REFERENCES `jodel`.`posts` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION,
    /*CONSTRAINT `FK_SURVEYS_VOTES_survey_id`
        FOREIGN KEY (`survey_id`)
            REFERENCES `jodel`.`surveys` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION,*/
    CONSTRAINT `FK_SURVEYS_VOTES_survey_options_id`
        FOREIGN KEY (`survey_options_id`)
            REFERENCES `jodel`.`surveys_options` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION,
    CONSTRAINT `FK_SURVEYS_VOTES_user_id`
        FOREIGN KEY (`user_id`)
            REFERENCES `jodel`.`users` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION
);


-- -----------------------------------------------------
-- Table `jodel`.`sequence`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `jodel`.`sequence`;

CREATE TABLE IF NOT EXISTS `jodel`.`sequence`
(
    `seq_name`  VARCHAR(255) NOT NULL,
    `seq_count` INT          NOT NULL,
    PRIMARY KEY (`seq_name`)
);


-- -----------------------------------------------------
-- Table `jodel`.`votings`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `jodel`.`votings`;

CREATE TABLE IF NOT EXISTS `jodel`.`votings`
(
    `id`         INT UNSIGNED        NOT NULL,
    `user_id`    INT UNSIGNED        NOT NULL,
    `comment_id` INT UNSIGNED        NULL DEFAULT NULL,
    `post_id`    INT UNSIGNED        NULL DEFAULT NULL,
    `type`       ENUM ('UP', 'DOWN') NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `FK_VOTING_user_id` (`user_id` ASC) VISIBLE,
    INDEX `FK_VOTING_comment_id` (`comment_id` ASC) VISIBLE,
    INDEX `fk_votings_posts1_idx` (`post_id` ASC) VISIBLE,
    CONSTRAINT `FK_VOTING_comment_id`
        FOREIGN KEY (`comment_id`)
            REFERENCES `jodel`.`comments` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION,
    CONSTRAINT `FK_VOTING_user_id`
        FOREIGN KEY (`user_id`)
            REFERENCES `jodel`.`users` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_votings_posts1`
        FOREIGN KEY (`post_id`)
            REFERENCES `jodel`.`posts` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION
);

-- -----------------------------------------------------
-- Table `jodel`.`sessions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `jodel`.`sessions`;

CREATE TABLE IF NOT EXISTS `jodel`.`sessions`
(
    `token`      VARCHAR(255) NOT NULL,
    `user_id`    INT UNSIGNED NOT NULL,
    `last_used`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`token`),
    INDEX `fk_sessions_users1_idx` (`user_id` ASC) VISIBLE,
    UNIQUE INDEX `token_UNIQUE` (`token` ASC) VISIBLE,
    CONSTRAINT `fk_sessions_users1`
        FOREIGN KEY (`user_id`)
            REFERENCES `jodel`.`users` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION
);


INSERT INTO `jodel`.`SEQUENCE` (`seq_name`, `seq_count`)
VALUES ('users', 1);
INSERT INTO `jodel`.`SEQUENCE` (`seq_name`, `seq_count`)
VALUES ('posts', 1);
INSERT INTO `jodel`.`SEQUENCE` (`seq_name`, `seq_count`)
VALUES ('comments', 1);
INSERT INTO `jodel`.`SEQUENCE` (`seq_name`, `seq_count`)
VALUES ('votings', 1);


INSERT INTO `jodel`.`channels` (`name`, `info`,`symbol`)
VALUES ('Main', 'Der Standardchannel', '📢');

INSERT INTO `jodel`.`channels` (`name`, `info`,`symbol`)
VALUES ('Fragen', 'Jodler helfen Jodlern', '❓');

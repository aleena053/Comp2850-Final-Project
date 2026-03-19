CREATE DATABASE IF NOT EXISTS fitness_app;
USE fitness_app;
CREATE TABLE Users (
    user_id     INT PRIMARY KEY AUTO_INCREMENT,
    Name        VARCHAR(100)    NOT NULL,
    email       VARCHAR(255)    NOT NULL UNIQUE,
    password    VARCHAR(255)    NOT NULL,
    date_of_birth DATE,
    fitness_level VARCHAR(20),
    role        VARCHAR(20)
);
CREATE TABLE Sports (
    sport_id    INT PRIMARY KEY AUTO_INCREMENT,
    sport_name  VARCHAR(100)    NOT NULL
);
CREATE TABLE TrainingPlan (
    plan_id     INT PRIMARY KEY AUTO_INCREMENT,
    user_id     INT             NOT NULL,
    title       VARCHAR(100),
    start_date  DATE,
    end_date    DATE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
);
CREATE TABLE Workout (
    workout_id      INT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT             NOT NULL,
    sport_id        INT,
    workout_date    DATE,
    duration        INT,
    distance_km     DECIMAL(10, 2),
    avg_pace        DECIMAL(10, 2),
    avg_heart_rate  INT,
    notes           TEXT,
    FOREIGN KEY (user_id)   REFERENCES Users(user_id)
        ON DELETE CASCADE,
    FOREIGN KEY (sport_id)  REFERENCES Sports(sport_id)
        ON DELETE SET NULL
);
CREATE TABLE PlannedWorkout (
    planned_workout_id  INT PRIMARY KEY AUTO_INCREMENT,
    plan_id             INT             NOT NULL,
    sport_id            INT,
    scheduled_date      DATE,
    target_distance     DECIMAL(10, 2),
    target_duration     INT,
    status              VARCHAR(20),
    FOREIGN KEY (plan_id)   REFERENCES TrainingPlan(plan_id)
        ON DELETE CASCADE,
    FOREIGN KEY (sport_id)  REFERENCES Sports(sport_id)
        ON DELETE SET NULL
);
CREATE TABLE Competition (
    competition_id      INT PRIMARY KEY AUTO_INCREMENT,
    name                VARCHAR(150),
    location            VARCHAR(150),
    competition_date    DATE,
    sport_id            INT,
    description         TEXT,
    FOREIGN KEY (sport_id) REFERENCES Sports(sport_id)
        ON DELETE SET NULL
);
CREATE TABLE CompetitionResult (
    result_id       INT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT             NOT NULL,
    competition_id  INT             NOT NULL,
    finish_time     DECIMAL(10, 2),
    position        INT,
    notes           TEXT,
    FOREIGN KEY (user_id)           REFERENCES Users(user_id)
        ON DELETE CASCADE,
    FOREIGN KEY (competition_id)    REFERENCES Competition(competition_id)
        ON DELETE CASCADE
);
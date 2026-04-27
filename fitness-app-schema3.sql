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


INSERT INTO Users (Name, email, password, date_of_birth, fitness_level, role) VALUES 
('Alice Johnson', 'alice_j@hotmail.com', '0', '1999-03-17', 'Advanced', 'Customer'),
('Zahim Muhammad', 'zahmuh12@jmail.com', '0', '2000-01-20', 'Intermediate', 'Customer'),
('Kimmy Holtson', 'holtkim33@jmail.com', '0', '1998-05-03', 'Intermediate', 'Customer'),
('Bobby Smith', 'smithbob6@hotmail.com', '0', '2001-09-16', 'Beginner', 'Customer'),
('Caroline White', 'white4carol@gmail.com', '0', '1995-04-13', 'Intermediate', 'Customer'),
('David Brown', 'browndave7@gmail.com', '0', '1997-09-03', 'Intermediate', 'Customer'),
('Eve Martinez', 'martin88@gmail.com', '0', '1998-05-02', 'Advanced', 'Customer'),
('Amelia Hart', 'amyyywa9@hotmail.com', '0', '1999-10-06', 'Beginner', 'Customer'),
('Lucas Benette', 'lucaaa8b@jmail.com', '0', '2002-10-09', 'Intermediate', 'Customer'),
('Maya Petal', 'flower7maya@gmail.com', '0', '2001-11-07', 'Advanced', 'Customer'),
('John Markson', 'marky9john@hotmail.com', '0', '1999-12-21', 'Beginner', 'Customer'),
('Elly Park', 'porkie5belly@gmail.com', '0', '2003-03-07', 'Advanced', 'Customer'),
('Rehan Sallah', 'sallah093ree@jmail.com', '0', '1994-08-09', 'Advanced', 'Customer'),
('Dylan Brookes', 'dylan0909brooke@hotmail.com', '2004-04-02', 'Intermediate', 'Customer'),
('Kimberly Lee', 'leekimsss799@gmail.com', '2002-11-11', 'Beginner', 'Customer'),
('Scott Wang', 'scottytyywang@gmail.com', '1998-10-14', 'Beginner', 'Customer'),
('Tilly Scottswood', 'tilll4dawoods@gmail.com', '2000-12-27', 'Beginner', 'Customer');


INSERT INTO Sports (sports_name) VALUES
('Running'),
('Cycling'),
('Triathlon'),
('Hyrox'),
('Weightlifting');

INSERT INTO TrainingPlan (user_id, title, start_date, end_date) VALUES
(1, 'Alice Running Prep', '2025-05-10', '2025-07-01'),
(2, 'Zahim Cycling Base Build', '2025-01-10', '2025-03-11'),
(4, 'Bobby Beginner Running', '2025-11-04', '2026-02-17'),
(6, 'David Triathlon Prep', '2025-04-13', '2025-08-23'),
(7, 'Amelia Beginner Run', '2025-02-22', '2025-04-19');

INSERT INTO Workout (user_id, sport_id, workout_date, duration, distance_km, avg_pace, avg_heart_rate, notes) VALUES
(1,1, '2025-05-10', 60, 10.00, 6.00, 155, 'Long Run'),
(1,1, '2025-05-11', 58, 10.50, 6.02, 157, 'Long Run');

INSERT INTO PlannedWorkout (plan_id, sport_id, scheduled_date, target_distance, target_duration, status) VALUES
(1,1, '2025-05-10', 10, 60, 'Completed'),
(1,1, '2025-05-11', 10.50, 60, 'Completed');

INSERT INTO Competition (name, location, competition_date, sport_id, description) VALUES
('Leeds Marathon 2025', 'Leeds, UK', '2025-08-20', 1, 'Marathon run through Leeds');

INSERT INTO CompetitionResult (user_id, competition_id, finish_time, position, notes) VALUES
(1, 4, 28.50, 7, 'Solid 5k run to test fitness');










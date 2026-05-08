CREATE DATABASE IF NOT EXISTS fitness_app;
USE fitness_app;

CREATE TABLE Users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    date_of_birth DATE,
    fitness_level VARCHAR(20),
    role VARCHAR(20),
    username VARCHAR(50) UNIQUE
);

CREATE TABLE Sports (
    sport_id INT PRIMARY KEY AUTO_INCREMENT,
    sport_name VARCHAR(100) NOT NULL
);

CREATE TABLE TrainingPlan (
    plan_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    plan_name VARCHAR(100),
    description TEXT,
    start_date DATE,
    end_date DATE,
    created_by_user_id INT,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE Workout (
    workout_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    sport_id INT,
    workout_date DATE,
    duration INT,
    distance_km DECIMAL(10,2),
    avg_pace DECIMAL(10,2),
    avg_heart_rate INT,
    notes TEXT,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE,
    FOREIGN KEY (sport_id) REFERENCES Sports(sport_id)
        ON DELETE SET NULL
);

CREATE TABLE WorkoutExercise (
    exercise_id INT PRIMARY KEY AUTO_INCREMENT,
    workout_id INT NOT NULL,
    exercise_name VARCHAR(100) NOT NULL,
    sets_count INT NOT NULL,
    reps_count INT NOT NULL,
    weight_kg DECIMAL(6,2) NOT NULL,
    FOREIGN KEY (workout_id) REFERENCES Workout(workout_id)
        ON DELETE CASCADE
);

CREATE TABLE PlannedWorkout (
    planned_workout_id INT PRIMARY KEY AUTO_INCREMENT,
    plan_id INT NOT NULL,
    sport_id INT,
    scheduled_date DATE,
    target_distance DECIMAL(10,2),
    target_duration INT,
    status VARCHAR(20),
    FOREIGN KEY (plan_id) REFERENCES TrainingPlan(plan_id)
        ON DELETE CASCADE,
    FOREIGN KEY (sport_id) REFERENCES Sports(sport_id)
        ON DELETE SET NULL
);

CREATE TABLE Competition (
    competition_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150),
    location VARCHAR(150),
    competition_date DATE,
    sport_id INT,
    description TEXT,
    user_id INT NOT NULL,
    event_type VARCHAR(100) NOT NULL DEFAULT 'General',
    FOREIGN KEY (sport_id) REFERENCES Sports(sport_id)
        ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE CompetitionResult (
    result_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    competition_id INT NOT NULL,
    finish_time DECIMAL(10,2),
    position INT,
    notes TEXT,
    is_personal_best TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE,
    FOREIGN KEY (competition_id) REFERENCES Competition(competition_id)
        ON DELETE CASCADE
);

CREATE TABLE TrainerClients (
    trainer_id INT NOT NULL,
    client_id INT NOT NULL,
    PRIMARY KEY (trainer_id, client_id),
    FOREIGN KEY (trainer_id) REFERENCES Users(user_id)
        ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE Conversation (
    conversation_id INT NOT NULL AUTO_INCREMENT,
    conversation_type ENUM('direct','group') NOT NULL,
    title VARCHAR(255) DEFAULT NULL,
    created_by_user_id INT NOT NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (conversation_id),
    FOREIGN KEY (created_by_user_id) REFERENCES Users(user_id)
);

CREATE TABLE ConversationParticipant (
    conversation_id INT NOT NULL,
    user_id INT NOT NULL,
    participant_role ENUM('admin','creator','member') NOT NULL,
    joined_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (conversation_id, user_id),
    FOREIGN KEY (conversation_id) REFERENCES Conversation(conversation_id)
        ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE Message (
    message_id INT NOT NULL AUTO_INCREMENT,
    conversation_id INT NOT NULL,
    sender_user_id INT NOT NULL,
    message_text TEXT NOT NULL,
    sent_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (message_id),
    FOREIGN KEY (conversation_id) REFERENCES Conversation(conversation_id)
        ON DELETE CASCADE,
    FOREIGN KEY (sender_user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
);
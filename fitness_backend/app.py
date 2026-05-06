from flask import Flask, request, jsonify
from flask_cors import CORS
from werkzeug.security import generate_password_hash, check_password_hash
from db import get_connection
from datetime import date, timedelta

app = Flask(__name__)
CORS(app)


@app.route("/")
def home():
    return jsonify({"message": "Fitness app backend is running"}), 200


@app.route("/signup", methods=["POST"])
def signup():
    data = request.get_json()

    name = (data.get("name") or "").strip()
    username = (data.get("username") or "").strip().lower()
    email = (data.get("email") or "").strip().lower()
    password = (data.get("password") or "").strip()
    role = (data.get("role") or "").strip().lower()
    date_of_birth = data.get("dateOfBirth")
    fitness_level = data.get("fitnessLevel")

    if not name or not username or not email or not password or not role:
        return jsonify({"success": False, "message": "Missing required fields"}), 400

    if role not in ["casual runner", "athlete", "trainer"]:
        return jsonify({"success": False, "message": "Invalid role"}), 400

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("SELECT user_id FROM Users WHERE email = %s", (email,))
            existing_user = cursor.fetchone()

            if existing_user:
                return jsonify({"success": False, "message": "Email already exists"}), 409

            cursor.execute("SELECT user_id FROM Users WHERE username = %s", (username,))
            existing_username = cursor.fetchone()

            if existing_username:
                return jsonify({"success": False, "message": "Username already exists"}), 409

            hashed_password = generate_password_hash(password)

            sql = """
                INSERT INTO Users (name, username, email, password, date_of_birth, fitness_level, role)
                VALUES (%s, %s, %s, %s, %s, %s, %s)
            """
            values = (name, username, email, hashed_password, date_of_birth, fitness_level, role)

            cursor.execute(sql, values)
            connection.commit()

            user_id = cursor.lastrowid

            return jsonify({
                "success": True,
                "message": "Account created successfully",
                "user": {
                    "userId": user_id,
                    "name": name,
                    "username": username,
                    "email": email,
                    "role": role
                }
            }), 201

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/login", methods=["POST"])
def login():
    data = request.get_json()

    email = (data.get("email") or "").strip().lower()
    password = (data.get("password") or "").strip()

    if not email or not password:
        return jsonify({"success": False, "message": "Email and password are required"}), 400

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT user_id, name, username, email, password, role, date_of_birth, fitness_level
                FROM Users
                WHERE email = %s
            """, (email,))
            user = cursor.fetchone()

            if not user:
                return jsonify({"success": False, "message": "Invalid email or password"}), 401

            if not check_password_hash(user["password"], password):
                return jsonify({"success": False, "message": "Invalid email or password"}), 401

            return jsonify({
                "success": True,
                "message": "Login successful",
                "user": {
                    "userId": user["user_id"],
                    "name": user["name"],
                    "username": user["username"],
                    "email": user["email"],
                    "role": user["role"],
                    "dateOfBirth": user["date_of_birth"],
                    "fitnessLevel": user["fitness_level"]
                }
            }), 200

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/workouts", methods=["POST"])
def create_workout():
    data = request.get_json()

    user_id = data.get("userId")
    sport_name = (data.get("sport") or "").strip()
    workout_date = (data.get("workoutDate") or "").strip()
    duration = data.get("duration")
    distance_km = data.get("distanceKm")
    avg_pace = data.get("avgPace")
    avg_heart_rate = data.get("avgHeartRate")
    notes = data.get("notes")
    exercises = data.get("exercises") or []

    if not user_id or not sport_name or not workout_date or duration is None:
        return jsonify({"success": False, "message": "Missing required workout fields"}), 400

    try:
        duration = int(duration)
    except (TypeError, ValueError):
        return jsonify({"success": False, "message": "Duration must be a valid number"}), 400

    if duration <= 0:
        return jsonify({"success": False, "message": "Duration must be greater than 0"}), 400

    is_gym = sport_name.lower() == "gym"

    if not is_gym:
        if distance_km is not None and distance_km != "":
            try:
                distance_km = float(distance_km)
            except (TypeError, ValueError):
                return jsonify({"success": False, "message": "Distance must be a valid number"}), 400

            if distance_km <= 0:
                return jsonify({"success": False, "message": "Distance must be greater than 0 if entered"}), 400
        else:
            distance_km = None

        if avg_pace is not None and avg_pace != "":
            try:
                avg_pace = float(avg_pace)
            except (TypeError, ValueError):
                return jsonify({"success": False, "message": "Average pace must be a valid number"}), 400

            if avg_pace <= 0:
                return jsonify({"success": False, "message": "Average pace must be greater than 0 if entered"}), 400
        else:
            avg_pace = None
    else:
        distance_km = None
        avg_pace = None

        if not exercises:
            return jsonify({"success": False, "message": "At least one exercise is required for gym workouts"}), 400

        for exercise in exercises:
            exercise_name = (exercise.get("exerciseName") or "").strip()
            sets_count = exercise.get("setsCount")
            reps_count = exercise.get("repsCount")
            weight_kg = exercise.get("weightKg")

            if not exercise_name:
                return jsonify({"success": False, "message": "Exercise name is required"}), 400

            try:
                sets_count = int(sets_count)
                reps_count = int(reps_count)
                weight_kg = float(weight_kg)
            except (TypeError, ValueError):
                return jsonify({"success": False, "message": "Sets, reps and weight must be valid numbers"}), 400

            if sets_count <= 0 or reps_count <= 0 or weight_kg <= 0:
                return jsonify({"success": False, "message": "Sets, reps and weight must be greater than 0"}), 400

    if avg_heart_rate is not None and avg_heart_rate != "":
        try:
            avg_heart_rate = int(avg_heart_rate)
        except (TypeError, ValueError):
            return jsonify({"success": False, "message": "Heart rate must be a valid number"}), 400

        if avg_heart_rate <= 0:
            return jsonify({"success": False, "message": "Heart rate must be greater than 0 if entered"}), 400
    else:
        avg_heart_rate = None

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute(
                "SELECT sport_id FROM Sports WHERE LOWER(sport_name) = LOWER(%s)",
                (sport_name,)
            )
            sport_row = cursor.fetchone()

            if not sport_row:
                return jsonify({"success": False, "message": "Selected sport does not exist"}), 400

            sport_id = sport_row["sport_id"]

            cursor.execute("""
                INSERT INTO Workout (
                    user_id, sport_id, workout_date, duration,
                    distance_km, avg_pace, avg_heart_rate, notes
                )
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
            """, (
                user_id,
                sport_id,
                workout_date,
                duration,
                distance_km,
                avg_pace,
                avg_heart_rate,
                notes
            ))

            workout_id = cursor.lastrowid

            if is_gym:
                for exercise in exercises:
                    cursor.execute("""
                        INSERT INTO WorkoutExercise (workout_id, exercise_name, sets_count, reps_count, weight_kg)
                        VALUES (%s, %s, %s, %s, %s)
                    """, (
                        workout_id,
                        exercise["exerciseName"],
                        exercise["setsCount"],
                        exercise["repsCount"],
                        exercise["weightKg"]
                    ))

            connection.commit()

            return jsonify({
                "success": True,
                "message": "Workout saved successfully"
            }), 201

    except Exception as e:
        connection.rollback()
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()

@app.route("/workouts/<int:user_id>", methods=["GET"])
def get_workouts(user_id):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            sql = """
                SELECT
                    w.workout_id,
                    s.sport_name,
                    w.workout_date,
                    w.duration,
                    w.distance_km,
                    w.avg_pace,
                    w.avg_heart_rate,
                    w.notes
                FROM Workout w
                JOIN Sports s ON w.sport_id = s.sport_id
                WHERE w.user_id = %s
                ORDER BY w.workout_date DESC, w.workout_id DESC
            """
            cursor.execute(sql, (user_id,))
            workouts = cursor.fetchall()

            camel_case_workouts = []

            for workout in workouts:
                workout_item = {
                    "workoutId": workout["workout_id"],
                    "sportName": workout["sport_name"],
                    "workoutDate": workout["workout_date"],
                    "duration": workout["duration"],
                    "distanceKm": workout["distance_km"],
                    "avgPace": workout["avg_pace"],
                    "avgHeartRate": workout["avg_heart_rate"],
                    "notes": workout["notes"]
                }

                if workout["sport_name"].lower() == "gym":
                    cursor.execute("""
                        SELECT exercise_name, sets_count, reps_count, weight_kg
                        FROM WorkoutExercise
                        WHERE workout_id = %s
                        ORDER BY exercise_id ASC
                    """, (workout["workout_id"],))
                    exercises = cursor.fetchall()

                    exercise_summaries = []
                    for ex in exercises:
                        summary = (
                            f'{ex["exercise_name"]}: '
                            f'{ex["sets_count"]}x{ex["reps_count"]} @ {float(ex["weight_kg"]):.1f}kg'
                        )
                        exercise_summaries.append(summary)

                    workout_item["exerciseSummaries"] = exercise_summaries
                else:
                    workout_item["exerciseSummaries"] = []

                camel_case_workouts.append(workout_item)

            return jsonify({
                "success": True,
                "workouts": camel_case_workouts
            }), 200

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/workouts/detail/<int:workout_id>", methods=["GET"])
def get_workout_detail(workout_id):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT
                    w.workout_id,
                    w.user_id,
                    s.sport_name,
                    w.workout_date,
                    w.duration,
                    w.distance_km,
                    w.avg_pace,
                    w.avg_heart_rate,
                    w.notes
                FROM Workout w
                JOIN Sports s ON w.sport_id = s.sport_id
                WHERE w.workout_id = %s
            """, (workout_id,))
            workout = cursor.fetchone()

            if not workout:
                return jsonify({"success": False, "message": "Workout not found"}), 404

            workout_item = {
                "workoutId": workout["workout_id"],
                "userId": workout["user_id"],
                "sportName": workout["sport_name"],
                "workoutDate": workout["workout_date"],
                "duration": workout["duration"],
                "distanceKm": workout["distance_km"],
                "avgPace": workout["avg_pace"],
                "avgHeartRate": workout["avg_heart_rate"],
                "notes": workout["notes"]
            }

            if workout["sport_name"].lower() == "gym":
                cursor.execute("""
                    SELECT exercise_name, sets_count, reps_count, weight_kg
                    FROM WorkoutExercise
                    WHERE workout_id = %s
                    ORDER BY exercise_id ASC
                """, (workout_id,))
                exercises = cursor.fetchall()

                workout_item["exercises"] = [
                    {
                        "exerciseName": row["exercise_name"],
                        "setsCount": row["sets_count"],
                        "repsCount": row["reps_count"],
                        "weightKg": float(row["weight_kg"])
                    }
                    for row in exercises
                ]
            else:
                workout_item["exercises"] = []

            return jsonify({
                "success": True,
                "workout": workout_item
            }), 200

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/workouts/<int:workout_id>", methods=["PUT"])
def update_workout(workout_id):
    data = request.get_json()

    sport_name = (data.get("sport") or "").strip()
    workout_date = (data.get("workoutDate") or "").strip()
    duration = data.get("duration")
    distance_km = data.get("distanceKm")
    avg_pace = data.get("avgPace")
    avg_heart_rate = data.get("avgHeartRate")
    notes = data.get("notes")
    exercises = data.get("exercises") or []

    if not sport_name or not workout_date or duration is None:
        return jsonify({"success": False, "message": "Missing required workout fields"}), 400

    try:
        duration = int(duration)
    except (TypeError, ValueError):
        return jsonify({"success": False, "message": "Duration must be a valid number"}), 400

    if duration <= 0:
        return jsonify({"success": False, "message": "Duration must be greater than 0"}), 400

    is_gym = sport_name.lower() == "gym"

    if not is_gym:
        if distance_km is not None and distance_km != "":
            try:
                distance_km = float(distance_km)
            except (TypeError, ValueError):
                return jsonify({"success": False, "message": "Distance must be a valid number"}), 400

            if distance_km <= 0:
                return jsonify({"success": False, "message": "Distance must be greater than 0 if entered"}), 400
        else:
            distance_km = None

        if avg_pace is not None and avg_pace != "":
            try:
                avg_pace = float(avg_pace)
            except (TypeError, ValueError):
                return jsonify({"success": False, "message": "Average pace must be a valid number"}), 400

            if avg_pace <= 0:
                return jsonify({"success": False, "message": "Average pace must be greater than 0 if entered"}), 400
        else:
            avg_pace = None
    else:
        distance_km = None
        avg_pace = None

        if not exercises:
            return jsonify({"success": False, "message": "At least one exercise is required for gym workouts"}), 400

        for exercise in exercises:
            exercise_name = (exercise.get("exerciseName") or "").strip()
            sets_count = exercise.get("setsCount")
            reps_count = exercise.get("repsCount")
            weight_kg = exercise.get("weightKg")

            if not exercise_name:
                return jsonify({"success": False, "message": "Exercise name is required"}), 400

            try:
                sets_count = int(sets_count)
                reps_count = int(reps_count)
                weight_kg = float(weight_kg)
            except (TypeError, ValueError):
                return jsonify({"success": False, "message": "Sets, reps and weight must be valid numbers"}), 400

            if sets_count <= 0 or reps_count <= 0 or weight_kg <= 0:
                return jsonify({"success": False, "message": "Sets, reps and weight must be greater than 0"}), 400

    if avg_heart_rate is not None and avg_heart_rate != "":
        try:
            avg_heart_rate = int(avg_heart_rate)
        except (TypeError, ValueError):
            return jsonify({"success": False, "message": "Heart rate must be a valid number"}), 400

        if avg_heart_rate <= 0:
            return jsonify({"success": False, "message": "Heart rate must be greater than 0 if entered"}), 400
    else:
        avg_heart_rate = None

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("SELECT workout_id FROM Workout WHERE workout_id = %s", (workout_id,))
            existing_workout = cursor.fetchone()

            if not existing_workout:
                return jsonify({"success": False, "message": "Workout not found"}), 404

            cursor.execute(
                "SELECT sport_id FROM Sports WHERE LOWER(sport_name) = LOWER(%s)",
                (sport_name,)
            )
            sport_row = cursor.fetchone()

            if not sport_row:
                return jsonify({"success": False, "message": "Selected sport does not exist"}), 400

            sport_id = sport_row["sport_id"]

            cursor.execute("""
                UPDATE Workout
                SET sport_id = %s,
                    workout_date = %s,
                    duration = %s,
                    distance_km = %s,
                    avg_pace = %s,
                    avg_heart_rate = %s,
                    notes = %s
                WHERE workout_id = %s
            """, (
                sport_id,
                workout_date,
                duration,
                distance_km,
                avg_pace,
                avg_heart_rate,
                notes,
                workout_id
            ))

            cursor.execute("DELETE FROM WorkoutExercise WHERE workout_id = %s", (workout_id,))

            if is_gym:
                for exercise in exercises:
                    cursor.execute("""
                        INSERT INTO WorkoutExercise (workout_id, exercise_name, sets_count, reps_count, weight_kg)
                        VALUES (%s, %s, %s, %s, %s)
                    """, (
                        workout_id,
                        exercise["exerciseName"],
                        exercise["setsCount"],
                        exercise["repsCount"],
                        exercise["weightKg"]
                    ))

            connection.commit()

            return jsonify({
                "success": True,
                "message": "Workout updated successfully"
            }), 200

    except Exception as e:
        connection.rollback()
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/workouts/<int:workout_id>", methods=["DELETE"])
def delete_workout(workout_id):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("DELETE FROM Workout WHERE workout_id = %s", (workout_id,))
            connection.commit()

            if cursor.rowcount == 0:
                return jsonify({"success": False, "message": "Workout not found"}), 404

            return jsonify({
                "success": True,
                "message": "Workout deleted successfully"
            }), 200

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/trainer/add-client", methods=["POST"])
def add_client_to_trainer():
    data = request.get_json()

    trainer_id = data.get("trainerId")
    client_email = (data.get("clientEmail") or "").strip().lower()

    if not trainer_id or not client_email:
        return jsonify({"success": False, "message": "Trainer ID and client email are required"}), 400

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute(
                "SELECT user_id FROM Users WHERE user_id = %s AND role = 'trainer'",
                (trainer_id,)
            )
            trainer = cursor.fetchone()

            if not trainer:
                return jsonify({"success": False, "message": "Trainer not found"}), 404

            cursor.execute(
                "SELECT user_id, name, email FROM Users WHERE email = %s",
                (client_email,)
            )
            client = cursor.fetchone()

            if not client:
                return jsonify({"success": False, "message": "Client not found"}), 404

            if client["user_id"] == trainer_id:
                return jsonify({"success": False, "message": "Trainer cannot add themselves"}), 400

            cursor.execute(
                "SELECT * FROM TrainerClients WHERE trainer_id = %s AND client_id = %s",
                (trainer_id, client["user_id"])
            )
            existing = cursor.fetchone()

            if existing:
                return jsonify({"success": False, "message": "Client already added"}), 409

            cursor.execute(
                "INSERT INTO TrainerClients (trainer_id, client_id) VALUES (%s, %s)",
                (trainer_id, client["user_id"])
            )
            connection.commit()

            return jsonify({
                "success": True,
                "message": "Client added successfully",
                "client": {
                    "userId": client["user_id"],
                    "name": client["name"],
                    "email": client["email"]
                }
            }), 201

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()

@app.route("/trainer/<int:trainer_id>/clients", methods=["GET"])
def get_trainer_clients(trainer_id):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            sql = """
                SELECT
                    u.user_id,
                    u.name,
                    u.email,
                    u.role,
                    u.fitness_level
                FROM TrainerClients tc
                JOIN Users u ON tc.client_id = u.user_id
                WHERE tc.trainer_id = %s
                ORDER BY u.name ASC
            """
            cursor.execute(sql, (trainer_id,))
            clients = cursor.fetchall()

            camel_case_clients = [
                {
                    "userId": client["user_id"],
                    "name": client["name"],
                    "email": client["email"],
                    "role": client["role"],
                    "fitnessLevel": client["fitness_level"]
                }
                for client in clients
            ]

            return jsonify({
                "success": True,
                "clients": camel_case_clients
            }), 200

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/trainer/<int:trainer_id>/clients/<int:client_id>", methods=["DELETE"])
def remove_trainer_client(trainer_id, client_id):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute(
                "DELETE FROM TrainerClients WHERE trainer_id = %s AND client_id = %s",
                (trainer_id, client_id)
            )
            connection.commit()

            if cursor.rowcount == 0:
                return jsonify({"success": False, "message": "Client link not found"}), 404

            return jsonify({
                "success": True,
                "message": "Client removed successfully"
            }), 200

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/training-plans", methods=["POST"])
def create_training_plan():
    data = request.get_json()

    user_id = data.get("userId")
    created_by_user_id = data.get("createdByUserId")
    plan_name = (data.get("planName") or "").strip()
    description = (data.get("description") or "").strip()
    start_date = data.get("startDate")
    end_date = data.get("endDate")

    if not user_id or not created_by_user_id or not plan_name or not start_date or not end_date:
        return jsonify({
            "success": False,
            "message": "Missing required fields"
        }), 400

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                INSERT INTO TrainingPlan
                (user_id, created_by_user_id, plan_name, description, start_date, end_date)
                VALUES (%s, %s, %s, %s, %s, %s)
            """, (user_id, created_by_user_id, plan_name, description, start_date, end_date))

            connection.commit()
            plan_id = cursor.lastrowid

            return jsonify({
                "success": True,
                "message": "Training plan created successfully",
                "planId": plan_id
            }), 201

    except Exception as e:
        connection.rollback()
        return jsonify({
            "success": False,
            "message": str(e)
        }), 500

    finally:
        connection.close()


@app.route("/training-plans/<int:user_id>", methods=["GET"])
def get_training_plans(user_id):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            sql = """
                SELECT
                    plan_id,
                    user_id,
                    plan_name,
                    description,
                    start_date,
                    end_date
                FROM TrainingPlan
                WHERE user_id = %s
                ORDER BY start_date DESC, plan_id DESC
            """
            cursor.execute(sql, (user_id,))
            plans = cursor.fetchall()

            camel_case_plans = [
                {
                    "planId": plan["plan_id"],
                    "userId": plan["user_id"],
                    "planName": plan["plan_name"],
                    "description": plan["description"],
                    "startDate": plan["start_date"],
                    "endDate": plan["end_date"]
                }
                for plan in plans
            ]

            return jsonify({
                "success": True,
                "plans": camel_case_plans
            }), 200

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/training-plans/detail/<int:plan_id>", methods=["GET"])
def get_training_plan_detail(plan_id):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            sql = """
                SELECT
                    plan_id,
                    user_id,
                    plan_name,
                    description,
                    start_date,
                    end_date
                FROM TrainingPlan
                WHERE plan_id = %s
            """
            cursor.execute(sql, (plan_id,))
            plan = cursor.fetchone()

            if not plan:
                return jsonify({"success": False, "message": "Training plan not found"}), 404

            plan_item = {
                "planId": plan["plan_id"],
                "userId": plan["user_id"],
                "planName": plan["plan_name"],
                "description": plan["description"],
                "startDate": plan["start_date"],
                "endDate": plan["end_date"]
            }

            return jsonify({
                "success": True,
                "plan": plan_item
            }), 200

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/training-plans/<int:plan_id>", methods=["PUT"])
def update_training_plan(plan_id):
    data = request.get_json()

    plan_name = (data.get("planName") or "").strip()
    description = (data.get("description") or "").strip()
    start_date = (data.get("startDate") or "").strip()
    end_date = (data.get("endDate") or "").strip()

    if not plan_name or not start_date or not end_date:
        return jsonify({"success": False, "message": "Missing required training plan fields"}), 400

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            sql = """
                UPDATE TrainingPlan
                SET plan_name = %s,
                    description = %s,
                    start_date = %s,
                    end_date = %s
                WHERE plan_id = %s
            """
            values = (plan_name, description, start_date, end_date, plan_id)

            cursor.execute(sql, values)
            connection.commit()

            if cursor.rowcount == 0:
                return jsonify({"success": False, "message": "Training plan not found"}), 404

            return jsonify({
                "success": True,
                "message": "Training plan updated successfully"
            }), 200

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/training-plans/<int:plan_id>", methods=["DELETE"])
def delete_training_plan(plan_id):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("DELETE FROM TrainingPlan WHERE plan_id = %s", (plan_id,))
            connection.commit()

            if cursor.rowcount == 0:
                return jsonify({"success": False, "message": "Training plan not found"}), 404

            return jsonify({
                "success": True,
                "message": "Training plan deleted successfully"
            }), 200

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


from datetime import date, timedelta

@app.route("/dashboard-stats/<int:user_id>", methods=["GET"])
def get_dashboard_stats(user_id):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT
                    COALESCE(SUM(distance_km), 0) AS total_distance,
                    COALESCE(SUM(duration), 0) AS total_duration,
                    COALESCE(AVG(avg_pace), 0) AS avg_pace,
                    COALESCE(AVG(avg_heart_rate), 0) AS avg_heart_rate
                FROM Workout
                WHERE user_id = %s
                  AND workout_date BETWEEN DATE_SUB(CURDATE(), INTERVAL 6 DAY) AND CURDATE()
            """, (user_id,))
            week_summary = cursor.fetchone()

            cursor.execute("""
                SELECT
                    DATE(workout_date) AS workout_day,
                    COALESCE(SUM(distance_km), 0) AS total_distance
                FROM Workout
                WHERE user_id = %s
                  AND workout_date BETWEEN DATE_SUB(CURDATE(), INTERVAL 6 DAY) AND CURDATE()
                GROUP BY DATE(workout_date)
                ORDER BY DATE(workout_date)
            """, (user_id,))
            rows = cursor.fetchall()

            day_map = {}
            for row in rows:
                day_map[row["workout_day"]] = float(row["total_distance"] or 0)

            today = date.today()
            daily_distance = []

            for offset in range(6, -1, -1):
                current_day = today - timedelta(days=offset)
                daily_distance.append({
                    "label": str(current_day.day),
                    "distance": day_map.get(current_day, 0.0)
                })

            return jsonify({
                "success": True,
                "stats": {
                    "thisWeekDistance": float(week_summary["total_distance"] or 0),
                    "thisWeekDuration": int(week_summary["total_duration"] or 0),
                    "thisWeekAvgPace": float(week_summary["avg_pace"] or 0),
                    "thisWeekAvgHeartRate": float(week_summary["avg_heart_rate"] or 0),
                    "dailyDistance": daily_distance
                }
            }), 200

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()

@app.route("/competitions", methods=["POST"])
def create_competition():
    data = request.get_json()

    user_id = data.get("userId")
    name = (data.get("name") or "").strip()
    location = (data.get("location") or "").strip()
    competition_date = (data.get("competitionDate") or "").strip()
    sport_id = data.get("sportId")
    event_type = (data.get("eventType") or "").strip()
    description = data.get("description")

    if not user_id or not name or not competition_date or not sport_id or not event_type:
        return jsonify({"success": False, "message": "Missing required competition fields"}), 400

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                INSERT INTO Competition (user_id, name, location, competition_date, sport_id, event_type, description)
                VALUES (%s, %s, %s, %s, %s, %s, %s)
            """, (user_id, name, location, competition_date, sport_id, event_type, description))

            connection.commit()

            return jsonify({
                "success": True,
                "message": "Competition created successfully"
            }), 201

    except Exception as e:
        connection.rollback()
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/competitions/<int:user_id>", methods=["GET"])
def get_competitions(user_id):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT
                    c.competition_id,
                    c.name,
                    c.location,
                    c.competition_date,
                    c.sport_id,
                    c.event_type,
                    s.sport_name,
                    c.description
                FROM Competition c
                JOIN Sports s ON c.sport_id = s.sport_id
                LEFT JOIN CompetitionResult r
                    ON c.competition_id = r.competition_id
                    AND r.user_id = %s
                WHERE c.user_id = %s
                  AND r.result_id IS NULL
                ORDER BY c.competition_date ASC
            """, (user_id, user_id))

            rows = cursor.fetchall()

            competitions = []
            for row in rows:
                competitions.append({
                    "competitionId": row["competition_id"],
                    "name": row["name"],
                    "location": row["location"],
                    "competitionDate": str(row["competition_date"]) if row["competition_date"] is not None else None,
                    "sportId": row["sport_id"],
                    "eventType": row["event_type"],
                    "sportName": row["sport_name"],
                    "description": row["description"]
                })

            return jsonify({
                "success": True,
                "competitions": competitions
            }), 200

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/competition-results", methods=["POST"])
def add_competition_result():
    data = request.get_json()

    user_id = data.get("userId")
    competition_id = data.get("competitionId")
    finish_time = data.get("finishTime")
    position = data.get("position")
    notes = data.get("notes")

    if user_id is None or competition_id is None or finish_time is None:
        return jsonify({
            "success": False,
            "message": "Missing required result fields"
        }), 400

    try:
        user_id = int(user_id)
        competition_id = int(competition_id)
        finish_time = float(finish_time)
    except (TypeError, ValueError):
        return jsonify({
            "success": False,
            "message": "Invalid user, competition, or finish time"
        }), 400

    if finish_time <= 0:
        return jsonify({
            "success": False,
            "message": "Finish time must be greater than 0"
        }), 400

    if position is not None and position != "":
        try:
            position = int(position)
        except (TypeError, ValueError):
            return jsonify({
                "success": False,
                "message": "Position must be a valid number"
            }), 400
    else:
        position = None

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT competition_id, event_type
                FROM Competition
                WHERE competition_id = %s
            """, (competition_id,))
            competition = cursor.fetchone()

            if not competition:
                return jsonify({
                    "success": False,
                    "message": "Competition not found"
                }), 404

            event_type = competition["event_type"]

            cursor.execute("""
                SELECT MIN(r.finish_time) AS best_time
                FROM CompetitionResult r
                JOIN Competition c ON r.competition_id = c.competition_id
                WHERE r.user_id = %s
                  AND c.event_type = %s
            """, (user_id, event_type))
            best_row = cursor.fetchone()

            previous_best = best_row["best_time"] if best_row and best_row["best_time"] is not None else None
            is_personal_best = previous_best is None or finish_time < float(previous_best)

            cursor.execute("""
                INSERT INTO CompetitionResult (
                    user_id,
                    competition_id,
                    finish_time,
                    position,
                    notes,
                    is_personal_best
                )
                VALUES (%s, %s, %s, %s, %s, %s)
            """, (
                user_id,
                competition_id,
                finish_time,
                position,
                notes,
                1 if is_personal_best else 0
            ))

            new_result_id = cursor.lastrowid

            if is_personal_best:
                cursor.execute("""
                    UPDATE CompetitionResult r
                    JOIN Competition c ON r.competition_id = c.competition_id
                    SET r.is_personal_best = 0
                    WHERE r.user_id = %s
                      AND c.event_type = %s
                      AND r.result_id <> %s
                """, (user_id, event_type, new_result_id))

                cursor.execute("""
                    UPDATE CompetitionResult
                    SET is_personal_best = 1
                    WHERE result_id = %s
                """, (new_result_id,))

            connection.commit()

            return jsonify({
                "success": True,
                "message": "Competition result added successfully",
                "isPersonalBest": bool(is_personal_best)
            }), 201

    except Exception as e:
        connection.rollback()
        return jsonify({
            "success": False,
            "message": str(e)
        }), 500

    finally:
        connection.close()


@app.route("/competition-results/<int:user_id>", methods=["GET"])
def get_competition_results(user_id):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT
                    r.result_id,
                    r.user_id,
                    r.competition_id,
                    r.finish_time,
                    r.position,
                    r.notes,
                    r.is_personal_best,
                    c.name,
                    c.location,
                    c.competition_date,
                    c.event_type,
                    s.sport_name
                FROM CompetitionResult r
                JOIN Competition c ON r.competition_id = c.competition_id
                JOIN Sports s ON c.sport_id = s.sport_id
                WHERE r.user_id = %s
                ORDER BY c.competition_date DESC
            """, (user_id,))
            rows = cursor.fetchall()

            results = []
            for row in rows:
                results.append({
                    "resultId": row["result_id"],
                    "userId": row["user_id"],
                    "competitionId": row["competition_id"],
                    "finishTime": float(row["finish_time"]) if row["finish_time"] is not None else None,
                    "position": row["position"],
                    "notes": row["notes"],
                    "isPersonalBest": bool(row["is_personal_best"]),
                    "name": row["name"],
                    "location": row["location"],
                    "competitionDate": str(row["competition_date"]) if row["competition_date"] is not None else None,
                    "eventType": row["event_type"],
                    "sportName": row["sport_name"]
                })

            return jsonify({
                "success": True,
                "results": results
            }), 200

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()

@app.route("/competitions/<int:competition_id>", methods=["DELETE"])
def delete_competition(competition_id):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("DELETE FROM Competition WHERE competition_id = %s", (competition_id,))
            connection.commit()

            if cursor.rowcount == 0:
                return jsonify({"success": False, "message": "Competition not found"}), 404

            return jsonify({
                "success": True,
                "message": "Competition deleted successfully"
            }), 200

    except Exception as e:
        connection.rollback()
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/competitions/<int:competition_id>", methods=["PUT"])
def update_competition(competition_id):
    data = request.get_json()

    name = (data.get("name") or "").strip()
    location = (data.get("location") or "").strip()
    competition_date = (data.get("competitionDate") or "").strip()
    sport_id = data.get("sportId")
    event_type = (data.get("eventType") or "").strip()
    description = data.get("description")

    if not name or not competition_date or not sport_id or not event_type:
        return jsonify({"success": False, "message": "Missing required competition fields"}), 400

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                UPDATE Competition
                SET name = %s,
                    location = %s,
                    competition_date = %s,
                    sport_id = %s,
                    event_type = %s,
                    description = %s
                WHERE competition_id = %s
            """, (
                name,
                location,
                competition_date,
                sport_id,
                event_type,
                description,
                competition_id
            ))

            connection.commit()

            if cursor.rowcount == 0:
                return jsonify({"success": False, "message": "Competition not found"}), 404

            return jsonify({
                "success": True,
                "message": "Competition updated successfully"
            }), 200

    except Exception as e:
        connection.rollback()
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/competition-results/<int:result_id>", methods=["PUT"])
def update_competition_result(result_id):
    data = request.get_json()

    finish_time = data.get("finishTime")
    position = data.get("position")
    notes = data.get("notes")

    if finish_time is None:
        return jsonify({"success": False, "message": "Missing finish time"}), 400

    try:
        finish_time = float(finish_time)
    except (TypeError, ValueError):
        return jsonify({"success": False, "message": "Finish time must be a valid number"}), 400

    if finish_time <= 0:
        return jsonify({"success": False, "message": "Finish time must be greater than 0"}), 400

    if position is not None and position != "":
        try:
            position = int(position)
        except (TypeError, ValueError):
            return jsonify({"success": False, "message": "Position must be a valid number"}), 400
    else:
        position = None

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT r.user_id, c.event_type
                FROM CompetitionResult r
                JOIN Competition c ON r.competition_id = c.competition_id
                WHERE r.result_id = %s
            """, (result_id,))
            existing = cursor.fetchone()

            if not existing:
                return jsonify({"success": False, "message": "Result not found"}), 404

            user_id = existing["user_id"]
            event_type = existing["event_type"]

            cursor.execute("""
                UPDATE CompetitionResult
                SET finish_time = %s,
                    position = %s,
                    notes = %s
                WHERE result_id = %s
            """, (
                finish_time,
                position,
                notes,
                result_id
            ))

            cursor.execute("""
                UPDATE CompetitionResult r
                JOIN Competition c ON r.competition_id = c.competition_id
                SET r.is_personal_best = 0
                WHERE r.user_id = %s
                  AND c.event_type = %s
            """, (user_id, event_type))

            cursor.execute("""
                SELECT r.result_id
                FROM CompetitionResult r
                JOIN Competition c ON r.competition_id = c.competition_id
                WHERE r.user_id = %s
                  AND c.event_type = %s
                ORDER BY r.finish_time ASC, r.result_id ASC
                LIMIT 1
            """, (user_id, event_type))
            best = cursor.fetchone()

            if best:
                cursor.execute("""
                    UPDATE CompetitionResult
                    SET is_personal_best = 1
                    WHERE result_id = %s
                """, (best["result_id"],))

            connection.commit()

            return jsonify({
                "success": True,
                "message": "Result updated successfully"
            }), 200

    except Exception as e:
        connection.rollback()
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/competition-results/<int:result_id>", methods=["DELETE"])
def delete_competition_result(result_id):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT r.user_id, c.event_type
                FROM CompetitionResult r
                JOIN Competition c ON r.competition_id = c.competition_id
                WHERE r.result_id = %s
            """, (result_id,))
            existing = cursor.fetchone()

            if not existing:
                return jsonify({"success": False, "message": "Result not found"}), 404

            user_id = existing["user_id"]
            event_type = existing["event_type"]

            cursor.execute("DELETE FROM CompetitionResult WHERE result_id = %s", (result_id,))

            cursor.execute("""
                UPDATE CompetitionResult r
                JOIN Competition c ON r.competition_id = c.competition_id
                SET r.is_personal_best = 0
                WHERE r.user_id = %s
                  AND c.event_type = %s
            """, (user_id, event_type))

            cursor.execute("""
                SELECT r.result_id
                FROM CompetitionResult r
                JOIN Competition c ON r.competition_id = c.competition_id
                WHERE r.user_id = %s
                  AND c.event_type = %s
                ORDER BY r.finish_time ASC, r.result_id ASC
                LIMIT 1
            """, (user_id, event_type))
            best = cursor.fetchone()

            if best:
                cursor.execute("""
                    UPDATE CompetitionResult
                    SET is_personal_best = 1
                    WHERE result_id = %s
                """, (best["result_id"],))

            connection.commit()

            return jsonify({
                "success": True,
                "message": "Result deleted successfully"
            }), 200

    except Exception as e:
        connection.rollback()
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/conversations/direct", methods=["POST"])
def create_or_get_direct_conversation():
    data = request.get_json()

    user1_id = data.get("user1Id")
    user2_id = data.get("user2Id")

    if not user1_id or not user2_id:
        return jsonify({"success": False, "message": "Missing user ids"}), 400

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT c.conversation_id
                FROM Conversation c
                JOIN ConversationParticipant cp1 ON c.conversation_id = cp1.conversation_id
                JOIN ConversationParticipant cp2 ON c.conversation_id = cp2.conversation_id
                WHERE c.conversation_type = 'direct'
                  AND cp1.user_id = %s
                  AND cp2.user_id = %s
                LIMIT 1
            """, (user1_id, user2_id))
            existing = cursor.fetchone()

            if existing:
                return jsonify({
                    "success": True,
                    "conversationId": existing["conversation_id"]
                }), 200

            cursor.execute("""
                INSERT INTO Conversation (conversation_type, title, created_by_user_id)
                VALUES ('direct', NULL, %s)
            """, (user1_id,))
            conversation_id = cursor.lastrowid

            cursor.execute("""
                INSERT INTO ConversationParticipant (conversation_id, user_id, participant_role)
                VALUES (%s, %s, 'member')
            """, (conversation_id, user1_id))

            cursor.execute("""
                INSERT INTO ConversationParticipant (conversation_id, user_id, participant_role)
                VALUES (%s, %s, 'member')
            """, (conversation_id, user2_id))

            connection.commit()

            return jsonify({
                "success": True,
                "conversationId": conversation_id
            }), 201

    except Exception as e:
        connection.rollback()
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/conversations/group", methods=["POST"])
def create_group_conversation():
    data = request.get_json()

    created_by_user_id = data.get("createdByUserId")
    title = (data.get("title") or "").strip()
    member_ids = data.get("memberIds") or []

    if not created_by_user_id or not title:
        return jsonify({"success": False, "message": "Missing group info"}), 400

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                INSERT INTO Conversation (conversation_type, title, created_by_user_id)
                VALUES ('group', %s, %s)
            """, (title, created_by_user_id))
            conversation_id = cursor.lastrowid

            all_members = set(member_ids)
            all_members.add(created_by_user_id)

            for user_id in all_members:
                role = "creator" if user_id == created_by_user_id else "member"
                cursor.execute("""
                    INSERT INTO ConversationParticipant (conversation_id, user_id, participant_role)
                    VALUES (%s, %s, %s)
                """, (conversation_id, user_id, role))

            connection.commit()

            return jsonify({
                "success": True,
                "conversationId": conversation_id,
                "message": "Group created successfully"
            }), 201

    except Exception as e:
        connection.rollback()
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()

@app.route("/conversations/<int:user_id>", methods=["GET"])
def get_user_conversations(user_id):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT
                    c.conversation_id,
                    c.conversation_type,
                    CASE
                        WHEN c.conversation_type = 'group' THEN c.title
                        ELSE (
                            SELECT u2.name
                            FROM ConversationParticipant cp2
                            JOIN Users u2 ON cp2.user_id = u2.user_id
                            WHERE cp2.conversation_id = c.conversation_id
                              AND cp2.user_id <> %s
                            LIMIT 1
                        )
                    END AS title,
                    CASE
                        WHEN c.conversation_type = 'group' THEN NULL
                        ELSE (
                            SELECT u2.username
                            FROM ConversationParticipant cp2
                            JOIN Users u2 ON cp2.user_id = u2.user_id
                            WHERE cp2.conversation_id = c.conversation_id
                              AND cp2.user_id <> %s
                            LIMIT 1
                        )
                    END AS username,
                    c.created_at,
                    (
                        SELECT m.message_text
                        FROM Message m
                        WHERE m.conversation_id = c.conversation_id
                        ORDER BY m.sent_at DESC, m.message_id DESC
                        LIMIT 1
                    ) AS last_message,
                    (
                        SELECT m.sent_at
                        FROM Message m
                        WHERE m.conversation_id = c.conversation_id
                        ORDER BY m.sent_at DESC, m.message_id DESC
                        LIMIT 1
                    ) AS last_message_time
                FROM Conversation c
                JOIN ConversationParticipant cp ON c.conversation_id = cp.conversation_id
                WHERE cp.user_id = %s
                ORDER BY last_message_time DESC, c.created_at DESC
            """, (user_id, user_id, user_id))

            rows = cursor.fetchall()

            conversations = []
            for row in rows:
                conversations.append({
                    "conversationId": row["conversation_id"],
                    "conversationType": row["conversation_type"],
                    "title": row["title"],
                    "username": row["username"],
                    "createdAt": str(row["created_at"]) if row["created_at"] is not None else None,
                    "lastMessage": row["last_message"],
                    "lastMessageTime": str(row["last_message_time"]) if row["last_message_time"] is not None else None
                })

            return jsonify({
                "success": True,
                "conversations": conversations
            }), 200

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/messages/<int:conversation_id>", methods=["GET"])
def get_messages(conversation_id):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT
                    m.message_id,
                    m.conversation_id,
                    m.sender_user_id,
                    u.name AS sender_name,
                    m.message_text,
                    m.sent_at
                FROM Message m
                JOIN Users u ON m.sender_user_id = u.user_id
                WHERE m.conversation_id = %s
                ORDER BY m.sent_at ASC
            """, (conversation_id,))
            rows = cursor.fetchall()

            messages = []
            for row in rows:
                messages.append({
                    "messageId": row["message_id"],
                    "conversationId": row["conversation_id"],
                    "senderUserId": row["sender_user_id"],
                    "senderName": row["sender_name"],
                    "messageText": row["message_text"],
                    "sentAt": str(row["sent_at"]) if row["sent_at"] is not None else None
                })

            return jsonify({
                "success": True,
                "messages": messages
            }), 200

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/messages", methods=["POST"])
def send_message():
    data = request.get_json()

    conversation_id = data.get("conversationId")
    sender_user_id = data.get("senderUserId")
    message_text = (data.get("messageText") or "").strip()

    if not conversation_id or not sender_user_id or not message_text:
        return jsonify({"success": False, "message": "Missing message fields"}), 400

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT 1
                FROM ConversationParticipant
                WHERE conversation_id = %s AND user_id = %s
            """, (conversation_id, sender_user_id))
            membership = cursor.fetchone()

            if not membership:
                return jsonify({"success": False, "message": "User not in conversation"}), 403

            cursor.execute("""
                INSERT INTO Message (conversation_id, sender_user_id, message_text)
                VALUES (%s, %s, %s)
            """, (conversation_id, sender_user_id, message_text))

            connection.commit()

            return jsonify({
                "success": True,
                "message": "Message sent successfully"
            }), 201

    except Exception as e:
        connection.rollback()
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/conversations/group/<int:conversation_id>/members", methods=["POST"])
def add_group_member(conversation_id):
    data = request.get_json()
    user_id = data.get("userId")

    if not user_id:
        return jsonify({"success": False, "message": "Missing user id"}), 400

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                INSERT INTO ConversationParticipant (conversation_id, user_id, participant_role)
                VALUES (%s, %s, 'member')
            """, (conversation_id, user_id))

            connection.commit()

            return jsonify({
                "success": True,
                "message": "Member added successfully"
            }), 201

    except Exception as e:
        connection.rollback()
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/athlete-trainer/<int:athlete_id>", methods=["GET"])
def get_athlete_trainer(athlete_id):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT
                    u.user_id,
                    u.name,
                    u.email
                FROM TrainerClients tc
                JOIN Users u ON tc.trainer_id = u.user_id
                WHERE tc.client_id = %s
                LIMIT 1
            """, (athlete_id,))
            trainer = cursor.fetchone()

            if not trainer:
                return jsonify({
                    "success": False,
                    "message": "No trainer found for this athlete"
                }), 404

            trainer_item = {
                "userId": trainer["user_id"],
                "name": trainer["name"],
                "email": trainer["email"]
            }

            return jsonify({
                "success": True,
                "trainer": trainer_item
            }), 200

    except Exception as e:
        return jsonify({
            "success": False,
            "message": str(e)
        }), 500

    finally:
        connection.close()


@app.route("/conversations/group/by-usernames", methods=["POST"])
def create_group_conversation_by_usernames():
    data = request.get_json()

    created_by_user_id = data.get("createdByUserId")
    title = (data.get("title") or "").strip()
    usernames = data.get("usernames") or []

    if not created_by_user_id or not title:
        return jsonify({"success": False, "message": "Missing group info"}), 400

    cleaned_usernames = []
    for username in usernames:
        if username and isinstance(username, str):
            cleaned = username.strip().lower()
            if cleaned:
                cleaned_usernames.append(cleaned)

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT user_id
                FROM Users
                WHERE user_id = %s
            """, (created_by_user_id,))
            creator = cursor.fetchone()

            if not creator:
                return jsonify({"success": False, "message": "Creator not found"}), 404

            member_ids = set()

            for username in cleaned_usernames:
                cursor.execute("""
                    SELECT user_id
                    FROM Users
                    WHERE username = %s
                """, (username,))
                user = cursor.fetchone()

                if not user:
                    return jsonify({
                        "success": False,
                        "message": f"User not found: {username}"
                    }), 404

                member_ids.add(user["user_id"])

            member_ids.add(created_by_user_id)

            if len(member_ids) < 2:
                return jsonify({
                    "success": False,
                    "message": "A group must include at least one other user"
                }), 400

            cursor.execute("""
                INSERT INTO Conversation (conversation_type, title, created_by_user_id)
                VALUES ('group', %s, %s)
            """, (title, created_by_user_id))
            conversation_id = cursor.lastrowid

            for user_id in member_ids:
                role = "creator" if user_id == created_by_user_id else "member"
                cursor.execute("""
                    INSERT INTO ConversationParticipant (conversation_id, user_id, participant_role)
                    VALUES (%s, %s, %s)
                """, (conversation_id, user_id, role))

            connection.commit()

            return jsonify({
                "success": True,
                "conversationId": conversation_id,
                "message": "Group created successfully"
            }), 201

    except Exception as e:
        connection.rollback()
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/conversations/group/<int:conversation_id>/members/by-username", methods=["POST"])
def add_group_member_by_username(conversation_id):
    data = request.get_json()
    username = (data.get("username") or "").strip().lower()

    if not username:
        return jsonify({"success": False, "message": "Username is required"}), 400

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT conversation_id
                FROM Conversation
                WHERE conversation_id = %s
                  AND conversation_type = 'group'
            """, (conversation_id,))
            group_row = cursor.fetchone()

            if not group_row:
                return jsonify({"success": False, "message": "Group conversation not found"}), 404

            cursor.execute("""
                SELECT user_id, name, username
                FROM Users
                WHERE username = %s
            """, (username,))
            user = cursor.fetchone()

            if not user:
                return jsonify({"success": False, "message": "Username not found"}), 404

            cursor.execute("""
                SELECT 1
                FROM ConversationParticipant
                WHERE conversation_id = %s
                  AND user_id = %s
            """, (conversation_id, user["user_id"]))
            existing = cursor.fetchone()

            if existing:
                return jsonify({"success": False, "message": "User is already in the group"}), 409

            cursor.execute("""
                INSERT INTO ConversationParticipant (conversation_id, user_id, participant_role)
                VALUES (%s, %s, 'member')
            """, (conversation_id, user["user_id"]))

            connection.commit()

            return jsonify({
                "success": True,
                "message": "Member added successfully"
            }), 201

    except Exception as e:
        connection.rollback()
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/conversations/group/<int:conversation_id>/leave", methods=["POST"])
def leave_group(conversation_id):
    data = request.get_json()
    user_id = data.get("userId")

    if not user_id:
        return jsonify({"success": False, "message": "User id is required"}), 400

    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT conversation_id
                FROM Conversation
                WHERE conversation_id = %s
                  AND conversation_type = 'group'
            """, (conversation_id,))
            group_row = cursor.fetchone()

            if not group_row:
                return jsonify({"success": False, "message": "Group conversation not found"}), 404

            cursor.execute("""
                SELECT 1
                FROM ConversationParticipant
                WHERE conversation_id = %s
                  AND user_id = %s
            """, (conversation_id, user_id))
            membership = cursor.fetchone()

            if not membership:
                return jsonify({"success": False, "message": "User is not in this group"}), 404

            cursor.execute("""
                DELETE FROM ConversationParticipant
                WHERE conversation_id = %s
                  AND user_id = %s
            """, (conversation_id, user_id))

            cursor.execute("""
                SELECT COUNT(*) AS member_count
                FROM ConversationParticipant
                WHERE conversation_id = %s
            """, (conversation_id,))
            count_row = cursor.fetchone()

            if count_row["member_count"] == 0:
                cursor.execute("""
                    DELETE FROM Conversation
                    WHERE conversation_id = %s
                """, (conversation_id,))

            connection.commit()

            return jsonify({
                "success": True,
                "message": "Left group successfully"
            }), 200

    except Exception as e:
        connection.rollback()
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


@app.route("/conversations/group/<int:conversation_id>/members", methods=["GET"])
def get_group_members(conversation_id):
    connection = get_connection()

    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT conversation_id, title
                FROM Conversation
                WHERE conversation_id = %s
                  AND conversation_type = 'group'
            """, (conversation_id,))
            group_row = cursor.fetchone()

            if not group_row:
                return jsonify({"success": False, "message": "Group conversation not found"}), 404

            cursor.execute("""
                SELECT
                    u.user_id,
                    u.name,
                    u.username,
                    cp.participant_role,
                    cp.joined_at
                FROM ConversationParticipant cp
                JOIN Users u ON cp.user_id = u.user_id
                WHERE cp.conversation_id = %s
                ORDER BY
                    CASE WHEN cp.participant_role = 'creator' THEN 0 ELSE 1 END,
                    u.name ASC
            """, (conversation_id,))
            rows = cursor.fetchall()

            members = []
            for row in rows:
                members.append({
                    "userId": row["user_id"],
                    "name": row["name"],
                    "username": row["username"],
                    "participantRole": row["participant_role"],
                    "joinedAt": str(row["joined_at"]) if row["joined_at"] is not None else None
                })

            return jsonify({
                "success": True,
                "groupTitle": group_row["title"],
                "members": members
            }), 200

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

    finally:
        connection.close()


if __name__ == "__main__":
    import os
    port = int(os.environ.get("PORT", 5001))
    app.run(host="0.0.0.0", port=port)
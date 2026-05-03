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
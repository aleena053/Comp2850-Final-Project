from flask import Flask, render_template, request, redirect, session
import mysql.connector

app = Flask(__name__)
app.secret_key = 'fitness_secret'

# connects to database
db = mysql.connector.connect(
    host="localhost",
    user="your_username",
    password="your_password",
    database="fitness_app" 
)

@app.route('/')
def home():
    return render_template('index.html')

if __name__ == '__main__':
    app.run(debug=True)

@app.route('/register', methods=['POST'])
def register():
    # extracting data from the HTML form
    name = request.form.get('Name')
    email = request.form.get('email')
    password = request.form.get('password')
    level = request.form.get('fitness_level') # from dropdown selection

    cursor = db.cursor()
    # matches the users table structure in the database
    sql = "INSERT INTO Users (Name, email, password, fitness_level, role) VALUES (%s, %s, %s, %s, %s)"
    cursor.execute(sql, (name, email, password, level, 'casual'))
    db.commit()
    return redirect('/dashboard')

    @app.route('/log_workout', methods=['POST'])
def log_workout():
    cursor = db.cursor()
    # Matches the Workout table requirement for durations and decimals
    sql = """INSERT INTO Workout (user_id, sport_id, workout_date, duration, distance_km, notes) 
             VALUES (%s, %s, %s, %s, %s, %s)"""
    
    values = (
        session['user_id'], 
        request.form.get('sport_id'),
        request.form.get('date'),
        request.form.get('duration'),
        request.form.get('distance_km'), # Matches DECIMAL(10,2)
        request.form.get('notes')
    )
    cursor.execute(sql, values)
    db.commit()
    return "Workout Saved Successfully!"

    @app.route('/save_competition', methods=['POST'])
def save_competition():
    cursor = db.cursor()
    # Uses column names: finish_time, position, and notes[cite: 2]
    sql = """
        INSERT INTO CompetitionResult (user_id, competition_id, finish_time, position, notes) 
        VALUES (%s, %s, %s, %s, %s)
    """
    data = (
        session['user_id'], 
        request.form.get('competition_id'), 
        request.form.get('finish_time'), # Decimal[cite: 2]
        request.form.get('position'), 
        request.form.get('notes')
    )
    cursor.execute(sql, data)
    db.commit()
    return redirect('/dashboard')

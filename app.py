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

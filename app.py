from flask import Flask, render_template, request, redirect, session
import mysql.connector

app = Flask(__name__)
app.secret_key = 'fitness_secret'

# Connects to the database named in your schema
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

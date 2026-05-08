import os


class Config:
    MYSQL_HOST = os.environ.get("DB_HOST", "localhost")
    MYSQL_USER = os.environ.get("DB_USER", "root")
    MYSQL_PASSWORD = os.environ.get("DB_PASSWORD", "replace_with_your_db_password")
    MYSQL_DB = os.environ.get("DB_NAME", "fitness_app")
    MYSQL_PORT = int(os.environ.get("DB_PORT", 3306))
import psycopg2

from parser.config import settings


def get_connection():
    return psycopg2.connect(settings.database_dsn)
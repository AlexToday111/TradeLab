from pathlib import Path

import psycopg

from parser.common.config.settings import settings
from parser.common.exceptions import RepositoryError

SCHEMA_PATH = Path(__file__).with_name("schema.sql")


def get_connection():
    try:
        return psycopg.connect(settings.database_dsn)
    except psycopg.Error as exc:
        raise RepositoryError("Failed to connect to PostgreSQL") from exc


def initialize_schema(connection) -> None:
    try:
        schema_sql = SCHEMA_PATH.read_text(encoding="utf-8")
        with connection.cursor() as cursor:
            cursor.execute(schema_sql)
        connection.commit()
    except OSError as exc:
        connection.rollback()
        raise RepositoryError("Failed to read database schema") from exc
    except psycopg.Error as exc:
        connection.rollback()
        raise RepositoryError("Failed to initialize database schema") from exc

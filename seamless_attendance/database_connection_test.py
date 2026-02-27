import os
import sys
import logging
from sqlalchemy import create_engine, text
from sqlalchemy.exc import SQLAlchemyError

# -*- coding: utf-8 -*-
"""
Database connection test utility.
Usage:
	- Set DATABASE_URL (preferred) e.g. postgresql+psycopg2://user:pass@host:port/dbname
	- OR set DB_TYPE (sqlite/postgresql/mysql/mssql) and DB_USER, DB_PASSWORD, DB_HOST, DB_PORT, DB_NAME
	- Then run: python database_connection_test.py
"""

logging.basicConfig(level=logging.INFO, format="%(levelname)s: %(message)s")


def build_database_url():
	# If a full URL is provided, use it
	url = os.getenv("//root:pass@47.108.57.12:3306/mysql")
	if url:
		return url

	db_type = os.getenv("DB_TYPE", "sqlite").lower()
	name = os.getenv("DB_NAME", "mqtt_data")
	user = os.getenv("DB_USER", "root")
	password = os.getenv("DB_PASSWORD", "123456")
	host = os.getenv("DB_HOST", "47.108.57.12")
	port = os.getenv("DB_PORT", "3306")

	if db_type == "sqlite":
		# file path or in-memory: use DB_NAME=":memory:" for in-memory
		if name == ":memory:":
				return "sqlite:///:memory:"
		# if absolute path provided, keep it; otherwise create relative file
		return f"sqlite:///{os.path.abspath(name)}"

	if db_type in ("postgresql", "postgres"):
		driver = os.getenv("DB_DRIVER", "psycopg2")
		port_part = f":{port}" if port else ""
		auth = f"{user}:{password}@" if user or password else ""
		return f"postgresql+{driver}://{auth}{host}{port_part}/{name}"

	if db_type in ("mysql",):
		driver = os.getenv("DB_DRIVER", "pymysql")
		port_part = f":{port}" if port else ""
		auth = f"{user}:{password}@" if user or password else ""
		return f"mysql+{driver}://{auth}{host}{port_part}/{name}"

	if db_type in ("mssql", "sqlserver"):
		# using pyodbc and a DSN-like connection string
		driver = os.getenv("DB_DRIVER", "pyodbc")
		port_part = f",{port}" if port else ""
		auth = f"{user}:{password}@" if user or password else ""
		# For SQL Server with pyodbc it's common to use a connection string; simplest approach:
		return f"mssql+{driver}://{auth}{host}{port_part}/{name}?driver=ODBC+Driver+17+for+SQL+Server"

	raise ValueError(f"Unsupported DB_TYPE: {db_type}")


def get_engine(echo=False):
	url = build_database_url()
	logging.info("Connecting to database URL: %s", url if len(url) < 200 else url[:200] + "...")
	# safe defaults: pool_pre_ping helps with stale connections
	engine = create_engine(url, echo=echo, pool_pre_ping=True)
	return engine


def test_connection(engine, timeout_seconds=5):
		try:
			with engine.connect() as conn:
				# use a lightweight test query
				result = conn.execute(text("SELECT 1"))
				row = result.fetchone()
				if row is None:
						logging.error("Test query returned no rows.")
						return False
				logging.info("Connection test succeeded, query result: %s", row[0])
				return True
		except SQLAlchemyError as e:
			logging.error("Database connection/test failed: %s", e)
			return False


def main():
		try:
			engine = get_engine(echo=False)
		except Exception as e:
			logging.error("Failed to build engine: %s", e)
			sys.exit(1)

		ok = test_connection(engine)
		if not ok:
			logging.error("Connection test failed.")
			sys.exit(2)

		logging.info("Database connection OK.")


if __name__ == "__main__":
	main()
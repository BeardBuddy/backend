-- PostgreSQL schema. Column names are camelCase and Hibernate's physical naming strategy is
-- the standard (non-snake-casing) one, so @Column names are used verbatim. Identifiers are
-- double-quoted because Postgres folds unquoted names to lower case.
--
-- This script owns the schema; Hibernate's ddl-auto is 'none' so the CHECK constraints survive
-- (Hibernate would not generate them).
--
-- Every table is dropped and recreated on each startup, so a run always ends up with exactly the
-- seed in data.sql regardless of what the previous run left behind. That makes startup idempotent
-- and immune to a stale beardbuddy.db from an older schema.
--
-- Children before parents: foreign keys are enforced.
DROP TABLE IF EXISTS review CASCADE;
DROP TABLE IF EXISTS appointment_extra CASCADE;
DROP TABLE IF EXISTS appointment CASCADE;
DROP TABLE IF EXISTS service_sub_service CASCADE;
DROP TABLE IF EXISTS barber_service CASCADE;
DROP TABLE IF EXISTS schedule CASCADE;
DROP TABLE IF EXISTS extra_service CASCADE;
DROP TABLE IF EXISTS service CASCADE;
DROP TABLE IF EXISTS app_user CASCADE;

CREATE TABLE IF NOT EXISTS app_user (
  id                  TEXT PRIMARY KEY,
  first_name           TEXT NOT NULL,
  last_name            TEXT NOT NULL,
  phone               TEXT NOT NULL,
  email               TEXT,
  date_of_birth         TEXT NOT NULL,
  username            TEXT UNIQUE,
  password_hash        TEXT,
  role                TEXT NOT NULL CHECK(role IN ('CUSTOMER','BARBER')),
  seniority_level      TEXT CHECK(seniority_level IN ('SENIOR','JUNIOR')),
  specialization_type  TEXT CHECK(specialization_type IN ('HAIRCUT','BEARD')),
  experience_years     INTEGER,
  hire_date            TEXT,
  description         TEXT,
  loyalty_points       INTEGER DEFAULT 0,
  management_access    BOOLEAN DEFAULT FALSE,
  can_mentor           BOOLEAN DEFAULT FALSE,
  certifications      TEXT,
  max_clients_per_day    INTEGER,
  scissors_mastery     BOOLEAN,
  supports_long_hair    BOOLEAN,
  trim_mastery         BOOLEAN,
  supports_hot_towel    BOOLEAN,
  beard_care_knowledge  TEXT
);

CREATE TABLE IF NOT EXISTS service (
  id               TEXT PRIMARY KEY,
  name             TEXT NOT NULL,
  price            NUMERIC(10,2) NOT NULL,
  type             TEXT NOT NULL CHECK(type IN ('HAIRCUT','BEARD','HYBRID')),
  duration         INTEGER NOT NULL,
  description      TEXT NOT NULL,
  is_available      BOOLEAN DEFAULT TRUE,
  requires_styling  BOOLEAN,
  complexity_level  TEXT CHECK(complexity_level IN ('BEGINNER','INTERMEDIATE','EXPERT'))
);

CREATE TABLE IF NOT EXISTS service_sub_service (
  service_id    TEXT NOT NULL REFERENCES service(id),
  sub_service_id TEXT NOT NULL REFERENCES service(id),
  PRIMARY KEY (service_id, sub_service_id)
);

-- A service may be offered by several barbers, and a barber offers several services:
-- a genuine many-to-many, with the pair kept unique so the same barber cannot be
-- registered twice for the same service.
CREATE TABLE IF NOT EXISTS barber_service (
  id                 TEXT PRIMARY KEY,
  barber_id           TEXT NOT NULL REFERENCES app_user(id),
  service_id          TEXT NOT NULL REFERENCES service(id),
  seniority          TEXT NOT NULL CHECK(seniority IN ('SENIOR','JUNIOR')),
  specialization_type TEXT NOT NULL CHECK(specialization_type IN ('HAIRCUT','BEARD')),
  years_of_experience  INTEGER,
  certification_level TEXT CHECK(certification_level IN ('BEGINNER','INTERMEDIATE','EXPERT')),
  courses_completed   TEXT,
  acquired_at         TEXT,
  notes              TEXT,
  UNIQUE (barber_id, service_id)
);

CREATE TABLE IF NOT EXISTS schedule (
  id        TEXT PRIMARY KEY,
  barber_id  TEXT NOT NULL REFERENCES app_user(id),
  day_of_week TEXT NOT NULL CHECK(day_of_week IN ('MON','TUE','WED','THU','FRI','SAT','SUN')),
  start_time TEXT NOT NULL,
  end_time   TEXT NOT NULL,
  valid_from TEXT NOT NULL,
  valid_to   TEXT NOT NULL,
  is_active  INTEGER DEFAULT 1
);

CREATE TABLE IF NOT EXISTS extra_service (
  id          TEXT PRIMARY KEY,
  type        TEXT NOT NULL CHECK(type IN ('ALCOHOL','CIGAR','CARD_GAME')),
  name        TEXT NOT NULL,
  price       NUMERIC(10,2) NOT NULL,
  description TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS appointment (
  id                  TEXT PRIMARY KEY,
  customer_id          TEXT NOT NULL REFERENCES app_user(id),
  barber_id            TEXT NOT NULL REFERENCES app_user(id),
  service_id           TEXT NOT NULL REFERENCES service(id),
  date                TEXT NOT NULL,
  start_time           TEXT NOT NULL,
  end_time             TEXT NOT NULL,
  status              TEXT NOT NULL DEFAULT 'NEW' CHECK(status IN ('NEW','CONFIRMED','IN_PROGRESS','COMPLETED','CANCELLED')),
  payment_status       TEXT NOT NULL DEFAULT 'UNPAID' CHECK(payment_status IN ('UNPAID','PAID','PENDING')),
  payment_method       TEXT NOT NULL DEFAULT 'CASH' CHECK(payment_method IN ('CARD','CASH','MOBILE')),
  total_price          NUMERIC(10,2) NOT NULL DEFAULT 0,
  notes               TEXT,
  cancellation_reason  TEXT,
  paid_at              TEXT,
  cancelled_at         TEXT
);

CREATE TABLE IF NOT EXISTS appointment_extra (
  appointment_id  TEXT NOT NULL REFERENCES appointment(id),
  extra_service_id TEXT NOT NULL REFERENCES extra_service(id),
  PRIMARY KEY (appointment_id, extra_service_id)
);

CREATE TABLE IF NOT EXISTS review (
  id             TEXT PRIMARY KEY,
  appointment_id  TEXT NOT NULL UNIQUE REFERENCES appointment(id),
  customer_id     TEXT NOT NULL REFERENCES app_user(id),
  rating         INTEGER NOT NULL CHECK(rating BETWEEN 1 AND 5),
  comment        TEXT,
  date           TEXT NOT NULL
);

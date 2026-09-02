-- Schema for the Java backend. Column names are camelCase and Hibernate's physical naming
-- strategy is set to the standard (non-snake-casing) one so @Column names are used verbatim.
--
-- This script owns the schema; Hibernate's ddl-auto is 'none' so the CHECK constraints survive
-- (Hibernate would not generate them).
--
-- Every table is dropped and recreated on each startup, so a run always ends up with exactly the
-- seed in data.sql regardless of what the previous run left behind. That makes startup idempotent
-- and immune to a stale beardbuddy.db from an older schema.
--
-- Children before parents: foreign keys are enforced.
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS appointment_extra;
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS service_sub_service;
DROP TABLE IF EXISTS barber_service;
DROP TABLE IF EXISTS schedule;
DROP TABLE IF EXISTS extra_service;
DROP TABLE IF EXISTS service;
DROP TABLE IF EXISTS user;

CREATE TABLE IF NOT EXISTS user (
  id                  TEXT PRIMARY KEY,
  firstName           TEXT NOT NULL,
  lastName            TEXT NOT NULL,
  phone               TEXT NOT NULL,
  email               TEXT,
  dateOfBirth         TEXT NOT NULL,
  role                TEXT NOT NULL CHECK(role IN ('CUSTOMER','BARBER')),
  seniorityLevel      TEXT CHECK(seniorityLevel IN ('SENIOR','JUNIOR')),
  specializationType  TEXT CHECK(specializationType IN ('HAIRCUT','BEARD')),
  experienceYears     INTEGER,
  hireDate            TEXT,
  description         TEXT,
  loyaltyPoints       INTEGER DEFAULT 0,
  managementAccess    INTEGER DEFAULT 0,
  canMentor           INTEGER DEFAULT 0,
  certifications      TEXT,
  maxClientsPerDay    INTEGER,
  scissorsMastery     INTEGER,
  supportsLongHair    INTEGER,
  trimMastery         INTEGER,
  supportsHotTowel    INTEGER,
  beardCareKnowledge  TEXT
);

CREATE TABLE IF NOT EXISTS service (
  id               TEXT PRIMARY KEY,
  name             TEXT NOT NULL,
  price            REAL NOT NULL,
  type             TEXT NOT NULL CHECK(type IN ('HAIRCUT','BEARD','HYBRID')),
  duration         INTEGER NOT NULL,
  description      TEXT NOT NULL,
  isAvailable      INTEGER DEFAULT 1,
  requiresStyling  INTEGER,
  complexityLevel  TEXT CHECK(complexityLevel IN ('BEGINNER','INTERMEDIATE','EXPERT'))
);

CREATE TABLE IF NOT EXISTS service_sub_service (
  serviceId    TEXT NOT NULL REFERENCES service(id),
  subServiceId TEXT NOT NULL REFERENCES service(id),
  PRIMARY KEY (serviceId, subServiceId)
);

-- A service may be offered by several barbers, and a barber offers several services:
-- a genuine many-to-many, with the pair kept unique so the same barber cannot be
-- registered twice for the same service.
CREATE TABLE IF NOT EXISTS barber_service (
  id                 TEXT PRIMARY KEY,
  barberId           TEXT NOT NULL REFERENCES user(id),
  serviceId          TEXT NOT NULL REFERENCES service(id),
  seniority          TEXT NOT NULL CHECK(seniority IN ('SENIOR','JUNIOR')),
  specializationType TEXT NOT NULL CHECK(specializationType IN ('HAIRCUT','BEARD')),
  yearsOfExperience  INTEGER,
  certificationLevel TEXT CHECK(certificationLevel IN ('BEGINNER','INTERMEDIATE','EXPERT')),
  coursesCompleted   TEXT,
  acquiredAt         TEXT,
  notes              TEXT,
  UNIQUE (barberId, serviceId)
);

CREATE TABLE IF NOT EXISTS schedule (
  id        TEXT PRIMARY KEY,
  barberId  TEXT NOT NULL REFERENCES user(id),
  dayOfWeek TEXT NOT NULL CHECK(dayOfWeek IN ('MON','TUE','WED','THU','FRI','SAT','SUN')),
  startTime TEXT NOT NULL,
  endTime   TEXT NOT NULL,
  validFrom TEXT NOT NULL,
  validTo   TEXT NOT NULL,
  isActive  INTEGER DEFAULT 1
);

CREATE TABLE IF NOT EXISTS extra_service (
  id          TEXT PRIMARY KEY,
  type        TEXT NOT NULL CHECK(type IN ('ALCOHOL','CIGAR','CARD_GAME')),
  name        TEXT NOT NULL,
  price       REAL NOT NULL,
  description TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS appointment (
  id                  TEXT PRIMARY KEY,
  customerId          TEXT NOT NULL REFERENCES user(id),
  barberId            TEXT NOT NULL REFERENCES user(id),
  serviceId           TEXT NOT NULL REFERENCES service(id),
  date                TEXT NOT NULL,
  startTime           TEXT NOT NULL,
  endTime             TEXT NOT NULL,
  status              TEXT NOT NULL DEFAULT 'NEW' CHECK(status IN ('NEW','CONFIRMED','IN_PROGRESS','COMPLETED','CANCELLED')),
  paymentStatus       TEXT NOT NULL DEFAULT 'UNPAID' CHECK(paymentStatus IN ('UNPAID','PAID','PENDING')),
  paymentMethod       TEXT NOT NULL DEFAULT 'CASH' CHECK(paymentMethod IN ('CARD','CASH','MOBILE')),
  totalPrice          REAL NOT NULL DEFAULT 0,
  notes               TEXT,
  cancellationReason  TEXT,
  paidAt              TEXT,
  cancelledAt         TEXT
);

CREATE TABLE IF NOT EXISTS appointment_extra (
  appointmentId  TEXT NOT NULL REFERENCES appointment(id),
  extraServiceId TEXT NOT NULL REFERENCES extra_service(id),
  PRIMARY KEY (appointmentId, extraServiceId)
);

CREATE TABLE IF NOT EXISTS review (
  id             TEXT PRIMARY KEY,
  appointmentId  TEXT NOT NULL UNIQUE REFERENCES appointment(id),
  customerId     TEXT NOT NULL REFERENCES user(id),
  rating         INTEGER NOT NULL CHECK(rating BETWEEN 1 AND 5),
  comment        TEXT,
  date           TEXT NOT NULL
);

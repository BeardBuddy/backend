-- Seed data. INSERT OR IGNORE keeps it idempotent; schema.sql drops and recreates every table on
-- startup, so a run always ends up with exactly this dataset.
--
-- Key rule: each service has its OWN barbers — no barber is shared between two services.
--   Classic Scissor Cut        -> Marcus + Elena
--   Classic Beard Trim & Shape -> Leo + Viktor
--   Hot Towel Royal Shave      -> nobody (alternative flow 4A)

-- ───────────────────────────────────────────────
-- Customer
-- ───────────────────────────────────────────────
INSERT OR IGNORE INTO user (id, firstName, lastName, phone, email, dateOfBirth, role, loyaltyPoints)
VALUES (
  'a1b2c3d4-0001-0000-0000-000000000001',
  'Alex', 'Mercer', '+48500123456', 'alex.mercer@email.com', '1994-08-14', 'CUSTOMER', 120
);

-- ───────────────────────────────────────────────
-- Barbers
-- ───────────────────────────────────────────────

-- Senior Hybrid Barber — HAIRCUT + BEARD, management access, can mentor
INSERT OR IGNORE INTO user (id, firstName, lastName, phone, dateOfBirth, role, seniorityLevel, specializationType, experienceYears, hireDate, description, managementAccess, canMentor, scissorsMastery, supportsLongHair, trimMastery, supportsHotTowel, beardCareKnowledge)
VALUES (
  'b1b2c3d4-0002-0000-0000-000000000002',
  'Marcus', 'Vance', '+48777888999', '1985-05-12', 'BARBER', 'SENIOR', 'HAIRCUT', 8, '2020-03-15',
  'Senior Master Barber specializing in modern texturized crops, beard styling, and luxury straight-razor shave rituals.',
  1, 1, 1, 1, 1, 1, '["Oils","Balms","Skin Conditioning"]'
);

-- Senior Hybrid Barber — HAIRCUT + BEARD, can mentor
INSERT OR IGNORE INTO user (id, firstName, lastName, phone, dateOfBirth, role, seniorityLevel, specializationType, experienceYears, hireDate, description, managementAccess, canMentor, scissorsMastery, supportsLongHair, trimMastery, supportsHotTowel, beardCareKnowledge)
VALUES (
  'c1b2c3d4-0003-0000-0000-000000000003',
  'Elena', 'Rostova', '+48777888998', '1990-09-18', 'BARBER', 'SENIOR', 'HAIRCUT', 6, '2022-05-10',
  'Precision stylist with a keen eye for classic scissor cuts, tapers, and meticulous beard shaping.',
  0, 1, 1, 1, 1, 1, '["Beard Grooming","Razor Detailing"]'
);

-- Junior Beard Specialist — constraint: one specialization only
INSERT OR IGNORE INTO user (id, firstName, lastName, phone, dateOfBirth, role, seniorityLevel, specializationType, experienceYears, hireDate, description, certifications, maxClientsPerDay, trimMastery, supportsHotTowel, beardCareKnowledge)
VALUES (
  'd1b2c3d4-0004-0000-0000-000000000004',
  'Leo', 'Sterling', '+48777888997', '2001-03-24', 'BARBER', 'JUNIOR', 'BEARD', 2, '2025-02-01',
  'Junior Beard Specialist with a precise hand for line-ups, tapers, and everyday beard upkeep.',
  '["Beard Sculpting Essentials","Clipper & Razor Fundamentals"]', 8, 1, 0, '["Beard Oils","Line-up Technique"]'
);

-- Junior Beard Specialist — constraint: one specialization only
INSERT OR IGNORE INTO user (id, firstName, lastName, phone, dateOfBirth, role, seniorityLevel, specializationType, experienceYears, hireDate, description, certifications, maxClientsPerDay, trimMastery, supportsHotTowel, beardCareKnowledge)
VALUES (
  'e1b2c3d4-0005-0000-0000-000000000005',
  'Viktor', 'Kael', '+48777888996', '1997-11-05', 'BARBER', 'JUNIOR', 'BEARD', 3, '2024-08-15',
  'Junior Beard Specialist focused on hot-towel treatments, line-ups, and traditional razor work.',
  '["Traditional Shave Rituals","Beard Artistry"]', 8, 1, 1, '["Hot Towel Compression","Shaving Soaps"]'
);

-- ───────────────────────────────────────────────
-- Services
-- ───────────────────────────────────────────────
-- Three services: two staffed by their own disjoint pair of barbers, one deliberately unstaffed.
INSERT OR IGNORE INTO service (id, name, price, type, duration, description, isAvailable, requiresStyling, complexityLevel) VALUES ('f1000000-0001-0000-0000-000000000001', 'Classic Scissor Cut',        35, 'HAIRCUT', 30, 'Traditional scissor haircut tailored to your head shape. Includes shampoo, neck shave, and premium styling product.', 1, 1, NULL);
INSERT OR IGNORE INTO service (id, name, price, type, duration, description, isAvailable, requiresStyling, complexityLevel) VALUES ('f1000000-0004-0000-0000-000000000004', 'Classic Beard Trim & Shape', 25, 'BEARD',   30, 'Beard trim with clippers, lined up with a trimmer. Finished with nourishing beard oil.',                             1, NULL, 'BEGINNER');
-- Deliberately has no barber_service row, so alternative flow 4A ("No barbers available for
-- this service.") is reachable in the GUI.
INSERT OR IGNORE INTO service (id, name, price, type, duration, description, isAvailable, requiresStyling, complexityLevel) VALUES ('f1000000-0007-0000-0000-000000000007', 'Hot Towel Royal Shave',      45, 'BEARD',   45, 'Traditional straight-razor shave with hot towel compression, pre-shave oil, and post-shave balm. Currently between specialists.', 1, NULL, 'EXPERT');

-- ───────────────────────────────────────────────
-- BarberService associations — each service has its OWN barbers
-- ───────────────────────────────────────────────
-- No barber is shared between services: every barber appears in exactly one service's list.
--
--   Classic Scissor Cut (HAIRCUT)         → Marcus + Elena   (the two SENIORs)
--   Classic Beard Trim & Shape (BEARD)    → Leo + Viktor     (the two JUNIORs)
--   Hot Towel Royal Shave (BEARD)         → nobody           (alternative flow 4A)
--
-- Marcus and Elena are the HAIRCUT pair; Leo and Viktor the BEARD pair.
INSERT OR IGNORE INTO barber_service (id, barberId, serviceId, seniority, specializationType) VALUES ('aa000000-0001-0000-0000-000000000001', 'b1b2c3d4-0002-0000-0000-000000000002', 'f1000000-0001-0000-0000-000000000001', 'SENIOR', 'HAIRCUT');
INSERT OR IGNORE INTO barber_service (id, barberId, serviceId, seniority, specializationType) VALUES ('aa000000-0002-0000-0000-000000000002', 'c1b2c3d4-0003-0000-0000-000000000003', 'f1000000-0001-0000-0000-000000000001', 'SENIOR', 'HAIRCUT');

INSERT OR IGNORE INTO barber_service (id, barberId, serviceId, seniority, specializationType) VALUES ('aa000000-0003-0000-0000-000000000003', 'd1b2c3d4-0004-0000-0000-000000000004', 'f1000000-0004-0000-0000-000000000004', 'JUNIOR', 'BEARD');
INSERT OR IGNORE INTO barber_service (id, barberId, serviceId, seniority, specializationType) VALUES ('aa000000-0004-0000-0000-000000000004', 'e1b2c3d4-0005-0000-0000-000000000005', 'f1000000-0004-0000-0000-000000000004', 'JUNIOR', 'BEARD');

-- ───────────────────────────────────────────────
-- Schedules — every barber works a different window
-- ───────────────────────────────────────────────
-- All four share MON–FRI so any date shows a comparable set of barbers, but the hours differ,
-- which is what makes the free slots visibly change when you switch barber on the same date:
--
--   Marcus 09:00–13:00  (mornings)
--   Elena  12:00–18:00  (midday into evening)
--   Leo    09:00–17:00  (full day)
--   Viktor 15:00–20:00  (late shift)
--
-- Marcus: MON–FRI mornings
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0001-0000-0000-000000000001', 'b1b2c3d4-0002-0000-0000-000000000002', 'MON', '09:00', '13:00', '2026-08-02', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0002-0000-0000-000000000002', 'b1b2c3d4-0002-0000-0000-000000000002', 'TUE', '09:00', '13:00', '2026-08-02', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0003-0000-0000-000000000003', 'b1b2c3d4-0002-0000-0000-000000000002', 'WED', '09:00', '13:00', '2026-08-02', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0004-0000-0000-000000000004', 'b1b2c3d4-0002-0000-0000-000000000002', 'THU', '09:00', '13:00', '2026-08-02', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0005-0000-0000-000000000005', 'b1b2c3d4-0002-0000-0000-000000000002', 'FRI', '09:00', '13:00', '2026-08-02', '2026-12-31', 1);
-- Elena: MON–FRI midday to evening
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0006-0000-0000-000000000006', 'c1b2c3d4-0003-0000-0000-000000000003', 'MON', '12:00', '18:00', '2026-08-02', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0007-0000-0000-000000000007', 'c1b2c3d4-0003-0000-0000-000000000003', 'TUE', '12:00', '18:00', '2026-08-02', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0008-0000-0000-000000000008', 'c1b2c3d4-0003-0000-0000-000000000003', 'WED', '12:00', '18:00', '2026-08-02', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0009-0000-0000-000000000009', 'c1b2c3d4-0003-0000-0000-000000000003', 'THU', '12:00', '18:00', '2026-08-02', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0010-0000-0000-000000000010', 'c1b2c3d4-0003-0000-0000-000000000003', 'FRI', '12:00', '18:00', '2026-08-02', '2026-12-31', 1);
-- Leo: MON–FRI full day
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0011-0000-0000-000000000011', 'd1b2c3d4-0004-0000-0000-000000000004', 'MON', '09:00', '17:00', '2026-08-02', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0012-0000-0000-000000000012', 'd1b2c3d4-0004-0000-0000-000000000004', 'TUE', '09:00', '17:00', '2026-08-02', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0013-0000-0000-000000000013', 'd1b2c3d4-0004-0000-0000-000000000004', 'WED', '09:00', '17:00', '2026-08-02', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0014-0000-0000-000000000014', 'd1b2c3d4-0004-0000-0000-000000000004', 'THU', '09:00', '17:00', '2026-08-02', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0015-0000-0000-000000000015', 'd1b2c3d4-0004-0000-0000-000000000004', 'FRI', '09:00', '17:00', '2026-08-02', '2026-12-31', 1);
-- Viktor: MON–FRI late shift
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0016-0000-0000-000000000016', 'e1b2c3d4-0005-0000-0000-000000000005', 'MON', '15:00', '20:00', '2026-08-02', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0017-0000-0000-000000000017', 'e1b2c3d4-0005-0000-0000-000000000005', 'TUE', '15:00', '20:00', '2026-08-02', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0018-0000-0000-000000000018', 'e1b2c3d4-0005-0000-0000-000000000005', 'WED', '15:00', '20:00', '2026-08-02', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0019-0000-0000-000000000019', 'e1b2c3d4-0005-0000-0000-000000000005', 'THU', '15:00', '20:00', '2026-08-02', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule (id, barberId, dayOfWeek, startTime, endTime, validFrom, validTo, isActive) VALUES ('bb000000-0020-0000-0000-000000000020', 'e1b2c3d4-0005-0000-0000-000000000005', 'FRI', '15:00', '20:00', '2026-08-02', '2026-12-31', 1);

-- ───────────────────────────────────────────────
-- Extra services
-- ───────────────────────────────────────────────
INSERT OR IGNORE INTO extra_service (id, type, name, price, description) VALUES ('cc000000-0001-0000-0000-000000000001', 'ALCOHOL',   '12-Year Single Malt Scotch',   15, 'A premium pour of Glenfiddich 12-Year, served neat or on the rocks.');
INSERT OR IGNORE INTO extra_service (id, type, name, price, description) VALUES ('cc000000-0002-0000-0000-000000000002', 'CIGAR',     'Premium Cohiba Cuban Cigar',   20, 'Hand-rolled Cuban cigar, perfect for enjoying on the patio post-service.');
INSERT OR IGNORE INTO extra_service (id, type, name, price, description) VALUES ('cc000000-0003-0000-0000-000000000003', 'CARD_GAME', 'Quick Hand Blackjack Setup',    5, 'Pull up a chair at our lounge table for a quick dealer-hosted blackjack game.');

-- ───────────────────────────────────────────────
-- Appointments — anchored on 2026-08-02 (Sunday)
-- ───────────────────────────────────────────────
-- Every row uses a barber who really offers that service, at a time inside that barber's hours,
-- and no two appointments overlap for the customer.
--
-- The two future ones on 2026-08-03 make the "slots differ per barber" demo concrete: Marcus is
-- booked 09:00–09:30 and Elena 12:00–12:30 that day, so each loses a different slot from the
-- Classic Scissor Cut list.
--
-- PAST (before 2026-08-02):
-- Mon 2026-07-27 — Marcus, Classic Scissor Cut (09:00–13:00 shift). Reviewed, with the AAAA
-- promo applied: 35 base + 15 scotch = 50, less 20% = 40.
INSERT OR IGNORE INTO appointment (id, customerId, barberId, serviceId, date, startTime, endTime, status, paymentStatus, paymentMethod, totalPrice, notes, cancellationReason) VALUES ('dd000000-0001-0000-0000-000000000001', 'a1b2c3d4-0001-0000-0000-000000000001', 'b1b2c3d4-0002-0000-0000-000000000002', 'f1000000-0001-0000-0000-000000000001', '2026-07-27', '10:00', '10:30', 'COMPLETED', 'PAID', 'CARD', 40, NULL, NULL);
-- Thu 2026-07-30 — Viktor, Classic Beard Trim (15:00–20:00 late shift). No review, so the
-- "Write Review" flow is demonstrable.
INSERT OR IGNORE INTO appointment (id, customerId, barberId, serviceId, date, startTime, endTime, status, paymentStatus, paymentMethod, totalPrice, notes, cancellationReason) VALUES ('dd000000-0002-0000-0000-000000000002', 'a1b2c3d4-0001-0000-0000-000000000001', 'e1b2c3d4-0005-0000-0000-000000000005', 'f1000000-0004-0000-0000-000000000004', '2026-07-30', '16:00', '16:30', 'COMPLETED', 'PAID', 'CASH', 25, NULL, NULL);
-- Wed 2026-07-29 — Leo, Classic Beard Trim (09:00–17:00 full day). Reviewed; different barber.
INSERT OR IGNORE INTO appointment (id, customerId, barberId, serviceId, date, startTime, endTime, status, paymentStatus, paymentMethod, totalPrice, notes, cancellationReason) VALUES ('dd000000-0007-0000-0000-000000000007', 'a1b2c3d4-0001-0000-0000-000000000001', 'd1b2c3d4-0004-0000-0000-000000000004', 'f1000000-0004-0000-0000-000000000004', '2026-07-29', '10:00', '10:30', 'COMPLETED', 'PAID', 'MOBILE', 25, NULL, NULL);
-- Tue 2026-07-28 — Elena, Classic Scissor Cut (12:00–18:00 shift). Cancelled.
INSERT OR IGNORE INTO appointment (id, customerId, barberId, serviceId, date, startTime, endTime, status, paymentStatus, paymentMethod, totalPrice, notes, cancellationReason) VALUES ('dd000000-0005-0000-0000-000000000005', 'a1b2c3d4-0001-0000-0000-000000000001', 'c1b2c3d4-0003-0000-0000-000000000003', 'f1000000-0001-0000-0000-000000000001', '2026-07-28', '13:00', '13:30', 'CANCELLED', 'UNPAID', 'CASH', 35, NULL, 'Schedule conflict on my end');

-- FUTURE (after 2026-08-02):
-- Mon 2026-08-03 — Marcus, Classic Scissor Cut (09:00–13:00 shift), with a cigar extra.
INSERT OR IGNORE INTO appointment (id, customerId, barberId, serviceId, date, startTime, endTime, status, paymentStatus, paymentMethod, totalPrice, notes, cancellationReason) VALUES ('dd000000-0003-0000-0000-000000000003', 'a1b2c3d4-0001-0000-0000-000000000001', 'b1b2c3d4-0002-0000-0000-000000000002', 'f1000000-0001-0000-0000-000000000001', '2026-08-03', '09:00', '09:30', 'CONFIRMED', 'UNPAID', 'MOBILE', 55, NULL, NULL);
-- Mon 2026-08-03 — Elena, Classic Scissor Cut (12:00–18:00 shift), same day, different barber.
INSERT OR IGNORE INTO appointment (id, customerId, barberId, serviceId, date, startTime, endTime, status, paymentStatus, paymentMethod, totalPrice, notes, cancellationReason) VALUES ('dd000000-0004-0000-0000-000000000004', 'a1b2c3d4-0001-0000-0000-000000000001', 'c1b2c3d4-0003-0000-0000-000000000003', 'f1000000-0001-0000-0000-000000000001', '2026-08-03', '12:00', '12:30', 'CONFIRMED', 'UNPAID', 'CASH', 35, NULL, NULL);
-- Wed 2026-08-05 — Viktor, Classic Beard Trim (15:00–20:00 late shift).
INSERT OR IGNORE INTO appointment (id, customerId, barberId, serviceId, date, startTime, endTime, status, paymentStatus, paymentMethod, totalPrice, notes, cancellationReason) VALUES ('dd000000-0008-0000-0000-000000000008', 'a1b2c3d4-0001-0000-0000-000000000001', 'e1b2c3d4-0005-0000-0000-000000000005', 'f1000000-0004-0000-0000-000000000004', '2026-08-05', '15:30', '16:00', 'NEW', 'UNPAID', 'CASH', 25, NULL, NULL);

-- Extra services linked to appointments
INSERT OR IGNORE INTO appointment_extra (appointmentId, extraServiceId) VALUES ('dd000000-0001-0000-0000-000000000001', 'cc000000-0001-0000-0000-000000000001');
INSERT OR IGNORE INTO appointment_extra (appointmentId, extraServiceId) VALUES ('dd000000-0003-0000-0000-000000000003', 'cc000000-0002-0000-0000-000000000002');

-- Reviews — two of the three completed appointments, different barbers
INSERT OR IGNORE INTO review (id, appointmentId, customerId, rating, comment, date) VALUES ('ee000000-0001-0000-0000-000000000001', 'dd000000-0001-0000-0000-000000000001', 'a1b2c3d4-0001-0000-0000-000000000001', 5, 'Marcus nailed the scissor cut and the scotch was a great touch. The promo made it a steal.', '2026-07-27');
INSERT OR IGNORE INTO review (id, appointmentId, customerId, rating, comment, date) VALUES ('ee000000-0002-0000-0000-000000000002', 'dd000000-0007-0000-0000-000000000007', 'a1b2c3d4-0001-0000-0000-000000000001', 4, 'Leo was quick and precise with the beard line-up. Clean finish, would book again.', '2026-07-29');

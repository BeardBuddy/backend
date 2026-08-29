-- Seed data, copied verbatim from the Next.js app's infrastructure/db/startup.sql so the frontend
-- sees an identical dataset — same ids, same values. INSERT OR IGNORE keeps it idempotent across
-- restarts, matching the old getDb() behaviour of re-running the whole script.
--
-- The ONE intentional deviation is the barber_service block: serviceId is now unique, so the
-- Marcus/Elena overlap is gone. See the comment above that block.

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

-- Junior Haircut Specialist — constraint: one specialization only
INSERT OR IGNORE INTO user (id, firstName, lastName, phone, dateOfBirth, role, seniorityLevel, specializationType, experienceYears, hireDate, description, certifications, maxClientsPerDay, scissorsMastery, supportsLongHair)
VALUES (
  'd1b2c3d4-0004-0000-0000-000000000004',
  'Leo', 'Sterling', '+48777888997', '2001-03-24', 'BARBER', 'JUNIOR', 'HAIRCUT', 2, '2025-02-01',
  'Junior Haircut Specialist with outstanding skill in high-contrast fades and modern hair styling.',
  '["Advanced Fade Masterclass","Clipper & Scissor Fundamentals"]', 8, 0, 0
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
INSERT OR IGNORE INTO service VALUES ('f1000000-0001-0000-0000-000000000001', 'Classic Scissor Cut',        35, 'HAIRCUT', 30, 'Traditional scissor haircut tailored to your head shape. Includes shampoo, neck shave, and premium styling product.', 1, 1, NULL, NULL);
INSERT OR IGNORE INTO service VALUES ('f1000000-0002-0000-0000-000000000002', 'High-Skin Fade',             40, 'HAIRCUT', 45, 'Precision clipper fade down to the skin. Completed with razor neck cleanup, wash, and style.',                       1, 1, NULL, NULL);
INSERT OR IGNORE INTO service VALUES ('f1000000-0003-0000-0000-000000000003', 'Buzz Cut & Styling',         25, 'HAIRCUT', 20, 'Simple, clean single-guard buzz cut. Includes wash and scalp conditioning.',                                         1, 0, NULL, NULL);
INSERT OR IGNORE INTO service VALUES ('f1000000-0004-0000-0000-000000000004', 'Classic Beard Trim & Shape', 25, 'BEARD',   30, 'Beard trim with clippers, lined up with a trimmer. Finished with nourishing beard oil.',                             1, NULL, 'BEGINNER', NULL);
INSERT OR IGNORE INTO service VALUES ('f1000000-0005-0000-0000-000000000005', 'Hot Towel Royal Shave',      35, 'BEARD',   45, 'Traditional razor beard shave or shape-up with hot towel compression, pre-shave cream, and post-shave balm.',         1, NULL, 'EXPERT',   NULL);
INSERT OR IGNORE INTO service VALUES ('f1000000-0006-0000-0000-000000000006', 'Signature Cut & Beard Combo',55, 'HYBRID',  60, 'Our premium hybrid package consisting of a Classic Scissor Cut combined with the Classic Beard Trim.',               1, NULL, NULL, '["f1000000-0001-0000-0000-000000000001","f1000000-0004-0000-0000-000000000004"]');

-- ───────────────────────────────────────────────
-- BarberService associations — one barber per service
-- ───────────────────────────────────────────────
-- Every serviceId appears exactly once (enforced by the UNIQUE constraint in schema.sql).
-- Both seniors keep one HAIRCUT and one BEARD row, so they still read as hybrid-qualified
-- (BarberService.isExpert()); each junior keeps a single specialization.
--
-- Marcus  → Classic Scissor Cut (HAIRCUT) + Hot Towel Royal Shave (BEARD)
-- Elena   → High-Skin Fade (HAIRCUT) + Signature Cut & Beard Combo (tagged BEARD)
-- Leo     → Buzz Cut & Styling (HAIRCUT)
-- Viktor  → Classic Beard Trim & Shape (BEARD)
INSERT OR IGNORE INTO barber_service VALUES ('aa000000-0001-0000-0000-000000000001', 'b1b2c3d4-0002-0000-0000-000000000002', 'f1000000-0001-0000-0000-000000000001', 'SENIOR', 'HAIRCUT');
INSERT OR IGNORE INTO barber_service VALUES ('aa000000-0004-0000-0000-000000000004', 'b1b2c3d4-0002-0000-0000-000000000002', 'f1000000-0005-0000-0000-000000000005', 'SENIOR', 'BEARD');
INSERT OR IGNORE INTO barber_service VALUES ('aa000000-0007-0000-0000-000000000007', 'c1b2c3d4-0003-0000-0000-000000000003', 'f1000000-0002-0000-0000-000000000002', 'SENIOR', 'HAIRCUT');
INSERT OR IGNORE INTO barber_service VALUES ('aa000000-0010-0000-0000-000000000010', 'c1b2c3d4-0003-0000-0000-000000000003', 'f1000000-0006-0000-0000-000000000006', 'SENIOR', 'BEARD');
INSERT OR IGNORE INTO barber_service VALUES ('aa000000-0012-0000-0000-000000000012', 'd1b2c3d4-0004-0000-0000-000000000004', 'f1000000-0003-0000-0000-000000000003', 'JUNIOR', 'HAIRCUT');
INSERT OR IGNORE INTO barber_service VALUES ('aa000000-0013-0000-0000-000000000013', 'e1b2c3d4-0005-0000-0000-000000000005', 'f1000000-0004-0000-0000-000000000004', 'JUNIOR', 'BEARD');

-- ───────────────────────────────────────────────
-- Schedules
-- ───────────────────────────────────────────────
-- Marcus: Mon–Sat
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0001-0000-0000-000000000001', 'b1b2c3d4-0002-0000-0000-000000000002', 'MON', '09:00', '18:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0002-0000-0000-000000000002', 'b1b2c3d4-0002-0000-0000-000000000002', 'TUE', '09:00', '18:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0003-0000-0000-000000000003', 'b1b2c3d4-0002-0000-0000-000000000002', 'WED', '09:00', '18:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0004-0000-0000-000000000004', 'b1b2c3d4-0002-0000-0000-000000000002', 'THU', '09:00', '18:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0005-0000-0000-000000000005', 'b1b2c3d4-0002-0000-0000-000000000002', 'FRI', '09:00', '20:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0006-0000-0000-000000000006', 'b1b2c3d4-0002-0000-0000-000000000002', 'SAT', '09:00', '16:00', '2026-01-01', '2026-12-31', 1);
-- Elena: Mon–Sat
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0007-0000-0000-000000000007', 'c1b2c3d4-0003-0000-0000-000000000003', 'MON', '10:00', '19:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0008-0000-0000-000000000008', 'c1b2c3d4-0003-0000-0000-000000000003', 'TUE', '10:00', '19:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0009-0000-0000-000000000009', 'c1b2c3d4-0003-0000-0000-000000000003', 'WED', '10:00', '19:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0010-0000-0000-000000000010', 'c1b2c3d4-0003-0000-0000-000000000003', 'THU', '10:00', '19:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0011-0000-0000-000000000011', 'c1b2c3d4-0003-0000-0000-000000000003', 'FRI', '10:00', '21:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0012-0000-0000-000000000012', 'c1b2c3d4-0003-0000-0000-000000000003', 'SAT', '09:00', '15:00', '2026-01-01', '2026-12-31', 1);
-- Leo: Mon–Fri
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0013-0000-0000-000000000013', 'd1b2c3d4-0004-0000-0000-000000000004', 'MON', '09:00', '17:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0014-0000-0000-000000000014', 'd1b2c3d4-0004-0000-0000-000000000004', 'TUE', '09:00', '17:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0015-0000-0000-000000000015', 'd1b2c3d4-0004-0000-0000-000000000004', 'WED', '09:00', '17:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0016-0000-0000-000000000016', 'd1b2c3d4-0004-0000-0000-000000000004', 'THU', '09:00', '17:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0017-0000-0000-000000000017', 'd1b2c3d4-0004-0000-0000-000000000004', 'FRI', '09:00', '17:00', '2026-01-01', '2026-12-31', 1);
-- Viktor: Tue–Sat
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0018-0000-0000-000000000018', 'e1b2c3d4-0005-0000-0000-000000000005', 'TUE', '11:00', '19:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0019-0000-0000-000000000019', 'e1b2c3d4-0005-0000-0000-000000000005', 'WED', '11:00', '19:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0020-0000-0000-000000000020', 'e1b2c3d4-0005-0000-0000-000000000005', 'THU', '11:00', '19:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0021-0000-0000-000000000021', 'e1b2c3d4-0005-0000-0000-000000000005', 'FRI', '11:00', '19:00', '2026-01-01', '2026-12-31', 1);
INSERT OR IGNORE INTO schedule VALUES ('bb000000-0022-0000-0000-000000000022', 'e1b2c3d4-0005-0000-0000-000000000005', 'SAT', '10:00', '16:00', '2026-01-01', '2026-12-31', 1);

-- ───────────────────────────────────────────────
-- Extra services
-- ───────────────────────────────────────────────
INSERT OR IGNORE INTO extra_service VALUES ('cc000000-0001-0000-0000-000000000001', 'ALCOHOL',   '12-Year Single Malt Scotch',   15, 'A premium pour of Glenfiddich 12-Year, served neat or on the rocks.');
INSERT OR IGNORE INTO extra_service VALUES ('cc000000-0002-0000-0000-000000000002', 'CIGAR',     'Premium Cohiba Cuban Cigar',   20, 'Hand-rolled Cuban cigar, perfect for enjoying on the patio post-service.');
INSERT OR IGNORE INTO extra_service VALUES ('cc000000-0003-0000-0000-000000000003', 'CARD_GAME', 'Quick Hand Blackjack Setup',    5, 'Pull up a chair at our lounge table for a quick dealer-hosted blackjack game.');

-- ───────────────────────────────────────────────
-- Appointments
-- ───────────────────────────────────────────────
-- Past (before June 11 defense):
INSERT OR IGNORE INTO appointment VALUES ('dd000000-0001-0000-0000-000000000001', 'a1b2c3d4-0001-0000-0000-000000000001', 'b1b2c3d4-0002-0000-0000-000000000002', 'f1000000-0006-0000-0000-000000000006', '2026-06-05', '14:00', '15:00', 'COMPLETED', 'PAID',   'CARD',   70, NULL, NULL);
INSERT OR IGNORE INTO appointment VALUES ('dd000000-0002-0000-0000-000000000002', 'a1b2c3d4-0001-0000-0000-000000000001', 'd1b2c3d4-0004-0000-0000-000000000004', 'f1000000-0001-0000-0000-000000000001', '2026-06-03', '16:30', '17:00', 'COMPLETED', 'PAID',   'CASH',   35, NULL, NULL);
INSERT OR IGNORE INTO appointment VALUES ('dd000000-0005-0000-0000-000000000005', 'a1b2c3d4-0001-0000-0000-000000000001', 'e1b2c3d4-0005-0000-0000-000000000005', 'f1000000-0005-0000-0000-000000000005', '2026-06-04', '13:00', '13:45', 'CANCELLED', 'UNPAID', 'CASH',   35, NULL, 'Schedule conflict on my end');
INSERT OR IGNORE INTO appointment VALUES ('dd000000-0006-0000-0000-000000000006', 'a1b2c3d4-0001-0000-0000-000000000001', 'c1b2c3d4-0003-0000-0000-000000000003', 'f1000000-0004-0000-0000-000000000004', '2026-06-06', '11:00', '11:30', 'COMPLETED', 'PAID',   'CASH',   25, NULL, NULL);
-- Future (after June 11 defense):
INSERT OR IGNORE INTO appointment VALUES ('dd000000-0003-0000-0000-000000000003', 'a1b2c3d4-0001-0000-0000-000000000001', 'c1b2c3d4-0003-0000-0000-000000000003', 'f1000000-0002-0000-0000-000000000002', '2026-06-16', '10:30', '11:15', 'CONFIRMED', 'UNPAID', 'MOBILE', 60, NULL, NULL);
INSERT OR IGNORE INTO appointment VALUES ('dd000000-0004-0000-0000-000000000004', 'a1b2c3d4-0001-0000-0000-000000000001', 'b1b2c3d4-0002-0000-0000-000000000002', 'f1000000-0004-0000-0000-000000000004', '2026-06-18', '11:00', '11:30', 'CONFIRMED', 'UNPAID', 'CASH',   25, NULL, NULL);

-- Extra services linked to appointments
INSERT OR IGNORE INTO appointment_extra VALUES ('dd000000-0001-0000-0000-000000000001', 'cc000000-0001-0000-0000-000000000001');
INSERT OR IGNORE INTO appointment_extra VALUES ('dd000000-0003-0000-0000-000000000003', 'cc000000-0002-0000-0000-000000000002');

-- Reviews — only for completed appointments, not all
INSERT OR IGNORE INTO review VALUES ('ee000000-0001-0000-0000-000000000001', 'dd000000-0001-0000-0000-000000000001', 'a1b2c3d4-0001-0000-0000-000000000001', 5, 'Marcus delivered an exceptional hybrid service. The beard shaping was immaculate.', '2026-06-05');
INSERT OR IGNORE INTO review VALUES ('ee000000-0002-0000-0000-000000000002', 'dd000000-0002-0000-0000-000000000002', 'a1b2c3d4-0001-0000-0000-000000000001', 5, 'Leo did an exceptional job with the scissors, very precise fade. Highly recommended!', '2026-06-03');

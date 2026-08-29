# BeardBuddy — Java backend

Java 17 + Spring Boot 3 + Hibernate/Spring Data JPA replacement for the Next.js API routes in
`../masproject/app/api/**`. Same paths, same verbs, same JSON in and out — the frontend does not
know the difference beyond which host it calls.

## Run

```bash
mvn spring-boot:run          # http://localhost:8080
```

The SQLite file `beardbuddy.db` is created in the working directory on first start; schema and
seed data are (re-)applied on every start and are idempotent. Point it elsewhere with
`BEARDBUDDY_DB=/path/to/file.db`.

## Point the frontend at it

```bash
cd ../masproject
echo 'NEXT_PUBLIC_API_BASE_URL=http://localhost:8080' > .env.local
npm run dev
```

`lib/apiBase.ts` prefixes every `fetch()` with that value. Unset, it stays empty and the app keeps
using the Next.js routes, so both backends remain runnable side by side.

## Endpoints

Migrated as-is from the Next.js routes:

| Method | Path | Body | Response |
| --- | --- | --- | --- |
| GET | `/api/data` | — | the whole dataset (`users`, `services`, `barberServices`, `schedules`, `appointments`, `extraServices`, `reviews`, `appointmentExtras`) |
| POST | `/api/appointments` | appointment + `extraServiceIds[]` | `{"ok": true}` |
| PATCH | `/api/appointments/{id}/cancel` | `{"cancellationReason"?}` | `{"ok": true}` |
| PATCH | `/api/appointments/{id}/status` | `{"status"}` | `{"ok": true}` |
| POST | `/api/reviews` | review | `{"ok": true}` |

Added for the association requirement (MAS 4.2.4):

| Method | Path | Response |
| --- | --- | --- |
| GET | `/api/barbers/{id}/services` | services of that barber; 404 if unknown or not a barber |
| GET | `/api/services` | every service with its `barbers` nested |

Failures answer `500` with the same message strings the old routes used
(`Failed to load data`, `Failed to create appointment`, `Failed to cancel appointment`,
`Failed to update status`, `Failed to save review`).

## Behaviour that is deliberately preserved

The old backend was intentionally thin, and this one matches it:

- **No server-side business validation.** Double-booking checks, schedule checks, status-transition
  rules, price computation and review eligibility all run in the browser
  (`business-objects/*.ts`, `use-cases/*.ts`) before the request is sent. A direct API call can
  still create a double-booked appointment — that was true before and stays true.
- **The only server-side guards are schema constraints**: the `CHECK` enumerations,
  `rating BETWEEN 1 AND 5`, the `UNIQUE` on `review.appointmentId`, and the foreign keys
  (`PRAGMA foreign_keys = ON` per connection).
- **Client-generated ids.** Appointments arrive as `appt-<epoch>` and reviews as
  `rev-<appointmentId>`; there is no `@GeneratedValue`. `EntityManager.persist` is used rather
  than `save()` so a duplicate id fails like the original `INSERT` instead of silently updating.
- **Wire formats.** Dates stay `"YYYY-MM-DD"` and times `"HH:mm"` plain strings;
  `certifications` / `beardCareKnowledge` / `subServiceIds` are JSON arrays in the response even
  though they are JSON-encoded strings in the column; `schedule.isActive` stays the raw 0/1
  integer the old route passed through; whole-numbered `REAL` prices serialise as `35`, not `35.0`
  (`CompactDoubleSerializer`), because the UI renders them verbatim.
- **`POST /api/appointments` is transactional** — appointment plus extra links commit or roll back
  together, like the route's manual `BEGIN`/`COMMIT`/`ROLLBACK`.

## Association navigation (MAS 4.2.4)

`User` stays one entity with a `role` discriminator; the association that matters is
`User`(BARBER) ↔ `Service` through the `BarberService` join entity, mapped in both directions:

```java
barber.getServices()   // User.barberServices -> BarberService.service
service.getBarbers()   // Service.barberServices -> BarberService.barber
```

Both new endpoints reach related objects only that way. Repositories are bare `JpaRepository`
declarations — no `@Query`, no JPQL/HQL/SQL, no derived finders such as `findByBarberId`, no
Criteria or Specification filtering. The only repository calls are `findById` (a primary-key
lookup) and `findAll` (no predicate).

`barber_service.serviceId` is `UNIQUE`, so a service belongs to exactly one barber, and the seed
data has no duplicate service ids. `Service.getBarbers()` therefore resolves to at most one barber
today — the association is still declared collection-typed on purpose, which is what the
requirement's "target multiplicity: many" refers to.

## Seed data

`src/main/resources/data.sql` reproduces `../masproject/infrastructure/db/startup.sql` verbatim —
same ids, same values — with one intentional change: the `barber_service` rows were reallocated so
no service is offered by two barbers.

| Service | Barber |
| --- | --- |
| Classic Scissor Cut (HAIRCUT) | Marcus Vance — `HAIRCUT` |
| Hot Towel Royal Shave (BEARD) | Marcus Vance — `BEARD` |
| High-Skin Fade (HAIRCUT) | Elena Rostova — `HAIRCUT` |
| Signature Cut & Beard Combo (HYBRID) | Elena Rostova — row tagged `BEARD` |
| Buzz Cut & Styling (HAIRCUT) | Leo Sterling — `HAIRCUT` |
| Classic Beard Trim & Shape (BEARD) | Viktor Kael — `BEARD` |

Both seniors keep one `HAIRCUT` and one `BEARD` row, so they still read as hybrid-qualified;
each junior keeps a single specialization. The existing seeded appointments still reference the
old pairings (e.g. Leo with a Classic Scissor Cut) — that is historical data and violates no
constraint, but it does mean the README's old manual-QA walkthroughs in `../masproject` no longer
resolve the same way.

`../masproject/infrastructure/db/startup.sql` is left untouched, so the Next.js app keeps its
original dataset.

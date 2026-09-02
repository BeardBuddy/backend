# BeardBuddy — Java backend

Java 17 + Spring Boot 3 + Hibernate/Spring Data JPA. This is the whole application model: entities,
associations, constraints and all business rules. The frontend is a view layer — it receives flat
DTOs and renders them.

## Run

```bash
mvn spring-boot:run          # http://localhost:8080
```

Schema and seed are recreated on every start, so a run always ends up with exactly the dataset
below regardless of what the previous run left behind. No cleanup step; deleting `beardbuddy.db` is
optional. Point it elsewhere with `BEARDBUDDY_DB=/path/to/file.db`.

## Endpoints

Read:

| Method | Path | Returns |
| --- | --- | --- |
| GET | `/api/services` | available services, each with its `barbers[]` |
| GET | `/api/services/{id}/barbers` | barbers offering that service |
| GET | `/api/barbers/{id}/services` | services of that barber |
| GET | `/api/barbers/{id}/slots?serviceId=&date=&customerId=` | bookable start times, sized to the service duration; excludes slots where the barber **or** the customer is busy |
| GET | `/api/extra-services` | extras catalogue |
| GET | `/api/customers/current` | the single seeded customer |
| GET | `/api/customers/{id}/appointments` | `{upcoming, completed, cancelled}`, pre-split |
| GET | `/api/appointments/{id}` | one appointment |

Write:

| Method | Path | Body |
| --- | --- | --- |
| POST | `/api/appointments` | `{customerId, barberId, serviceId, date, startTime, extraServiceIds?, promoCode?, notes?}` |
| PATCH | `/api/appointments/{id}/cancel` | `{customerId, cancellationReason?}` |
| PATCH | `/api/appointments/{id}/complete` | `{customerId}` |
| POST | `/api/appointments/{id}/review` | `{customerId, rating, comment?}` |
| POST | `/api/promo-codes/apply` | `{code, total}` |

A broken rule returns `400 {"error": "<message>"}`; a missing entity returns `404`. The frontend
shows the message as-is.

## Business rules (all server-side)

Enforced in the domain entities, not in controllers:

- A barber must actually offer the requested service, have a schedule, and be open at that time
- No overlapping booking for the same barber, **and none for the customer** — a customer cannot sit in two chairs at once (`startA < endB && endA > startB`)
- `endTime` is derived from the service duration; a HYBRID sums its sub-services
- `totalPrice` is derived: service price + extras, then any promo discount
- Cancel only from a non-terminal status; complete only from `NEW`/`CONFIRMED`/`IN_PROGRESS`
- Review only on a `COMPLETED` appointment, once, rating 1–5
- Customers have no schedule; barbers cannot write reviews
- Promo codes validated against status and expiry

Ids are still generated server-side as `appt-<epoch>` / `rev-<appointmentId>`.

## Model

Real Hibernate associations throughout:

- `User` ↔ `Service` through the `BarberService` join entity, both directions mapped
- `User` → `Appointment` twice (`customer` / `barber`), `User` → `Schedule`, `User` → `Review`
- `Appointment` → `Service`, `Appointment` → `Review` (1:0..1)
- `Appointment` ↔ `ExtraService` as a `@ManyToMany` over the `appointment_extra` join table
- **Composition** `Service ◆ Service` — sub-services via the `service_sub_service` join table
  (previously a JSON string blob)

`User` stays one entity with a `role` discriminator, and `Service` one entity with a `type` enum,
both by earlier decision.

`barber_service` is a true many-to-many: a service may be offered by several barbers and a barber
offers several services. `UNIQUE (barberId, serviceId)` only stops the same pair being registered
twice.

## Seed data

Anchored on **2026-08-02**. Five services, most shared by several barbers:

| Service | Type | Price | Duration | Barbers |
| --- | --- | --- | --- | --- |
| Classic Scissor Cut | HAIRCUT | 35 | 30 | Marcus, Elena, Leo |
| High-Skin Fade | HAIRCUT | 40 | 45 | Elena, Leo |
| Classic Beard Trim & Shape | BEARD | 25 | 30 | Marcus, Viktor |
| Signature Cut & Beard Combo | HYBRID | 55 | 60 (derived) | Elena |
| Hot Towel Royal Shave | BEARD | 45 | 45 | *none* — makes flow 4A reachable |

Every barber works MON–FRI but on a **different shift**, which is what makes the free slots change
when you pick a different barber for the same service and date:

| Barber | Shift |
| --- | --- |
| Marcus Vance (SENIOR) | 09:00–13:00 |
| Elena Rostova (SENIOR) | 12:00–18:00 |
| Leo Sterling (JUNIOR) | 09:00–17:00 |
| Viktor Kael (JUNIOR) | 15:00–20:00 |

Appointments (each with a barber who offers that service, at a time inside that barber's shift):

| Date | Barber | Service | Status | Note |
| --- | --- | --- | --- | --- |
| 2026-07-27 | Marcus | Classic Scissor Cut | COMPLETED | reviewed ★5; **AAAA promo applied** — 35 + 15 scotch = 50, −20% = **40** |
| 2026-07-29 | Leo | High-Skin Fade | COMPLETED | reviewed ★4 (different barber + service) |
| 2026-07-30 | Viktor | Classic Beard Trim & Shape | COMPLETED | **no review** — reviewable in the UI |
| 2026-07-28 | Elena | High-Skin Fade | CANCELLED | |
| 2026-08-03 | Marcus | Classic Beard Trim & Shape | CONFIRMED | + cigar extra; blocks his 09:00 |
| 2026-08-03 | Elena | Classic Scissor Cut | CONFIRMED | same day, blocks her 12:00 |
| 2026-08-05 | Leo | Classic Scissor Cut | NEW | |

The two bookings on 2026-08-03 are deliberate: pick *Classic Scissor Cut* on that Monday and switch
between barbers to see three different slot sets — Marcus 09:30–12:30 (09:00 taken), Elena
12:30–17:30 (12:00 taken), Leo the full 09:00–16:30.

Promo codes are **fixtures in `PromoCode.java`**, not a database table: `AAAA` 20% active,
`BBBB` limit reached, `CCCC` expired. Anything else is rejected with "Code cannot be applied".

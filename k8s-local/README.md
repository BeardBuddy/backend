# Running the stack on local Kubernetes

Verifies the manifests in `../k8s` on Docker Desktop's built-in cluster, without
a registry. The base manifests pull from GHCR; this overlay swaps the three
first-party images for ones built on this machine.

## One-time setup

Docker Desktop → Settings → **Kubernetes** → *Enable Kubernetes* → Apply & restart.
It takes a few minutes on first start.

```sh
kubectl config use-context docker-desktop
kubectl get nodes            # expect one Ready node
```

## 1. Build the images locally

Tags must match the ones in `kustomization.yaml`. Run from the backend repo root:

```sh
docker build -f Dockerfile.api.local    -t beardbuddy/api:local    .
docker build -f Dockerfile.worker.local -t beardbuddy/worker:local .
docker build -f Dockerfile.local -t beardbuddy/frontend:local ../masproject
```

The `.local` Dockerfiles build from source, so no prior `mvn package` or
`npm run build` is needed. Docker Desktop's Kubernetes shares this image store,
which is why `imagePullPolicy: Never` resolves them.

## 2. Deploy

```sh
kubectl apply -k k8s-local
kubectl -n beardbuddy get pods -w
```

Expect 6 deployments: zookeeper, kafka, postgres, backend, worker (2 replicas),
frontend. First start pulls postgres/kafka/zookeeper from Docker Hub.

The worker pods sit in `Init:0/1` until the API has seeded the schema — that is
the `wait-for-api` initContainer doing its job, not a failure.

## 3. Verify

Everything except `/api/auth/**` requires a session, so log in first and keep the
cookie. The token comes back as an httpOnly cookie, not in the body.

```sh
curl -s -c /tmp/bb.jar -X POST localhost:30080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"alex","password":"password"}'

curl -s -b /tmp/bb.jar localhost:30080/api/services
curl -s -b /tmp/bb.jar \
  localhost:30080/api/services/f1000000-0001-0000-0000-000000000001/barbers
```

Expect 3 services, and Marcus + Elena on the first one. In the browser:
`http://localhost:30300`, log in as **alex / password**.

Kafka pipeline end to end. The endpoint returns **202 Accepted** as soon as the
events are published — the worker materialises schedules afterwards, so the row
count climbs for a few seconds after the curl returns:

```sh
curl -s -b /tmp/bb.jar -X POST localhost:30080/api/barbers/load \
  -H 'Content-Type: application/json' \
  -d '{"count":5000,"startTime":"09:00","endTime":"17:00",
       "weekDays":["MON","TUE","WED","THU","FRI"],"validFrom":"2026-01-01"}'

# re-run a few times; it settles around 25,000
kubectl -n beardbuddy exec deploy/postgres -- \
  psql -U beardbuddy -d beardbuddy -c 'SELECT count(*) FROM schedule;'
```

Both worker replicas share the `barber.created` partitions, so the load splits
between them. Watch it happen:

```sh
kubectl -n beardbuddy logs -l app=worker --tail=20 -f --max-log-requests=2
```

## Teardown

```sh
kubectl delete -k k8s-local                          # keeps the volume
kubectl -n beardbuddy delete pvc postgres-pvc        # wipes the database too
```

## When something is wrong

| Symptom | Cause |
|---|---|
| `ErrImageNeverPull` | Image not built, or tagged differently than `kustomization.yaml` expects. Re-run step 1. |
| Worker stuck in `Init:0/1` | The API never became ready. `kubectl -n beardbuddy logs deploy/backend`. |
| `CrashLoopBackOff` on backend | Usually postgres not up yet; it retries. If it persists, check the datasource env in `../k8s/config.yaml`. |
| Frontend loads but calls fail | `API_BASE_URL` must be reachable from the *browser*, so `localhost:30080` — not the in-cluster name `backend:8080`. |

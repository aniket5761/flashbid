# k6 Load Tests

This folder contains load-testing assets for FlashBid.

## Structure

- `scripts/browse.js`
  Login-aware browse load test.
- `scripts/bidrace.js`
  Same-auction concurrent bidding test.
- `scripts/register-users.sh`
  Bulk user registration helper for test accounts.
- `data/users.json`
  Test user credentials consumed by the k6 scripts.
- `results/`
  Place saved test outputs here.

## Run From Repo Root

Browse test:

```bash
docker run --rm -i \
  -v "$PWD:/work" \
  -w /work/k6 \
  -e BASE_URL="https://flashbid-production-e7f4.up.railway.app" \
  -e PRODUCT_ID="7" \
  grafana/k6 run scripts/browse.js
```

Bid race test:

```bash
docker run --rm -i \
  -v "$PWD:/work" \
  -w /work/k6 \
  -e BASE_URL="https://flashbid-production-e7f4.up.railway.app" \
  -e PRODUCT_ID="7" \
  -e START_BID="1000" \
  -e STEP="10" \
  grafana/k6 run scripts/bidrace.js
```

Register test users:

```bash
bash k6/scripts/register-users.sh https://flashbid-production-e7f4.up.railway.app k6/data/users.json
```

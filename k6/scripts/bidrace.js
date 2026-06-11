import http from "k6/http";
import { check, sleep } from "k6";
import { Rate } from "k6/metrics";

export const options = {
  setupTimeout: "2m",
  stages: [
    { duration: "1m", target: 10 },
    { duration: "2m", target: 10 },
    { duration: "1m", target: 25 },
    { duration: "2m", target: 25 },
    { duration: "1m", target: 50 },
    { duration: "2m", target: 50 },
    { duration: "1m", target: 75 },
    { duration: "2m", target: 75 },
    { duration: "1m", target: 100 },
    { duration: "3m", target: 100 },
    { duration: "30s", target: 0 }
  ],
  thresholds: {
    http_req_failed: ["rate<0.02"],
    "http_req_duration{name:place_bid}": ["p(95)<800"]
  }
};

http.setResponseCallback(http.expectedStatuses(200, 400, 403, 409));

const BASE_URL = __ENV.BASE_URL;
const PRODUCT_ID = Number(__ENV.PRODUCT_ID);
const START_BID = Number(__ENV.START_BID || "200");
const STEP = Number(__ENV.STEP || "110");
const USER_LIMIT = Number(__ENV.USER_LIMIT || "100");
const USERS = JSON.parse(open("../data/users.json")).slice(0, USER_LIMIT);

const bidAcceptedRate = new Rate("bid_accepted_rate");
const bidBusinessRejectRate = new Rate("bid_business_reject_rate");
const bidUnexpectedFailRate = new Rate("bid_unexpected_fail_rate");

export function setup() {
  const tokens = {};

  for (const user of USERS) {
    const res = http.post(
      `${BASE_URL}/api/auth/login`,
      JSON.stringify({
        username: user.username,
        password: user.password
      }),
      {
        headers: { "Content-Type": "application/json" },
        tags: { name: "login" }
      }
    );

    check(res, {
      "login ok in setup": (r) => r.status === 200
    });

    const token = res.json("token");
    tokens[user.username] = token;
  }

  return { tokens };
}

export default function (data) {
  const user = USERS[(__VU - 1) % USERS.length];
  const token = data.tokens[user.username];

  // Use a globally increasing bid sequence so each attempt is comfortably above
  // the prior one, while still allowing legitimate race-lost responses.
  const sequence = (__ITER * USERS.length) + ((__VU - 1) % USERS.length);
  const amount = START_BID + (sequence * STEP);

  const res = http.post(
    `${BASE_URL}/api/bids`,
    JSON.stringify({
      productId: PRODUCT_ID,
      amount: amount
    }),
    {
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      tags: { name: "place_bid" }
    }
  );

  bidAcceptedRate.add(res.status === 200);
  bidBusinessRejectRate.add([400, 403, 409].includes(res.status));
  bidUnexpectedFailRate.add(![200, 400, 403, 409].includes(res.status));

  check(res, {
    "bid accepted or rejected cleanly": (r) =>
      [200, 400, 403, 409].includes(r.status)
  });

  sleep(1);
}

import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
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

const BASE_URL = __ENV.BASE_URL;
const PRODUCT_ID = Number(__ENV.PRODUCT_ID);
const START_BID = Number(__ENV.START_BID || "1000");
const STEP = Number(__ENV.STEP || "10");
const USERS = JSON.parse(open("../data/users.json"));

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

  const amount = START_BID + (__ITER * STEP) + __VU;

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

  check(res, {
    "bid accepted or rejected cleanly": (r) =>
      [200, 400, 403, 409].includes(r.status)
  });

  sleep(1);
}

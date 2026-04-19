import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  stages: [
    { duration: "1m", target: 100 },
    { duration: "1m", target: 200 },
    { duration: "1m", target: 300 },
    { duration: "1m", target: 400 },
    { duration: "10m", target: 500 },
    { duration: "30s", target: 0 }
  ],
  thresholds: {
    http_req_failed: ["rate<0.02"]
  }
};

const BASE_URL = __ENV.BASE_URL;
const PRODUCT_ID = __ENV.PRODUCT_ID;
const USERS = JSON.parse(open("../data/users.json"));

export default function () {
  const user = USERS[(__VU - 1) % USERS.length];

  const pick = Math.random();

  if (pick < 0.1) {
    const loginRes = http.post(
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

    check(loginRes, {
      "login status 200": (r) => r.status === 200
    });
  } else if (pick < 0.3) {
    const detailRes = http.get(`${BASE_URL}/api/products/${PRODUCT_ID}`, {
      tags: { name: "product_detail" }
    });

    check(detailRes, {
      "product detail ok": (r) => r.status === 200
    });
  } else {
    const listRes = http.get(`${BASE_URL}/api/products?page=0`, {
      tags: { name: "product_list" }
    });

    check(listRes, {
      "product list ok": (r) => r.status === 200
    });
  }

  sleep(1);
}

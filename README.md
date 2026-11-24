# MealMate

MealMate is a secure Spring Boot food-delivery microservices demonstration. The React client talks only to the API Gateway; business services own their data and communicate synchronously for inventory reservation and asynchronously through Kafka for the order Saga.

## Architecture

```text
Browser :3000 -> API Gateway :9000 -> Eureka :8761
                           |-> restaurant-service -> restaurant_db
                           |-> availability-service -> availability_db
                           |-> delivery-order-service -> order_db
                           |-> payment-service -> payment_db
                           |-> delivery-service -> delivery_db
                           |-> notification-service -> notification_db
                           `-> Kafka (KRaft) -> payment/delivery/notification consumers
```

`restaurant-service` manages restaurants and menu items. `availability-service` reserves and releases menu stock. `delivery-order-service` owns order state and coordinates the Saga. Payment, delivery, and notification services each own only their own PostgreSQL database. No service reads another service's database.

## Communication, Saga, and security

Order creation synchronously reserves availability. On success it publishes `FoodOrderCreated`. Payment emits `PaymentSucceeded` or `PaymentFailed`; successful payment allows delivery assignment, which emits `DeliveryAssigned` or `DeliveryAssignmentFailed`. The order becomes `CONFIRMED` only after both succeed. Payment failure cancels the order and releases the reservation. Delivery failure cancels the order, releases the reservation, and records that a refund is required. User cancellation follows the same compensation path.

The gateway issues the bearer token at `POST /auth/login`; all business routes require `Authorization: Bearer <token>`. Health endpoints may remain public. Internal calls use an internal token or equivalent service authentication. The browser must never call a service directly. Clients send an `Idempotency-Key` on order creation; the order service stores the key/result and returns the original order for retries. Kafka consumers persist processed event IDs before applying side effects so duplicate events are harmless.

## Swagger/OpenAPI

The API Gateway exposes interactive Swagger UI at `http://localhost:9000/swagger-ui.html`. Use the Swagger UI service selector to view Restaurant, Availability, Order, Payment, Delivery, or Notification APIs. The aggregated service documents are available under `/openapi/<service-name>`.

To initialize availability for a newly created menu item, call the Gateway API with the menu item UUID:

```bash
curl -X PUT http://localhost:9000/availability/menu-items/<MENU_ITEM_ID> \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"quantity":20}'
```

The endpoint creates the availability row when it does not exist and updates the total quantity when it does. It will reject reducing the total below the currently reserved quantity.

## Start with Docker Compose

The Compose file uses deliberately non-default host ports: PostgreSQL `55432`–`55437`, Kafka `59092`, Eureka `8761`, gateway `9000`, and frontend `3000`. It expects these sibling backend directories: `discovery-server`, `api-gateway`, `restaurant-service`, `availability-service`, `delivery-order-service`, `payment-service`, `delivery-service`, and `notification-service`.

```bash
mvn clean verify
docker compose up --build
```

Open <http://localhost:3000> and sign in with the credentials configured by the gateway. Open <http://localhost:8761> to inspect Eureka. Stop with `docker compose down`; add `-v` only when intentionally deleting local databases.

For local frontend development:

```bash
cd frontend
npm install
npm run dev
```

Set `VITE_API_BASE_URL` when the gateway is not at `http://localhost:9000`. The production image is a multi-stage Node build served by nginx.

## API quick reference

| Capability | Gateway endpoint |
|---|---|
| Login | `POST /auth/login` |
| Restaurants | `POST/GET /restaurants`, `GET/PUT/DELETE /restaurants/{id}` |
| Menu | `POST/GET /restaurants/{restaurantId}/menu-items`, `PUT/DELETE .../{menuItemId}` |
| Availability | `GET /availability/menu-items/{id}`, `POST /availability/reservations`, `DELETE /availability/reservations/{orderId}` |
| Orders | `POST /food-orders`, `GET /food-orders/{id}`, `POST /food-orders/{id}/cancel` |
| Payment | `GET /payments/{orderId}` |
| Delivery | `GET /deliveries/{orderId}`, `POST /deliveries/{orderId}/status` |
| Notifications | `GET /notifications/{orderId}` |

Example flow:

```bash
TOKEN=$(curl -s -X POST http://localhost:9000/auth/login -H 'Content-Type: application/json' -d '{"username":"customer","password":"password"}' | jq -r '.token // .accessToken')
curl -H "Authorization: Bearer $TOKEN" http://localhost:9000/restaurants
curl -H "Authorization: Bearer $TOKEN" http://localhost:9000/restaurants/22222222-2222-2222-2222-222222222222/menu-items
curl -X POST http://localhost:9000/food-orders -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -H 'Idempotency-Key: demo-order-001' -d '{"customerId":"11111111-1111-1111-1111-111111111111","restaurantId":"22222222-2222-2222-2222-222222222222","items":[{"menuItemId":"33333333-3333-3333-3333-333333333333","quantity":1}],"failPayment":false,"failDeliveryAssignment":false}'
curl -H "Authorization: Bearer $TOKEN" http://localhost:9000/food-orders/<ORDER_ID>
curl -X POST -H "Authorization: Bearer $TOKEN" http://localhost:9000/food-orders/<ORDER_ID>/cancel
```

Set `failPayment` to `true` to demonstrate cancellation and release. Set `failDeliveryAssignment` to `true` to demonstrate cancellation, release, and refund-required state. Repeat the same order request with `demo-order-001` to demonstrate idempotency.

## Database access

Every database uses user `meal_mate` and password `meal_mate`, with host `localhost` and the mapped port below. Use the database name shown in DBeaver.

| Service | Database | Port |
|---|---|---:|
| Restaurant | `restaurant_db` | 55432 |
| Availability | `availability_db` | 55433 |
| Order | `order_db` | 55434 |
| Payment | `payment_db` | 55435 |
| Delivery | `delivery_db` | 55436 |
| Notification | `notification_db` | 55437 |

## Frontend demonstration

The frontend provides gateway login, restaurant/menu loading and selection, order creation, the two failure simulation switches, automatic four-second order polling, cancellation, and payment, delivery, and notification panels. If the backend returns a different resource envelope, the UI accepts either arrays or `{content: [...]}` and common token fields (`token` or `accessToken`).

## Verification checklist

```bash
cd frontend
npm install
npm run build
cd ..
docker compose config
```

The complete project should additionally pass `mvn clean verify` once backend service directories and the root Maven build are present. Required tests cover validation, reservation release, order and event idempotency, payment/delivery outcomes, Saga compensation, gateway security, and this frontend production build.

## Assumptions

- Backend service directory names and ports follow the assignment and are the Compose build contexts.
- Gateway login is `POST /auth/login` and accepts `{username,password}`; the UI supports `token` and `accessToken` response fields.
- Demo IDs are the UUIDs from the assignment and can be replaced in the UI.
- Backend implementation, root `pom.xml`, and service configuration are outside this change and were not modified.

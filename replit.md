# Smart Pantry Manager

Java Android Studio app for managing leftover ingredients and suggesting recipes only when every required ingredient and quantity is already in the pantry.

## Run & Operate

- `pnpm --filter @workspace/api-server run dev` — run the API server (port 5000)
- `pnpm run typecheck` — full typecheck across all packages
- `pnpm run build` — typecheck + build all packages
- `pnpm --filter @workspace/api-spec run codegen` — regenerate API hooks and Zod schemas from the OpenAPI spec
- `pnpm --filter @workspace/db run push` — push DB schema changes (dev only)
- Required env: `DATABASE_URL` — Postgres connection string
- `cd android-app && bash logic-tests/run.sh` — run the standalone Java strict-matching checks
- Open `android-app/` in Android Studio to sync and run the native app (Android SDK Platform 35 required)

## Stack

- Native app: Java 17 source, Android Gradle Plugin, SQLiteOpenHelper, RecyclerView, Material Components
- pnpm workspaces, Node.js 24, TypeScript 5.9
- API: Express 5
- DB: PostgreSQL + Drizzle ORM
- Validation: Zod (`zod/v4`), `drizzle-zod`
- API codegen: Orval (from OpenAPI spec)
- Build: esbuild (CJS bundle)

## Where things live

- `android-app/app/src/main/java/za/co/richfield/smartpantry/data/DatabaseHelper.java` — SQLite schema, CRUD, and recipe seeds
- `android-app/app/src/main/java/za/co/richfield/smartpantry/logic/IngredientMatcher.java` — strict recipe matching and unit conversion
- `android-app/app/src/main/res/layout/` — Android screen and list-row layouts
- `android-app/logic-tests/` — plain-Java checks for the matching rule

## Architecture decisions

- SQLite is the app's only persistence layer so the pantry works offline and stays on-device.
- Suggestions require every recipe ingredient in sufficient quantity; partial matches are excluded.

## Product

Users can manage pantry ingredients, inspect recipes that strictly match their current stock, view recipe methods, and enable expiry-soon highlights.

## User preferences

_Populate as you build — explicit user instructions worth remembering across sessions._

## Gotchas

- The Replit environment does not include the Android SDK; the Android Gradle build must be run in Android Studio with SDK Platform 35 installed.
- Do not add server-side recipe or pantry matching; the source assignment requires an offline Java Android application.

## Pointers

- See the `pnpm-workspace` skill for workspace structure, TypeScript setup, and package details

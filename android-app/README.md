# Smart Pantry Manager

A Java Android app for tracking pantry leftovers and finding recipes that can be made without buying missing ingredients.

## Features

- Pantry item create, read, update, and delete with quantity, unit, and optional expiry date
- Local SQLite storage that persists after the app is closed
- 20 seeded recipes, each with required quantities and preparation steps
- Strict suggestions: every listed recipe has every required ingredient in sufficient quantity
- Matching handles common singular/plural names and converts compatible weight and volume units
- Pantry, recipe suggestions, recipe detail, add/edit ingredient, and settings screens
- Optional expiry reminders highlight items expiring within seven days
- RecyclerView adapters and bottom navigation

## Database choice

**Chosen database: SQLite.** SQLite stores the pantry directly on the device, works offline, and fits this app's single-user scope without requiring a hosted service or network connection.

## Open and run

1. Open the `android-app` folder in Android Studio.
2. Install Android SDK Platform 35 and use JDK 17 or newer.
3. Allow Gradle to sync, then run the `app` configuration on an emulator or Android device.

The first launch creates the SQLite database and seeds the recipe collection. The pantry starts empty so the user can enter ingredients they actually have.

## Verify the matching rule

The matching engine is plain Java and can be checked without an Android emulator:

```sh
cd android-app
bash logic-tests/run.sh
```

The checks cover missing ingredients, insufficient quantities, plural names, duplicate pantry quantities, compatible unit conversions, incompatible units, and empty data.

## Data model

- `pantry_items`: ingredient name, quantity, unit, optional expiry date
- `recipes`: recipe name and preparation method
- `recipe_ingredients`: the required ingredient names, quantities, and units for each recipe

## GitHub and assignment evidence

Use the public repository created for this assignment and commit real development steps as they happen. Do not squash the work into a single final commit. Add the student's report, actual app screenshots, and narrated demonstration separately; the app source does not replace those assessment deliverables.

## Matching notes

The matcher compares normalized ingredient names and adds quantities only when their units are compatible. Grams, kilograms, milligrams, ounces, and pounds share a weight dimension; millilitres, litres, teaspoons, tablespoons, and cups share a volume dimension. Custom units only match the same normalized unit. A missing ingredient, an incompatible unit, or an insufficient amount excludes the whole recipe.

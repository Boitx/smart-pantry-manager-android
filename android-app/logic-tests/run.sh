#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PACKAGE_DIR="$ROOT/app/src/main/java/za/co/richfield/smartpantry"
OUTPUT_DIR="$(mktemp -d)"
trap 'rm -rf "$OUTPUT_DIR"' EXIT

javac -d "$OUTPUT_DIR" \
  "$PACKAGE_DIR/model/PantryItem.java" \
  "$PACKAGE_DIR/model/RecipeIngredient.java" \
  "$PACKAGE_DIR/model/Recipe.java" \
  "$PACKAGE_DIR/logic/IngredientMatcher.java" \
  "$ROOT/logic-tests/IngredientMatcherSmokeTest.java"

java -ea -cp "$OUTPUT_DIR" IngredientMatcherSmokeTest

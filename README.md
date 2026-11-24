# OpenPOS

A free, open-source, offline point-of-sale app for Android — built as an
alternative to proprietary POS apps like Nexchar that lock features behind
a monthly subscription and an account login.

**No account. No login. No payment. No internet permission at all.**
Every feature in this app is unlocked from the moment you install it, and
all your data stays on your device, in a local database, forever. The app
doesn't even request the `INTERNET` permission — look at
`app/src/main/AndroidManifest.xml` and you'll see it isn't there.

## Features

- **Sell screen** — tap-to-add product grid, category filter chips, search
  by name/SKU/barcode, running cart total
- **Cart & checkout** — per-line quantity and discount, order-level
  discount, tax, multiple payment methods (cash with change-due, card,
  mobile, other), customer name, notes
- **Receipts** — auto-generated invoice numbers, a receipt screen you can
  share as text (to messaging apps, printers via share sheet, etc.)
- **Sales history** — every past sale, tap through to its receipt
- **Inventory** — stock-on-hand per product, low-stock tab, restock entries
  that are logged as stock movements
- **Expenses** — categorized expense tracking with custom categories
- **Reports** — sales vs. expenses bar chart over 7/30/90 days, gross &
  net profit (accounting for cost of goods sold), top-selling products
- **Categories** — create/delete product categories
- **Backup & restore** — export your entire database to a single JSON
  file you choose where to save, and import it back (e.g. to move to a
  new phone). This is the only "sync" the app has — it's a file you move
  yourself, never a server.
- **Currency symbol** — change the `$` to whatever you need in Settings

## What's not included (yet)

This is a solid first version, not a byte-for-byte clone of every Nexchar
feature. Things like multi-store support, barcode-scanner camera input
(you can still type a barcode into search/SKU fields), staff-role
permissions, and printer-specific receipt formatting aren't built yet.
The architecture (Room database + repository pattern) is set up so any of
these can be added later without a rewrite.

## Tech stack

- Kotlin, Jetpack Compose (Material 3), Navigation Compose
- Room (local SQLite database) — the *only* place data lives
- Gson, used only to read/write your own local backup JSON file
- MVVM: `data/` (entities, DAOs, repository) → `ui/<feature>/` (ViewModel + Composable screens)

No networking library is included anywhere in this project, on purpose.

## How to build it

1. Install [Android Studio](https://developer.android.com/studio) (free).
2. Open this folder (`OpenPOS/`) as a project — **File → Open**.
3. Let Android Studio sync Gradle (first sync downloads dependencies and
   can take a few minutes). If it asks about the Gradle wrapper, let it
   regenerate `gradle-wrapper.jar` automatically — that one small binary
   file wasn't included in this delivery since it couldn't be downloaded
   from this environment, but Android Studio creates it for you on sync.
4. Press **Run ▶** with a device or emulator selected (minimum Android 7.0 / API 24).
5. To get an installable APK without Android Studio's Run button:
   **Build → Build Bundle(s) / APK(s) → Build APK(s)**, then find it in
   `app/build/outputs/apk/debug/`.

## Project layout

```
app/src/main/java/com/openpos/app/
  data/
    entity/        Room entities (Product, Sale, Expense, ...)
    dao/            Room DAOs
    AppDatabase.kt
    repository/     PosRepository — the single source of truth
    BackupManager.kt  JSON export/import
  ui/
    products/       Sell screen + product editor
    categories/      Category manager
    cart/            Cart + checkout + the shared CartViewModel
    sales/           Sales history + receipt
    inventory/       Stock view + restock
    expenses/        Expense list + editor
    reports/         Charts + profit/loss
    settings/        Currency + backup/restore
    navigation/      Bottom nav + NavHost wiring
    theme/           Material 3 theme
  MainActivity.kt
  OpenPosApp.kt      Tiny manual DI container (repository + backup manager)
```

## License

Public domain / do whatever you want with it (equivalent to CC0 / Unlicense) —
it's yours. Add features, rebrand it, sell support for it, whatever you need.

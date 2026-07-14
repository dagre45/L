# Lifesteal SMP — Complete Setup Pack

A load-and-go Lifesteal SMP for **Paper 1.21.x**. Kill a player, steal a
heart. Hit 0 hearts, get eliminated. Craft Heart Fragments to buy hearts
back, craft (or earn) a Revive Beacon to bring someone back from the dead.
Everything below is copy-paste config plus one small custom plugin
(`LifestealCore`) that implements the mechanic itself — **every other
plugin in the stack is free**, so buyers need nothing extra to launch.

## What's in this repo

```
LifestealCore/              Java/Maven source for the core heart-mechanic plugin
server/plugins/             Ready-to-copy plugin config files, laid out exactly
                             as they should sit in your server's plugins/ folder
server/config/               server.properties / paper-world-defaults.yml excerpts
                             + the one-time WorldGuard spawn-region setup script
docs/                        Install guide, plugin stack reference, recipes
                             reference, and specs for the sellable upsell addons
```

## Core mechanic (implemented in `LifestealCore`)

- Players start at **10 hearts** (`hearts.starting-hearts`), capped at **20**
  (`hearts.max-hearts`) — both configurable.
- A PvP kill transfers 1 heart from victim to killer.
- Hitting 0 hearts eliminates the player: permanently banned, temporarily
  banned (auto-expires), or forced-spectator, per
  `plugins/LifestealCore/config.yml` → `elimination.mode`.
- **Heart Fragment** item (craftable, see `docs/RECIPES.md`): right-click to
  permanently gain +1 heart, capped at the server max.
- **Revive Beacon** item (craftable or crate/shop-earned): `/revive <player>`
  while holding it un-bans/un-spectates an eliminated player.
- `/withdrawheart` converts one of your live hearts into a tradeable Heart
  Fragment item (floor-protected so you can't withdraw yourself to death).
- Native combat-log punishment (steals a heart or bans, your choice) — no
  separate CombatLogX dependency, and it's the only implementation that
  correctly applies to the heart economy.
- `%lifesteal_hearts%`, `%lifesteal_maxhearts%`, `%lifesteal_kills%`,
  `%lifesteal_deaths%`, `%lifesteal_eliminated%` PlaceholderAPI placeholders
  for the scoreboard/tablist/shop displays.

Full command/permission reference:
`LifestealCore/src/main/resources/plugin.yml`.
Full config reference: `server/plugins/LifestealCore/config.yml` (comments
inline) and `server/plugins/LifestealCore/messages.yml`.

## Plugin stack — 100% free, zero required premium purchases

| Plugin | Role | Cost | Optional premium upgrade |
|---|---|---|---|
| **Paper** 1.21.x | Server software | Free | — |
| **LifestealCore** | Heart mechanic, elimination, custom items (this pack) | Included | — |
| **Vault** | Economy API bridge | Free | — |
| **EssentialsX** | Economy backend, homes/tpa/spawn/kits | Free | — |
| **EconomyShopGUI** (free tier) | Item shop, heart pricing | Free | EconomyShopGUI Premium / ShopGUI+ (~$20) — NPC shops, multi-currency. Not required. |
| **CrazyCrates** | Heart/gear crates | Free (fully open source) | ExcellentCrates (~$20) — nicer GUIs. Not required. |
| **TAB** | Scoreboard, tablist, nametags | Free | — |
| **PlaceholderAPI** | Placeholder engine (LuckPerms/Vault/LifestealCore expansions) | Free | — |
| **LuckPerms** | Ranks, prefixes | Free | — |
| **GriefPrevention** | Player land claims | Free | Lands (~$20) — richer claim GUI. Not required. |
| **WorldEdit + WorldGuard** | Spawn safe-zone region (no PvP at spawn) | Free | — |
| **Vulcan** (or Grim) | Anti-cheat baseline | Free | — |
| Paper built-in Anti-Xray | Ore obfuscation | Free, no plugin | Orebfuscator — heavier, only needed for very high-pop servers. |

**Recommended, not required:** CoreProtect (free) for staff grief-rollback
logging — install it and run `//` nothing needed, it works with zero config.

## Quick install

See `docs/INSTALL.md` for the full walkthrough. Short version:

1. Stand up a Paper 1.21.x server.
2. `cd LifestealCore && mvn clean package` → drop the built jar from
   `target/LifestealCore-1.0.0.jar` into `plugins/`.
3. Download the free plugins listed above into `plugins/` (links in
   `docs/INSTALL.md`).
4. Copy everything under `server/plugins/` into your server's `plugins/`
   folder (merging into each plugin's auto-generated defaults — see the
   header comment in each file for merge-vs-overwrite guidance).
5. Merge the `server/config/*.partial` files into `server.properties` /
   `config/paper-world-defaults.yml`.
6. Start the server once so every plugin generates its remaining defaults,
   stop it, then run the WorldGuard spawn setup in
   `server/config/spawn-region-setup.txt` and the LuckPerms rank bootstrap in
   `server/plugins/LuckPerms/rank-ladder-setup.txt`.
7. Start for real.

## Balance knobs you'll actually touch

- `plugins/LifestealCore/config.yml` → `hearts.*` and `elimination.*` — the
  entire game balance lives here.
- `plugins/EconomyShopGUI/shops/hearts.yml` — heart/revive-beacon shop
  pricing (pre-tuned as a "catch-up" sink, not a primary heart source).
- `plugins/CrazyCrates/Crates/heart_crate.yml` — crate drop odds.

## Upsell addons

Four sellable, custom-coded add-ons are specced (not implemented) in
`docs/UPSELL_ADDONS.md`: Heart Bounty System, Elimination Killstreaks,
Custom Enchants Pack, Seasons/Reset Automation.

## A note on building the plugin

This repo was assembled in a sandboxed environment without access to the
PaperMC Maven repository, so `LifestealCore` has **not** been
compiled/tested here — treat it as reviewed, ready-to-build source, not a
verified jar. Run `mvn clean package` on a machine with normal internet
access (or in your CI) before shipping; the `pom.xml` pulls `paper-api`
1.21.1 and `placeholderapi` (both provided-scope, so the built jar has no
bundled dependencies to worry about). Please compile-test it before selling.

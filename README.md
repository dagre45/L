# Lifesteal SMP — Setup Pack

Kill a player, steal a heart. Hit 0 hearts, get banned. Craft a Heart
Fragment to earn a heart back. Craft (or earn) a Revive Beacon to bring
someone back from the dead.

This pack includes one small custom plugin (`LifestealCore`) that runs the
whole mechanic, plus ready-to-copy configs for everything else. **The rest
of the stack is 100% free** — nothing else to buy.

## Setup in 3 steps

1. **Build the plugin.**
   ```
   cd LifestealCore
   mvn clean package
   ```
   Copy `target/LifestealCore-1.0.0.jar` into your server's `plugins/` folder.

2. **Get the free plugins** listed in the table below, and copy everything
   from this pack's `server/plugins/` and `server/config/` folders into the
   matching folders on your server.

3. **Run the one-time setup commands** — `server/config/spawn-region-setup.txt`
   (locks spawn as no-PvP) and `server/plugins/LuckPerms/rank-ladder-setup.txt`
   (builds the rank ladder). Then start the server.

Full walkthrough with download links and a testing checklist:
**`docs/INSTALL.md`**.

## What you get

- **Hearts:** start at 10, cap at 20 (both configurable).
- **PvP kills** steal 1 heart from the loser.
- **0 hearts = eliminated** — ban, temp-ban, or spectator mode, your choice.
- **Heart Fragment** item: craft it, right-click it, +1 heart.
- **Revive Beacon** item: `/revive <player>` brings someone back.
- **`/withdrawheart`**: turn a heart into a tradeable item.
- Built-in combat-log punishment — no extra plugin needed for that.
- Scoreboard, tablist, and shop all show live heart/kill counts automatically.

All of this lives in `plugins/LifestealCore/config.yml` — that one file is
where you tune the whole game.

## The plugin stack (all free)

| Plugin | What it does |
|---|---|
| Paper 1.21.x | The server itself |
| **LifestealCore** | Heart mechanic (included in this pack) |
| Vault + EssentialsX | Economy, homes, tpa, kits |
| EconomyShopGUI | The item shop (hearts included) |
| CrazyCrates | Reward crates |
| TAB | Scoreboard, tablist, nametags |
| PlaceholderAPI | Glue between the other plugins |
| LuckPerms | Ranks and prefixes |
| GriefPrevention | Player land claims |
| WorldEdit + WorldGuard | No-PvP spawn zone |
| Vulcan | Anti-cheat |
| Paper's built-in anti-xray | Ore hiding (no plugin needed) |

Want fancier versions later? ShopGUI+, ExcellentCrates, and Lands are
optional paid upgrades for the shop/crates/claims — but nothing here
requires them.

## Where to tune things

- `plugins/LifestealCore/config.yml` — hearts, elimination, combat-log rules.
- `plugins/EconomyShopGUI/shops/hearts.yml` — heart/revive-beacon prices.
- `plugins/CrazyCrates/Crates/heart_crate.yml` — crate odds.

## Want more? (sold separately)

Four add-ons are designed and spec'd, ready to be custom-coded on request:
Heart Bounties, Killstreaks, a Custom Enchants pack, and Season Resets. See
`docs/UPSELL_ADDONS.md`.

## Before you sell it

The plugin was written and reviewed here, but **not yet compiled** — this
environment couldn't reach the Maven repo it needs. Run `mvn clean package`
yourself (or in CI) and smoke-test it before shipping to buyers.

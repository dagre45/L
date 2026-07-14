# Upsell addons — specs for separate, custom-coded modules

These are **not implemented** in this pack. Each is scoped as a standalone
add-on plugin that hooks into `LifestealCore`'s public API
(`HeartManager`, `LifestealConfig`, the `lifesteal_*` PlaceholderAPI
expansion) so it can be sold and installed independently on top of the base
pack. Suggested BuiltByBit tiering: bundle 2+ of these as a "Pro Pack" above
the base resource's price.

---

## 1. Heart Bounty System

**Pitch:** turns any player into a hunted target with a visible cash + heart
reward, driving PvP engagement during slow hours.

- `/bounty set <player> <amount>` — places a bounty funded from the
  setter's Vault balance; stacks with existing bounties on the same target.
- Auto-bounties: any player who eliminates 3+ players in `X` minutes
  automatically gets a bounty placed on them from a server-funded pool
  (config: `auto-bounty.trigger-kills`, `auto-bounty.window-seconds`,
  `auto-bounty.amount`).
- Killing a bountied player pays out their bounty pool **in addition to**
  the normal `HeartManager.handlePvpKill()` heart steal — call the same
  method, then pay the bounty separately so hearts and cash stay decoupled.
- Bounty board GUI (paginated, sorted by amount) + `%bounty_top_target%`
  and `%bounty_amount_<player>%` placeholders for TAB/scoreboard.
- Broadcasts on bounty placement and payout for server-wide hype.
- Anti-abuse: cannot bounty yourself; a cooldown prevents bounty-farming
  with an alt account (same-IP / same-Discord-linked-account check).

**Data:** own SQLite table `bounties(target_uuid, amount, funded_by)`,
independent of LifestealCore's `playerdata.yml`.

---

## 2. Elimination Killstreaks

**Pitch:** rewards momentum — the more consecutive kills without dying, the
better the perks, with escalating server-wide announcements.

- Tracks a per-player streak counter, reset to 0 on death (any cause) —
  hook `EliminationListener`'s death handling (or listen to the same
  `PlayerDeathEvent` at a lower priority) to read `killer`/`victim`.
- Streak tiers (configurable): 3 kills = speed/strength potion effect for
  60s, 5 kills = glowing outline visible to all + broadcast, 10 kills =
  "Godlike" title + a guaranteed Revive Beacon drop on eventual death, 15+ =
  server-wide boss-bar countdown challenging anyone to stop them.
- `/killstreak top` leaderboard (all-time and this-wipe), fed by
  `%lifesteal_kills%`-adjacent placeholders this addon adds
  (`%killstreak_current%`, `%killstreak_best%`).
- Streak-breaker bonus: the player who ends a 10+ streak gets a bonus heart
  or crate key on top of the normal steal, via a direct call into
  `HeartManager.addHearts()` after the base kill is processed.
- Config toggle to disable streak perks entirely and keep only the
  leaderboard/cosmetics, for servers that want zero PvP-power creep.

---

## 3. Custom Enchants Pack

**Pitch:** a curated, PvP-balanced enchant set that gives progression depth
without power-creeping past what a stolen-heart economy can handle.

Proposed enchants (all custom, applied via anvil/enchant table hook or
sold pre-applied in the shop/crates):

| Enchant | Effect | Max level | Balance note |
|---|---|---|---|
| Vampirism | On PvP hit, heal 1 HP to attacker (does **not** steal a heart — that stays exclusive to a kill) | 3 | capped healing/sec to prevent infinite-sustain kiting |
| Executioner | +15% damage vs. targets under 3 hearts | 2 | rewards finishing fights, discourages fleeing at 1 heart |
| Lifeline | On taking fatal damage, 10%/lvl chance to survive at 1 HP (like Totem, but a %, not guaranteed) | 3 | internal cooldown (5 min) so it's not a repeatable Totem |
| Withdraw Guard | Reduces the minimum-hearts-remaining floor for `/withdrawheart` by 1/lvl | 2 | economy-only, zero combat power |

- Ships as its own `NamespacedEnchantment`-backed items (Paper 1.20.5+
  custom enchantment API) or a PDC-tag + listener implementation for
  cross-version safety on older Paper builds.
- Config-gated per-enchant max level and enable/disable, so server owners
  can trim the set to taste.
- Anti-stacking rule: Vampirism + Executioner explicitly excluded from
  co-existing on the same weapon (config `mutually-exclusive` groups) to
  cap PvP power creep.

---

## 4. Seasons / Reset Automation

**Pitch:** turnkey wipe-and-relaunch cycle so admins never hand-run a reset
again — critical for Lifesteal servers, which live and die by season hype.

- `/season end` (or scheduled via config `season.length-days`) triggers:
  1. Freeze joins, broadcast countdown (configurable T-minus warnings).
  2. Snapshot final leaderboard (kills, hearts, playtime) to a webhook
     (Discord embed) and a static HTML recap page written to
     `plugins/SeasonReset/recaps/season-N.html`.
  3. Archive `playerdata.yml`/DB to `plugins/LifestealCore/archive/season-N/`.
  4. Reset every player's hearts to `hearts.starting-hearts`, clear
     eliminated/ban flags (full amnesty for a fresh season), reset kill/death
     counters.
  5. Regenerate or roll over to a pre-generated new world folder (integrates
     with Multiverse-Core if present, else raw world-folder swap + restart).
  6. Wipe LuckPerms `veteran`/`elite` ranks back to `member` unless
     `season.keep-donor-ranks: true` (Immortal/donor cosmetics usually
     persist across seasons; earned PvP ranks usually don't).
  7. Re-open joins, announce season start, optionally auto-run a "starter
     crate key" giveaway to everyone online for hype.
- Dry-run mode (`/season end --dry-run`) that logs every step it *would*
  take without executing, for admins to sanity-check before a real wipe.
- Season pass integration hook (stub) for a future battle-pass addon.

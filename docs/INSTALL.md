# Install guide

Target: **Paper 1.21.1+**, Java 21. Written for a fresh server; adapt paths
if you're layering this onto an existing install.

## 1. Server software

Download Paper 1.21.x from https://papermc.io/downloads and set it up as
normal (`eula.txt`, start script with `-Xmx`/`-Xms` sized to your host).
Start it once and stop it so the base folder structure exists.

## 2. Build LifestealCore

```
cd LifestealCore
mvn clean package
cp target/LifestealCore-1.0.0.jar /path/to/server/plugins/
```

Needs Maven + JDK 21. If you don't want to build it yourself, any
Java-plugin build service (Jenkins, GitHub Actions with `actions/setup-java`)
works — the `pom.xml` has no unusual repositories beyond PaperMC's and
PlaceholderAPI's public Maven repos.

## 3. Download the free plugin stack

| Plugin | Where to get it |
|---|---|
| Vault | https://www.spigotmc.org/resources/vault.34315/ |
| EssentialsX | https://essentialsx.net/downloads.html |
| EconomyShopGUI | https://www.spigotmc.org/resources/economyshopgui.69927/ |
| CrazyCrates | https://modrinth.com/plugin/crazycrates or https://github.com/Crazy-Crew/CrazyCrates |
| TAB | https://www.spigotmc.org/resources/tab-updated-continued.57806/ |
| PlaceholderAPI | https://www.spigotmc.org/resources/placeholderapi.6245/ |
| LuckPerms | https://luckperms.net/download |
| GriefPrevention | https://www.spigotmc.org/resources/griefprevention.1884/ |
| WorldEdit | https://enginehub.org/worldedit |
| WorldGuard | https://enginehub.org/worldguard |
| Vulcan | https://www.spigotmc.org/resources/vulcan.99201/ (or Grim: https://github.com/GrimAnticheat/Grim) |
| CoreProtect (recommended) | https://www.spigotmc.org/resources/coreprotect.8631/ |

Drop every jar into `plugins/`, start the server once so each generates its
default config, then stop it again before continuing.

## 4. Copy in the pack's configs

Everything under this repo's `server/plugins/` mirrors your server's
`plugins/` folder path-for-path. For each file:

- If the target file doesn't exist yet (plugin hasn't been started), just
  copy it straight in.
- If the plugin already generated a big default file (EssentialsX,
  GriefPrevention, LuckPerms, WorldGuard, Vulcan), **open both files side by
  side and merge only the keys shown** — those files are marked "partial"
  in their header comment specifically so you don't blow away the plugin's
  other defaults.
- `LifestealCore/config.yml` and `messages.yml` are complete, standalone
  files — copy them in wholesale (or just let the plugin generate them from
  its bundled resources on first boot, which are byte-identical).

## 5. World / server-level config

- Merge `server/config/server.properties.partial` into `server.properties`.
- Merge `server/config/paper-world-defaults.yml.partial`'s `anticheat:`
  block into `config/paper-world-defaults.yml`.

## 6. One-time bootstrap commands

Start the server, then from console:

1. Run every line in `server/plugins/LuckPerms/rank-ladder-setup.txt` to
   build the rank ladder (member → veteran → elite → immortal, plus a
   helper/mod/admin staff branch).
2. Have PlaceholderAPI pull in the expansions TAB needs:
   ```
   papi ecloud download Vault
   papi ecloud download Player
   papi reload
   ```
   (the LuckPerms expansion registers itself automatically once LuckPerms
   is present — no ecloud download needed for it.)
3. Stand at your spawn build and run every command in
   `server/config/spawn-region-setup.txt` to lock spawn down as a
   no-PvP/no-grief WorldGuard region.
4. Restart once more so TAB, GriefPrevention, and the WorldGuard region all
   pick up the final config.

## 7. Sanity checklist before you open to players

- [ ] Kill a friend in a test — victim should visibly lose a heart, killer
      gain one, and a broadcast should fire (`elimination.broadcast-message`).
- [ ] Reduce a test account to 0 hearts (`/lifesteal sethearts <you> 1` then
      let a friend finish you) and confirm the ban/elimination screen shows.
- [ ] `/lifesteal unban <player>` (or a Revive Beacon + `/revive`) brings
      them back with `hearts.revive-hearts`.
- [ ] Craft a Heart Fragment and Revive Beacon using the recipes in
      `docs/RECIPES.md` — confirm right-click consumption works.
- [ ] Open the EconomyShopGUI hearts category, buy and sell a Heart
      Fragment — confirm the bought item is right-click consumable (this
      validates the material+CustomModelData fallback matching described in
      `LifestealCore`'s `CustomItems.java`).
- [ ] Open/win a Heart Crate — confirm the reward item also right-click
      consumes correctly.
- [ ] Walk into the spawn region — confirm PvP is blocked there and enabled
      the moment you step outside it.
- [ ] Combat-tag a test account and log out during the timer — confirm the
      configured punishment (`combat-log.punishment`) fires.

## Storage upgrade (large servers only)

`LifestealCore` ships with a flatfile YAML player-data store
(`playerdata.yml`), fine for a few hundred concurrent players. If you're
running a network-scale player count, swap `DataStore.java` for a
SQLite/MySQL-backed implementation of the same public methods
(`load()`, `get(UUID)`, `markDirty()`, `saveAll()`) — the rest of the plugin
only talks to that interface, so nothing else needs to change.

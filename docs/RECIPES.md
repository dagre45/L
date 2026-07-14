# Custom recipes — Heart Fragment & Revive Beacon

The two custom items are defined entirely in `plugins/LifestealCore/config.yml`
under the `recipes:` and `items:` sections, and are registered as real shaped
crafting recipes by `RecipeManager` on plugin startup (no datapack required).

## Heart Fragment

```
G D G
D N D      G = Gold Ingot
G D G      D = Diamond
           N = Nether Star
```

Crafts one **Heart Fragment** (`NETHER_STAR`, CustomModelData `100001`).
Right-clicking it grants the consumer +1 heart (capped at `hearts.max-hearts`).

## Revive Beacon

```
G E G
E H E      G = Gold Block
G E G      E = Emerald Block
           H = one crafted Heart Fragment (special token, not a vanilla item)
```

Crafts one **Revive Beacon** (`BEACON`, CustomModelData `100002`). Consumed by
`/revive <player>` to un-ban/un-spectate an eliminated player.

## Editing the recipes

Edit the `shape` (3x3 grid, up to 3 rows/cols, spaces = empty) and
`ingredients` map in `config.yml`, then run `/lifesteal reload` — recipes are
re-registered automatically. Any vanilla `Material` enum name works as an
ingredient; the literal string `HEART_ITEM` in an ingredient slot means "one
of this pack's crafted Heart Fragments."

## Why not a pure datapack?

A `.json` datapack recipe can produce the *result* item, but data-pack JSON
cannot attach the persistent-data tag LifestealCore uses to recognize the
item (nor react to it being right-clicked). Registering the recipe from the
plugin means the crafted result is always correctly tagged. If you'd rather
ship it as a vanilla datapack for a build that doesn't want the plugin
controlling recipes, set `recipes.enabled: false` in config.yml and add your
own datapack recipe under
`world/datapacks/lifesteal/data/lifesteal/recipe/heart_fragment.json` — but
note the crafted item then won't carry the internal tag, so it will only be
recognized by the material + CustomModelData fallback matcher (see
`CustomItems.java`), which still works as long as your datapack item stack's
model data matches `items.heart-item.custom-model-data` in config.yml.

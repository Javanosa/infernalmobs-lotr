You may have heard of the mod "Infernal Mobs" which turns various mobs into epic bosses and great loot. However it didnt work properly with lotr mod previously. I have made a fork with countless bugfixes and rewrites, lotr mod compatibility and even some new features. The original author is [Atomicstryker](<https://github.com/AtomicStryker/atomicstrykers-minecraft-mods>)

**Infernalmobs-LOTR** - **Download: [Release 1.7.3](https://github.com/Javanosa/infernalmobs-lotr/releases/tag/1.7.3)**

- LOTR NPCs can become infernal now
  - Factor to control the rarity of Allied and enemied NPCs seperately
  - Civlians and Hireds cannot turn infernal (for now, request if you want it differently)
- Improved Boss Glow Rendering, Destructive mobs now appear with a flame
- Improved Boss Bar rendering and taking lotr huds into account
- Added lotr enchantments to the loot generator
- Added lotr drops to the loot
- Option to configure the need of a direct player kill and general drop chance
- Item now drop with configurable durability and enchanting power
- Stops infernalmobs from using XP and vanilla enchantments if vanilla Enchanting is disabled in lotr
- Added lotr banner protection compatibility
- Fixed Network spam
- Fixed Boss Glow Rendering during paused game
- Added Serverside Particles
- Tweaked health for entities that have a huge range of health values
- Reworked client-server sync, saving and loading of mods
- New Mods:
  - Bomber - explodes
  - Arsonist - sets things on fire during attacks
  - Various tweaks and bugfixes to existing Mods
    - Fixed overpowered lifesteal and regen mods
    - Made many mods work with proper targets aswell as other entities
    - Remove Blocks and Projectiles after some time to avoid trashing the world
    - Increased effect length for Mod Cloaking
    - Fixed Teleportation for Ninja and Ender
    - Added more effects to Alchemist
    - Webs now place in better ways and dont miss the target as much anymore

- Added Server Side Particles to some Mods like Ender and Bomber
- Particles are now lit so it can be seen better in dark
- Fix Choke Ability and rendering
- Hired Units now have a rare chance to learn abilities of killed infernal mobs
	- The chance scales with amount of abilities of the killed boss
	- infernalmobslotr.cfg -> abilityRewardChance
	- Whether Hired Units can learn destructive Abilities
	- infernalmobslotr.cfg -> canLearnDestructiveAbilities
- Reworked player & entity targeting
- Attacks in Creative Mode no longer trigger destructive abilities
- The new [ME-Tweaks update](https://curseforge.com/minecraft/mc-mods/lotr-middleearth-tweaks) patched fireballs ignoring Banner Protection

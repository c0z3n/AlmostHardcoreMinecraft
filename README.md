## Almost Hardcore Minecraft
AlmostHardcoreMinecraft is a minecraft server (bukkit API) plugin implementing something pretty close to hardcore mode on a multiplayer server. This is because I wanted to play "hardcore" minecraft on a server with friends, without dealing with getting banned when you die. I also wanted to leave the chance open for cooperative play.

AlmostHardcoreMinecraft is in early stages.

### Features/Mechanics

- when a player dies, they spawn at a new location. If the last place they spawned is the current server spawn point, a new spawn point for the server is generated. If not, the player spawns at the server spawn point.

- spawn points are [currently|naively] implemented to simply pick a new random block within a large square area surrounding spawn, the dimension of which is defined by a value in the plugin config file. this implementation is something I am trying to decide how to replace

- when a player dies, all the ender chests they placed during that life are converted to regular chests, and their ender chest inventory is split between them. then their ender chest inventory is cleared, so that when they build an ender chest in their next life, they are unable to access the ender chest inventory from their previous life

- commands `/mydeaths` and `/deaths` which display your total deaths and all players total deaths, respectively 

- the server keeps track of how many nights a player survives for the purposes of keeping a stat for you to try to beat.

The basic implementation of this plugin was slammed together in a single evening, so there's probably a lot wrong with it. Now that I am working on it a little more, I *know* that there are a few architectural issues and corner cases I haven't covered, but it works fine enough for now. I plan to keep updating it as I run it on a small server and we see how it plays. check out the [issues](https://github.com/c0z3n/AlmostHardcoreMinecraft/issues) to see some stuff that I plan on working on, or feel free to submit your own issues for ideas, suggestions, or bugs. Let me know if you try it with your friends, I'd love to hear about it.

### Installation

This is a bukkit plugin, and it should work if you compile it against your favorite bukkit api compatable .jar file and chuck it in your server /plugins directory. ~~I'll add a .jar release to this repo when i get around to it (and it's a little more stable).~~ There is a precompiled `AlmostHardcore.jar` file compatible with craftbukkit 1.8.8 in the [Builds/](Builds) directory. AlmostHardcoreMinecraft also uses the database provided by / defined in `bukkit.yml`, which by default is sqlite. So make sure you have that installed or an alternative defined in `bukkit.yml`.

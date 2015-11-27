## Almost Hardcore Minecraft
AlmostHardcoreMinecraft is a minecraft server (bukkit API) plugin implementing something pretty close to hardcore mode on a multiplayer server. This is the result of wanting to play "hardcore" minecraft on a server with friends, without dealing with getting banned when you die. I also wanted to leave the chance open for cooperative play.

Currently, this plugin basically just manages the world spawn points. it's behavior is pretty simple:

 - keep a running record of where each player spawned last
 - when a player dies, check if the last place they spawned is the current server spawn point
     - if no, spawn the player at the most current server spawn point
     - if yes, randomly generate a new server spawn point tens or hundreds of thousands of blocks away, and spawn them there.

The result is that no player will spawn at the same place twice, but players may end up spawning at the same location and running into each other. 

there are also commands `/mydeaths` and `/deaths` which display your total deaths and all players total deaths, respectively 

The basic implementation of this plugin was slammed together in a single evening, so there's probably a lot wrong with it. I plan to keep updating it as I run it on a small server and we see how it plays. check out the [issues](https://github.com/c0z3n/AlmostHardcoreMinecraft/issues) to see some stuff that I plan on working on, or feel free to submit your own issues for ideas, suggestions, or bugs. Let me know if you try it with your friends, I'd love to hear about it.

### Installation

This is a bukkit plugin, and it should work if you compile it against your favorite bukkit api compatable .jar file and chuck it in your server /plugins directory. ~~I'll add a .jar release to this repo when i get around to it (and it's a little more stable).~~ There is a precompiled .jar file compatible with craftbukkit 1.8.8 in the [Builds/](Builds) directory.

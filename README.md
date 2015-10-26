AlmostHardcoreMinecraft
=======================
A minecraft server plugin implementing something very close to hardcore mode.

This is the result of a desire for the ability to play "hardcore" minecraft on a server with friends, without dealing with getting banned when you die. I also wanted to leave the chance open for cooperative play.

Currently, this plugin basically just manages the world spawn points. it's behavior is simple:

 - keep a running record of where each player spawned last
 - when a player dies, check if the last place they spawned is the current server spawn point
     - if no, spawn the player at the most current server spawn point
     - if yes, randomly generate a new server spawn point tens or hundreds of thousands of blocks away, and spawn them there.

The result is that no player will spawn at the same place twice, but players may end up spawning at the same location and running into each other. 

The basic implementation of this plugin was slammed together in a single evening, so there's probably a lot wrong with it. I plan to keep updating it as I run it on a small server and we see how it plays. Let me know if you try it with your friends, I'd love to hear about it.

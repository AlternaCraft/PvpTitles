# PvpTitles 2015 Edition
[![Build Status](https://travis-ci.org/AlternaCraft/PvpTitles.svg)](https://travis-ci.org/AlternaCraft/PvpTitles)
[![codecov](https://codecov.io/gh/AlternaCraft/PvpTitles/branch/2.x/graph/badge.svg)](https://codecov.io/gh/AlternaCraft/PvpTitles)
[![Download](https://api.bintray.com/packages/alternacraft/maven/PvpTitles/images/download.svg)](https://www.github.com/alternacraft/PvpTitles/releases)
[![Web](https://img.shields.io/badge/Web-alternacraft.github.io%2FPvpTitles%2F-yellow.svg)](https://alternacraft.github.io/PvpTitles) 
[![IRC](https://img.shields.io/badge/IRC-%23PvpTitles-yellow.svg)](http://webchat.freenode.net/?channels=%23PvpTitles) 

PvpTitles is a [Bukkit](https://github.com/Bukkit/Bukkit) / [Spigot](https://github.com/SpigotMC/Spigot-API) plugin which is based on the [PvP titles plugin](https://github.com/dreanor/PvPTitles) by [asc_dreanor](https://github.com/dreanor). These titles reflect a player's success in defeating other players by earning titles after a specific amount of Kills.

Developed from version [1.7.10](https://mcupdate.tumblr.com/post/89960443749/minecraft-1710) of [Minecraft](http://minecraft.net) using [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

Downloads
---------

**Official builds**:

You can find the official builds at bukkit and spigot:

* http://dev.bukkit.org/bukkit-plugins/pvptitles/
* https://www.spigotmc.org/resources/pvptitles.20927/

**Development builds**:

```python
"Development builds of this project can be acquired at the provided continuous integration server."
"These builds have not been approved by the BukkitDev staff. Use them at your own risk."
```

[http://oss.jfrog.org/oss-snapshot-local/com/alternacraft/PvpTitles/](http://oss.jfrog.org/oss-snapshot-local/com/alternacraft/PvpTitles/ "dev builds")

The dev builds are primarily for testing purposes.

Getting Started & Documentation
-------------------------------

All documentation is available on the [Wiki page](https://github.com/AlternaCraft/PvpTitles/wiki).

Feel free to contribute there too. Just click the edit button after you login into your Github account.


### Compatibility
This plugin is compatible with all Minecraft versions above 1.7.10

Also, there are some optional dependencies for having a better experience with the plugin:
* ScoreboardStats (http://dev.bukkit.org/bukkit-plugins/scoreboardstats)
* Vault (http://dev.bukkit.org/bukkit-plugins/vault)
* HolographicDisplays & ProtocolLib (http://dev.bukkit.org/bukkit-plugins/holographic-displays/)
* Placeholder API (https://www.spigotmc.org/resources/placeholderapi.6245/)
* MVdWPlaceholderAPI (https://www.spigotmc.org/resources/mvdwplaceholderapi.11182/)
* VanishNoPacket (https://dev.bukkit.org/projects/vanish/)

You don't need to do anything for getting it works together. It is automatically hooked.


### Building
PvpTitles uses Maven 3 to manage building configurations, general project informations and dependencies. You can compile this project yourself by using Maven.

* Just import the project from [Github](http://github.com/).
  Your IDE would detect the Maven project.
* If not: You can download it from [here](http://maven.apache.org/download.cgi)
  You will find executable in the bin folder.
* Run (using IDE, console or something else)

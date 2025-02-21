package com.palmergames.bukkit.towny;

import com.palmergames.bukkit.towny.command.BaseCommand;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.townblockstatus.NationZoneTownBlockStatusEvent;
import com.palmergames.bukkit.towny.exceptions.KeyAlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.ResidentList;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.PlayerCache.TownBlockStatus;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.tasks.TeleportWarmupTimerTask;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.util.MathUtil;

import io.papermc.lib.PaperLib;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Towny's class for external API Methods
 * For more dynamic/controlled changing of Towny's behavior, for example Database, War, Permissions
 * The {@link TownyUniverse} class should be used. It contains the map of all objects
 * aswell as serving as an internal API, that Towny uses.
 * @author Articdive
 */
public class TownyAPI {
    private static TownyAPI instance;
    private final Towny towny;
    private final TownyUniverse townyUniverse;
    
    private TownyAPI() {
        towny = Towny.getPlugin();
        townyUniverse = TownyUniverse.getInstance();
    }
    
    /**
     * Gets the town spawn {@link Location} of a {@link Player}.
     *
     * @param player {@link Player} of which you want the town spawn.
     * @return {@link Location} of the town spawn or if it is not obtainable null.
     */
    @Nullable
    public Location getTownSpawnLocation(Player player) {
    	Resident resident = townyUniverse.getResident(player.getUniqueId());
    	
    	if (resident == null)
    		return null;
    	
        if (resident.hasTown())
			return resident.getTownOrNull().getSpawnOrNull();

		return null;
    }
    
    /**
     * Gets the nation spawn {@link Location} of a {@link Player}.
     *
     * @param player {@link Player} of which you want the nation spawn.
     * @return {@link Location} of the nation spawn or if it is not obtainable null.
     */
    @Nullable
    public Location getNationSpawnLocation(Player player) {
		Resident resident = townyUniverse.getResident(player.getUniqueId());
		
		if (resident == null)
			return null;
		
        try {
            if (resident.hasTown()) {
            	Town t = resident.getTown();
            	if (t.hasNation()) {
					Nation nation = t.getNation();
					return nation.getSpawn();
				}
			}
        } catch (TownyException ignore) {
        }

		return null;
    }
 
    /**
     * Gets the resident's town if they have one.
     * 
     * @param resident Resident to get the town from.
     * @return The resident's Town or null if they have none.
     */
    @Nullable
    public Town getResidentTownOrNull(Resident resident) {
    	return resident.getTownOrNull();
    }
    
    /**
     * Gets the resident's nation if they have one.
     * 
     * @param resident Resident to get the nation from.
     * @return The resident's Nation or null if they have none.
     */
    @Nullable
    public Nation getResidentNationOrNull(Resident resident) {
    	if (resident.hasNation())
    		return resident.getTownOrNull().getNationOrNull();    	
    	return null;
    }
    
    /**
     * Gets the town's nation if they have one.
     * 
     * @param town Town to get the nation from.
     * @return The town's Nation or null if they have none.
     */
    @Nullable
    public Nation getTownNationOrNull(Town town) {
    	return town.getNationOrNull();
    }
    
    /**
     * Gets the nation from the given UUID.
     * @param uuid UUID of the nation.
     * @return nation or null if it doesn't exist.
     */
    @Nullable
    public Nation getNation(UUID uuid) {
    	return TownyUniverse.getInstance().getNation(uuid);
    }
    
    /**
     * Gets the town from the given UUID.
     * @param uuid UUID name of the town.
     * @return town or null if it doesn't exist.
     */
    @Nullable
    public Town getTown(UUID uuid) {
    	return TownyUniverse.getInstance().getTown(uuid);
    }
    
    /**
     * Gets the resident from the given UUID.
     * @param uuid UUID name of the resident.
     * @return resident or null if it doesn't exist.
     */
    @Nullable
    public Resident getResident(UUID uuid) {
    	return TownyUniverse.getInstance().getResident(uuid);
    }  
    
    /**
     * Gets the resident from the given Player.
     * 
     * @param player Player to get the resident from.
     * @return resident or null if it doesn't exist.
     */
    @Nullable
    public Resident getResident(Player player) {
    	return getResident(player.getUniqueId());
    }

    /**
     * Gets the nation from the given name.
     * @param name String name of the nation.
     * @return nation or null if it doesn't exist.
     */
    @Nullable
    public Nation getNation(String name) {
    	return TownyUniverse.getInstance().getNation(name);
    }
    
    /**
     * Gets the town from the given name.
     * @param name String name of the town.
     * @return town or null if it doesn't exist.
     */
    @Nullable
    public Town getTown(String name) {
    	return TownyUniverse.getInstance().getTown(name);
    }
    
    /**
     * Gets the resident from the given name.
     * @param name String name of the resident.
     * @return resident or null if it doesn't exist.
     */
    @Nullable
    public Resident getResident(String name) {
    	return TownyUniverse.getInstance().getResident(name);
    }
    
    /**
     * Find the the matching {@link Player} of the specified {@link Resident}.
     *
     * @param resident {@link Resident} of which you want the matching {@link Player}.
     * @return an online {@link Player} or if it's not obtainable.
     */
    public Player getPlayer(Resident resident) {
    	// NPCs are not players
    	if (resident.isNPC())
    		return null;
    	
    	Player player = null;
    	
    	if (resident.hasUUID())
    		player = BukkitTools.getPlayer(resident.getUUID());
    	
    	// Some servers use cross-platform proxies / offline mode where UUIDs may not be accurate. 
    	if (player == null)
    		player = BukkitTools.getPlayerExact(resident.getName());
    	
        return player;
    }
    
    /**
     * Find the {@link UUID} for the matching {@link Player} of the specified {@link Resident}.
     *
     * @param resident {@link Resident} of which you want the {@link UUID}.
     * @return an online {@link Player}'s {@link UUID} or null if it's not obtainable.
     */
    @Nullable
    public UUID getPlayerUUID(Resident resident) {
    	// NPCs are not players
    	if (resident.isNPC())
    		return null;
    	
    	// Use stored UUID if it exists
    	if (resident.hasUUID())
    		return resident.getUUID();
    	
    	Player player = BukkitTools.getPlayerExact(resident.getName());
    	
    	if (player != null)
    		return player.getUniqueId();
        
        return null;
    }
    
    /**
     * Gets all online {@link Player}s for a specific {@link ResidentList}.
     *
     * @param owner {@link ResidentList} of which you want all the online {@link Player}s.
     * @return {@link List} of all online {@link Player}s in the specified {@link ResidentList}.
     */
    public List<Player> getOnlinePlayers(ResidentList owner) {
        return Bukkit.getOnlinePlayers().stream().filter(player -> owner.hasResident(player.getName())).collect(Collectors.toList());
    }
    
    /**
     * Gets all online {@link Player}s for a specific {@link Town}.
     * 
     * @param town {@link Town} of which you want all the online {@link Player}s.
     * @return {@link List} of all online {@link Player}s in the specified {@link Town}.
     */
    public List<Player> getOnlinePlayersInTown(Town town){
    	return getOnlinePlayers(town);
    }

    /**
     * Gets all online {@link Player}s for a specific {@link Nation}.
     * 
     * @param nation {@link Nation} of which you want all the online {@link Player}s.
     * @return {@link List} of all online {@link Player}s in the specified {@link Nation}.
     */
    public List<Player> getOnlinePlayersInNation(Nation nation){
    	return getOnlinePlayers(nation);
    }
    
    /** 
     * Gets all online {@link Player}s for a specific {@link Nation}s alliance.
     * 
     * @param nation {@link Nation} of which you want all the online allied {@link Player}s.
     * @return {@link List} of all online {@link Player}s in the specified {@link Nation}s allies.
     */
    public List<Player> getOnlinePlayersAlliance(Nation nation) {
		ArrayList<Player> players = new ArrayList<>(getOnlinePlayers(nation));
        if (!nation.getAllies().isEmpty()) {
			for (Nation nations : nation.getAllies()) {
				players.addAll(getOnlinePlayers(nations));
			}
        }
        return players;
    }
    
    /**
     * Check if the specified {@link Block} is in the wilderness.
     *
     * @param block {@link Block} to test for.
     * @return true if the {@link Block} is in the wilderness, false otherwise.
     */
    public boolean isWilderness(Block block) {
        return isWilderness(block.getLocation());
    }
    
    /**
     * Check if the specified {@link Location} is in the wilderness.
     *
     * @param location {@link Location} to test widlerness for.
     * @return true if the {@link Location} is in the wilderness, false otherwise.
     */
    public boolean isWilderness(Location location) {
        return isWilderness(WorldCoord.parseWorldCoord(location));
    }
    
    /**
     * Check if the specified {@link WorldCoord} is in the wilderness.
     *
     * @param worldCoord {@link WorldCoord} to test widlerness for.
     * @return true if the {@link WorldCoord} is in the wilderness, false otherwise.
     */
    public boolean isWilderness(WorldCoord worldCoord) {
        
		if (worldCoord.hasTownBlock() && worldCoord.getTownBlockOrNull().hasTown())
			return false;

		// Must be wilderness
		return true;
    }    
    
	/**
	 * Answers whether Towny considers PVP enabled at a location.
	 * 
	 * @param location Location to check for PVP.
	 * @return true if PVP is enabled or this isn't a world with Towny enabled.
	 */
	public boolean isPVP(Location location) {
		return !isTownyWorld(location.getWorld()) || CombatUtil.preventPvP(getTownyWorld(location.getWorld().getName()), getTownBlock(location));
	}

    /**
     * Returns value of usingTowny for the given world.
     * 
     * @param world - the world to check
     * @return true or false
     */
    public boolean isTownyWorld(World world) {
    	try {
			return townyUniverse.getDataSource().getWorld(world.getName()).isUsingTowny();
		} catch (NotRegisteredException e) {}
    	return false;
    }
    
    /**
     * Returns {@link TownyWorld} unless it is null.
     * 
     * @param worldName - the name of the world to get.
     * @return TownyWorld or {@code null}.
     */
    @Nullable
    public TownyWorld getTownyWorld(String worldName) {
    	try {
			return townyUniverse.getDataSource().getWorld(worldName);
    	} catch (NotRegisteredException ignored) {
    		return null;
		}
    }
    
    /**
     * Returns {@link TownyWorld} unless it is null.
     * 
     * @param world - the world to get.
     * @return TownyWorld or {@code null}.
     */
    @Nullable
    public TownyWorld getTownyWorld(World world) {
    	return getTownyWorld(world.getName());
    }
    
    /**
     * Get the {@link Town} at a specific {@link Location}.
     *
     * @param location {@link Location} to get {@link Town} for.
     * @return {@link Town} at this location, or {@code null} for none.
     */
    @Nullable
    public Town getTown(Location location) {
        WorldCoord worldCoord = WorldCoord.parseWorldCoord(location);
		return worldCoord.getTownOrNull();
    }
    
    /**
     * Get the {@link Town} of a {@link TownBlock} or null.
     * Should be used after testing TownBlock.hasTown().
     * 
     * @param townBlock {@link TownBlock} from which to get a {@link Town}.
     * @return {@link Town} or {@code null}. 
     */
    @Nullable
    public Town getTownOrNull(TownBlock townBlock) {
    	return townBlock.getTownOrNull();
    }
    
    /**
     * Get the {@link Resident} who owns the {@link TownBlock} or null.
     * Resident will be returned if the TownBlock is owned by a player.
     * Should be used after testing TownBlock.hasResident().
     * 
     * @param townBlock {@link TownBlock} from which to get a {@link Resident}.
     * @return {@link Resident} or {@code null}. 
     */
    @Nullable
    public Resident getResidentOrNull(TownBlock townBlock) {
    	return townBlock.getResidentOrNull();
    }
    
    /**
     * Get the name of a {@link Town} at a specific {@link Location}.
     *
     * @param location {@link Location} to get {@link Town} name for.
     * @return {@link String} containg the name of the {@link Town} at this location, or {@code null} for none.
     */
    @Nullable
    public String getTownName(Location location) {
    	Town town = getTown(location);
    	return town != null ? town.getName() : null;
    }
    
    
    /**
     * Get the {@link UUID} of a {@link Town} at the specified {@link Location}.
     *
     * @param location {@link Location} to get {@link Town} {@link UUID} for.
     * @return {@link UUID} of any {@link Town} at this {@link Location}, or {@code null} for none.
     */
    @Nullable
    public UUID getTownUUID(Location location) {
    	Town town = getTown(location);
    	return town != null ? town.getUUID() : null;
    }
    
    /**
     * Get the {@link TownBlock} at a specific {@link Location}.
     *
     * @param location {@link Location} to get {@link TownBlock} of.
     * @return {@link TownBlock} at this {@link Location}, or {@code null} for none.
     */
    @Nullable
    public TownBlock getTownBlock(Location location) {
        WorldCoord worldCoord = WorldCoord.parseWorldCoord(location);
		return worldCoord.getTownBlockOrNull();
    }
    
    /**
     * Get the {@link TownBlock} in which a {@link Player} is located.
     *
     * @param player {@link Player} to get {@link TownBlock} of.
     * @return {@link TownBlock} at the location of this {@link Player}, or {@code null} when the player is in the wilderness.
     */
    @Nullable
    public TownBlock getTownBlock(@NotNull Player player) {
		return WorldCoord.parseWorldCoord(player.getLocation()).getTownBlockOrNull();
    }
    
    /** 
     * Get the {@link TownBlock} at a specific {@link WorldCoord}.
     * 
     * @param wc {@link WorldCoord} to get the {@link TownBlock} of (if it claimed by a town.)
     * @return {@link TownBlock} at this {@link WorldCoord}, or {@code null} if this isn't claimed.
     */
    @Nullable
    public TownBlock getTownBlock(WorldCoord wc) {
    	return wc.getTownBlockOrNull();
    }

	/**
	 * Get a list of active {@link Resident}s.
	 *
	 * @return {@link List} of active {@link Resident}s.
	 * @deprecated This is deprecated as of 0.97.2.6, and will be removed in a future release.
	 */
	@Deprecated
	public List<Resident> getActiveResidents() {
		return new ArrayList<>(townyUniverse.getResidents());
	}

	/**
	 * Check if the specified {@link Resident} is an active Resident.
	 *
	 * @param resident {@link Resident} to test for activity.
	 * @return true if the player is active, false otherwise.
	 * @deprecated This is deprecated as of 0.97.2.6, and will be removed in a future release.
	 */
	@Deprecated
	public boolean isActiveResident(Resident resident) {
		return resident.isOnline();
	}
    
    /**
     * Gets Towny's saving Database
     *
     * @return the {@link TownyDataSource}
     */
    public TownyDataSource getDataSource() {
        return townyUniverse.getDataSource();
    }
    
    /**
     * Check which {@link Resident}s are online in a {@link ResidentList}
     *
     * @param owner {@link ResidentList} to check for online {@link Resident}s.
     * @return {@link List} of {@link Resident}s that are online.
     */
    public List<Resident> getOnlineResidents(ResidentList owner) {
        
        List<Resident> onlineResidents = new ArrayList<>();
        for (Player player : BukkitTools.getOnlinePlayers()) {
            if (player != null)
                for (Resident resident : owner.getResidents()) {
                    if (resident.getName().equalsIgnoreCase(player.getName()))
                        onlineResidents.add(resident);
                }
        }
        return onlineResidents;
    }
    
    /**
     * Teleports the Player to the specified jail {@link Location}.
     *
     * @param player   {@link Player} to be teleported to jail.
     * @param location {@link Location} of the jail to be teleported to.
	 * @deprecated Since 0.97.3.0 use {@link com.palmergames.bukkit.towny.utils.SpawnUtil#jailTeleport(Resident)} or {@link com.palmergames.bukkit.towny.utils.SpawnUtil#jailAwayTeleport(Resident)} instead.
     */
	@Deprecated
    public void jailTeleport(final Player player, final Location location) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(towny, () -> PaperLib.teleportAsync(player, location, PlayerTeleportEvent.TeleportCause.PLUGIN),
			(long) TownySettings.getTeleportWarmupTime() * 20);
    }
    
    public void requestTeleport(Player player, Location spawnLoc) {
    	Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
    	
    	if (resident != null) {
			TeleportWarmupTimerTask.requestTeleport(resident, spawnLoc);
		}
    }
    
    public void abortTeleportRequest(Resident resident) {
        
        TeleportWarmupTimerTask.abortTeleportRequest(resident);
    }
    
    public void registerCustomDataField(CustomDataField<?> field) throws KeyAlreadyRegisteredException {
    	townyUniverse.addCustomCustomDataField(field);
	}

    /**
     * Method to figure out if a location is in a NationZone.
     * 
     * @param location - Location to test.
     * @return true if the location is in a NationZone.
     */
    public boolean isNationZone(Location location) {
    	if (!isWilderness(location))
    		return false;
    	TownBlockStatus status = hasNationZone(location);
    	if (status.equals(TownBlockStatus.NATION_ZONE))
    		return true;
    	
    	return false;
    }
    /**
     * Method to figure out if a location in the wilderness is normal wilderness of nation zone.
     * Recommended to use {@link TownyAPI#isWilderness(Location)} prior to using this, to confirm the location is not in a town.  
     * 
     * @param location - Location to test whether it is a nation zone or normal wilderness.
     * @return returns either UNCLAIMED_ZONE or NATION_ZONE
     */
    public TownBlockStatus hasNationZone(Location location) {
    	
    	return hasNationZone(WorldCoord.parseWorldCoord(location));
    }

    /**
     * Method to figure out if a worldcoord in the wilderness is normal wilderness of nation zone.
     * Recommended to use {@link TownyAPI#isWilderness(WorldCoord)} prior to using this, to confirm the location is not in a town.  
     * 
     * @param worldCoord - WorldCoord to test whether it is a nation zone or normal wilderness.
     * @return returns either UNCLAIMED_ZONE or NATION_ZONE
     */
    public TownBlockStatus hasNationZone(WorldCoord worldCoord) {
    	
		final TownBlock nearestTownblock = TownyAPI.getInstance().getTownyWorld(worldCoord.getWorldName()).getClosestTownblockWithNationFromCoord(worldCoord);
		
		if (nearestTownblock == null)
			return TownBlockStatus.UNCLAIMED_ZONE;
		
		Town nearestTown = nearestTownblock.getTownOrNull();
		
		// Safety validation, both these cases should never occur.
		if (nearestTown == null || !nearestTown.hasNation())
			return TownBlockStatus.UNCLAIMED_ZONE;
		
		// This nation zone system can be disabled during wartime.
		if (TownySettings.getNationZonesWarDisables() && nearestTown.getNationOrNull().hasActiveWar())
			return TownBlockStatus.UNCLAIMED_ZONE;

		// It is possible to only have nation zones surrounding nation capitals. If this is true, we treat this like a normal wilderness.
		if (!nearestTown.isCapital() && TownySettings.getNationZonesCapitalsOnly())
			return TownBlockStatus.UNCLAIMED_ZONE;
		
		// Even after checking for having a nation, and whether it might need to be a capital,
		// towns can disable their nation zone manually.
		if (!nearestTown.isNationZoneEnabled())
			return TownBlockStatus.UNCLAIMED_ZONE;
		
		int distance = (int) MathUtil.distance(worldCoord.getX(), nearestTownblock.getX(), worldCoord.getZ(), nearestTownblock.getZ());
		int nationZoneRadius = nearestTown.getNationZoneSize();

		if (distance <= nationZoneRadius) {
			NationZoneTownBlockStatusEvent event = new NationZoneTownBlockStatusEvent(nearestTown);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled())
				return TownBlockStatus.UNCLAIMED_ZONE;
			
			return TownBlockStatus.NATION_ZONE;
		}
		
		return TownBlockStatus.UNCLAIMED_ZONE;
	}

	public static void addTranslations(Plugin plugin, Map<String, Map<String, String>> translations) {
		Translation.addTranslations(translations);
		Towny.getPlugin().getLogger().info("Loaded additional language files for plugin: " + plugin.getName());
	}
    
    public static TownyAPI getInstance() {
        if (instance == null) {
            instance = new TownyAPI();
        }
        return instance;
    }
    
	/**
	 * Returns a List&lt;String&gt; containing strings of resident, town, and/or
	 * nation names that match with arg. Can check for multiple types, for example
	 * "rt" would check for residents and towns but not nations or worlds. Useful
	 * for tab completion systems calling for Towny Objects.
	 *
	 * @param arg  the string to match with the chosen type
	 * @param type the type of Towny object to check for, can be r(esident), t(own),
	 *             n(ation), w(orld), or any combination of those to check.
	 * @return Matches for the arg with the chosen type
	 */
	public static List<String> getTownyObjectStartingWith(String arg, String type) {
		return BaseCommand.getTownyStartingWith(arg, type);
	}

	/**
	 * Checks if arg starts with filters, if not returns matches from
	 * {@link #getTownyObjectStartingWith(String, String)}. Add a "+" to the type to
	 * return both cases. Useful for tab completion systems.
	 *
	 * @param filters the strings to filter arg with
	 * @param arg     the string to check with filters and possibly match with Towny
	 *                objects if no filters are found
	 * @param type    the type of check to use, see
	 *                {@link #getTownyObjectStartingWith(String, String)} for possible
	 *                types. Add "+" to check for both filters and
	 *                {@link #getTownyObjectStartingWith(String, String)}
	 * @return Matches for the arg filtered by filters or checked with type
	 */
	public static List<String> filterByStartOrGetTownyObjectStartingWith(List<String> filters, String arg, String type) {
		return BaseCommand.filterByStartOrGetTownyStartingWith(filters, arg, type);
	}

	/**
	 * Returns the names a player's town's residents that start with a string.
	 * Useful for tab completion systems.
	 *
	 * @param player the player to get the town's residents of
	 * @param str the string to check if the town's residents start with
	 * @return the resident names that match str
	 */
	public static List<String> getTownResidentNamesOfPlayerStartingWith(Player player, String str){
		return BaseCommand.getTownResidentNamesOfPlayerStartingWith(player, str);
	}

	/**
	 * Returns the names a town's residents that start with a string.
	 * Useful for tab completion systems.
	 *
	 * @param townName the town to get the residents of
	 * @param str the string to check if the town's residents start with
	 * @return the resident names that match str
	 */
	public static List<String> getResidentsOfTownStartingWith(String townName, String str) {
		return BaseCommand.getResidentsOfTownStartingWith(townName, str);
	}
	
	/**
	 * Returns a list of residents which are online and have no town.
	 * Useful for tab completion systems.
	 * 
	 * @param str the string to check if the resident's name starts with.
	 * @return the residents name or an empty list.
	 */
	public static List<String> getResidentsWithoutTownStartingWith(String str) {
		return BaseCommand.getResidentsWithoutTownStartingWith(str);
	}

	/**
     * @deprecated since 0.97.3.0 use {@link Town#hasActiveWar()} or {@link Nation#hasActiveWar()} instead.
     * @return false.
     */
    @Deprecated
    public boolean isWarTime() {
    	return false;
    }
}

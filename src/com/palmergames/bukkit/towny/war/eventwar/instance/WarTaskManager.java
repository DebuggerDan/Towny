package com.palmergames.bukkit.towny.war.eventwar.instance;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.scheduler.BukkitScheduler;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.war.eventwar.ServerBroadCastTimerTask;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.util.Colors;
import com.palmergames.util.TimeMgmt;
import com.palmergames.util.TimeTools;

public class WarTaskManager {
	
	private War war;
	private List<Integer> warTaskIds = new ArrayList<>();
	
	public WarTaskManager(War war) {
		this.war = war;
	}

	/*
	 * Task Related
	 */
	public List<Integer> getTaskIds() {

		return new ArrayList<>(warTaskIds);
	}
	
	public void addTaskId(int id) {

		warTaskIds.add(id);
	}

	public void clearTaskIds() {

		warTaskIds.clear();
	}

	public void cancelTasks(BukkitScheduler scheduler) {

		for (Integer id : getTaskIds())
			scheduler.cancelTask(id);
		clearTaskIds();
	}
	
	/**
	 * When Townblocks have HP the WarTimerTask will make the
	 * healing and damaging of plots possible.
	 * 
	 * @param plugin Towny instance.
	 */
	public void scheduleWarTimerTask(Towny plugin) {

		int id = BukkitTools.scheduleAsyncRepeatingTask(new WarTimerTask(plugin, war), 0, TimeTools.convertToTicks(5));
		if (id == -1) {
			war.addErrorMsg("Could not schedule war event loop.");
			war.end(false);
		} else {
			addTaskId(id);
		}
	}
	
	/**
	 * Creates a delay before war begins
	 * @param delay - Delay before war begins
	 */
	public void setupDelay(int delay) {

		if (delay <= 0)
			war.start();
		else {
			// Create a countdown timer
			for (Long t : TimeMgmt.getCountdownDelays(delay, TimeMgmt.defaultCountdownDelays)) {
				int id = BukkitTools.scheduleAsyncDelayedTask(new ServerBroadCastTimerTask(war, Colors.Red + Translation.of("war_starts_in_x", TimeMgmt.formatCountdownTime(t))), TimeTools.convertToTicks(delay - t));
				if (id == -1) {
					war.addErrorMsg("Could not schedule a countdown message for war event.");
					war.end(false);
				} else
					addTaskId(id);
			}
			// Schedule set up delay
			int id = BukkitTools.scheduleAsyncDelayedTask(() -> war.start(), TimeTools.convertToTicks(delay));
			if (id == -1) {
				war.addErrorMsg("Could not schedule setup delay for war event.");
				war.end(false);
			} else {
				addTaskId(id);
			}
		}
	}

	public void teamSelectionDelay(int delay) {
		if (delay <= 0)
			war.preStart();
		else {
			// Create countdown.
			for (Long t : TimeMgmt.getCountdownDelays(delay, TimeMgmt.defaultCountdownDelays)) {
				int id = BukkitTools.scheduleAsyncDelayedTask(new ServerBroadCastTimerTask(war, Colors.Red + Translation.of("msg_team_selection_delay_seconds_remaining", TimeMgmt.formatCountdownTime(t))), TimeTools.convertToTicks(delay - t));
				if (id != -1)
					addTaskId(id);
			}
			// Set up delayed preStart.
			int id = BukkitTools.scheduleAsyncDelayedTask(() -> war.preStart(), TimeTools.convertToTicks(delay));
			if (id == -1) {
				war.addErrorMsg("Could not schedule post-team-selection delay for war event.");
				war.end(false);
			} else {
				addTaskId(id);
			}
		}
	}

}

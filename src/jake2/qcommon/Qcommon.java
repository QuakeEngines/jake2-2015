/*
 * Qcommon.java
 * Copyright 2003
 * 
 * $Id: Qcommon.java,v 1.14 2004-01-31 16:56:11 rst Exp $
 */
/*
Copyright (C) 1997-2001 Id Software, Inc.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package jake2.qcommon;

import jake2.Globals;
import jake2.client.*;
import jake2.game.Cmd;
import jake2.game.Swap;
import jake2.server.SV;
import jake2.server.SV_MAIN;
import jake2.sys.NET;
import jake2.sys.Sys;
import jake2.util.Vargs;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Qcommon contains some  basic routines for the game engine
 * namely initialization, shutdown and frame generation.
 */
public final class Qcommon {

	/**
	 * This function initializes the different subsystems of
		 * the game engine. The setjmp/longjmp mechanism of the original
		 * was replaced with exceptions.
	 * @param args the original unmodified command line arguments
	 */
	public static void Init(String[] args) {
		try {
			Z.chain.next= Z.chain.prev= Z.chain;

			// prepare enough of the subsystems to handle
			// cvar and command buffer management
			Com.InitArgv(args);

			Swap.Init();
			Cbuf.Init();

			Cmd.Init();
			Cvar.Init();

			Key.Init();

			// we need to add the early commands twice, because
			// a basedir or cddir needs to be set before execing
			// config files, but we want other parms to override
			// the settings of the config files
			Cbuf.AddEarlyCommands(false);
			Cbuf.Execute();

			FS.InitFilesystem();

			Cbuf.AddText("exec default.cfg\n");
			Cbuf.AddText("exec config.cfg\n");

			Cbuf.AddEarlyCommands(true);
			Cbuf.Execute();

			//
			// init commands and vars
			//
			Cmd.AddCommand("z_stats", Z.Stats_f);
			Cmd.AddCommand("error", Com.Error_f);

			Globals.host_speeds= Cvar.Get("host_speeds", "0", 0);
			Globals.log_stats= Cvar.Get("log_stats", "0", 0);
			Globals.developer= Cvar.Get("developer", "0", 0);
			Globals.timescale= Cvar.Get("timescale", "1", 0);
			Globals.fixedtime= Cvar.Get("fixedtime", "0", 0);
			Globals.logfile_active= Cvar.Get("logfile", "0", 0);
			Globals.showtrace= Cvar.Get("showtrace", "0", 0);
			Globals.dedicated= Cvar.Get("dedicated", "0", Cvar.NOSET);

			String s = Com.sprintf("%4.2f %s %s %s",
					new Vargs(4)
						.add(Globals.VERSION)
						.add(Globals.CPUSTRING)
						.add(Globals.__DATE__)
						.add(Globals.BUILDSTRING));

			Cvar.Get("version", s, Cvar.SERVERINFO | Cvar.NOSET);

			NET.NET_Init();
			Netchan.Init();

			SV.Init();
			CL.Init();

			// add + commands from command line
			if (!Cbuf.AddLateCommands()) {
				// if the user didn't give any commands, run default action
				Cbuf.AddText("d1\n");
				Cbuf.Execute();
			} else {
				// the user asked for something explicit
				// so drop the loading plaque
				SCR.EndLoadingPlaque();
			}

			Com.Printf("====== Quake2 Initialized ======\n\n");

		} catch (longjmpException e) {
			Sys.Error("Error during initialization");
		}
	}

	/**
	 * Trigger generation of a frame for the given time. The setjmp/longjmp
	 * mechanism of the original was replaced with exceptions.
	 * @param msec the current game time
	 */
	public static void Frame(long msec) {
		try {

			if (Globals.log_stats.modified) {
				Globals.log_stats.modified= false;

				if (Globals.log_stats.value != 0.0f) {

					if (Globals.log_stats_file != null) {
						try {
							Globals.log_stats_file.close();
						} catch (IOException e) {
						}
						Globals.log_stats_file= null;
					}

					try {
						Globals.log_stats_file= new FileWriter("stats.log");
					} catch (IOException e) {
						Globals.log_stats_file= null;
					}
					if (Globals.log_stats_file != null) {
						try {
							Globals.log_stats_file.write("entities,dlights,parts,frame time\n");
						} catch (IOException e) {
						}
					}

				} else {

					if (Globals.log_stats_file != null) {
						try {
							Globals.log_stats_file.close();
						} catch (IOException e) {
						}
						Globals.log_stats_file= null;
					}
				}
			}

			if (Globals.fixedtime.value != 0.0f) {
				msec= (long) Globals.fixedtime.value;
			} else if (Globals.timescale.value != 0.0f) {
				msec *= Globals.timescale.value;
				if (msec < 1)
					msec= 1;
			}

			if (Globals.showtrace.value != 0.0f) {
				Com.Printf("%4i traces  %4i points\n",
					new Vargs(2).add(Globals.c_traces)
								.add(Globals.c_pointcontents));

				
				Globals.c_traces= 0;
				Globals.c_brush_traces= 0;
				Globals.c_pointcontents= 0;
			}

			Cbuf.Execute ();

			long time_before= 0;
			long time_between= 0;
			long time_after= 0;

			if (Globals.host_speeds.value != 0.0f)
				time_before= Sys.Milliseconds();

			SV_MAIN.SV_Frame(msec);

			if (Globals.host_speeds.value != 0.0f)
				time_between= Sys.Milliseconds();

			CL.Frame(msec);

			if (Globals.host_speeds.value != 0.0f) {
				time_after= Sys.Milliseconds();

				long all= time_after - time_before;
				long sv= time_between - time_before;
				long cl= time_after - time_between;
				long gm= Globals.time_after_game - Globals.time_before_game;
				long rf= Globals.time_after_ref - Globals.time_before_ref;
				sv -= gm;
				cl -= rf;

				Com.Printf("all:%3i sv:%3i gm:%3i cl:%3i rf:%3i\n",
					new Vargs(5).add(all).add(sv).add(gm).add(cl).add(rf));
			}

		} catch (longjmpException e) {
		}
	}

}

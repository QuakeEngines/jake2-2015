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

// Created on 27.11.2003 by RST.
// $Id: centity_t.java,v 1.3 2004-01-31 16:56:11 rst Exp $

package jake2.client;

import jake2.game.entity_state_t;

public class centity_t {
	entity_state_t baseline= new entity_state_t(); // delta from this if not from a previous frame
	entity_state_t current= new entity_state_t();
	entity_state_t prev= new entity_state_t(); // will always be valid, but might just be a copy of current

	int serverframe; // if not current, this ent isn't in the frame

	int trailcount; // for diminishing grenade trails
	float[] lerp_origin = { 0, 0, 0 }; // for trails (variable hz)

	int fly_stoptime;
}

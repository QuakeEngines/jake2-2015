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

// Created on 28.11.2003 by RST.
// $Id: sfxcache_t.java,v 1.2 2003-11-29 13:28:29 rst Exp $

package jake2.client;

public class sfxcache_t {
	int 		length;
	int 		loopstart;
	int 		speed;			// not needed, because converted on load?
	int 		width;
	int 		stereo;
	byte		data[];		// variable sized

}
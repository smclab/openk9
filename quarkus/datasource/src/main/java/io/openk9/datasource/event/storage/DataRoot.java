/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.event.storage;


import java.util.ArrayList;
import java.util.List;

public class DataRoot {

	private List<Event> events = new ArrayList<>();

	public DataRoot() {
	}

	public void addEvent(Event p) {
		this.getEvents().add(p);
	}

	public List<Event> getEvents() {
		if (events == null) {
			events = new ArrayList<>();
		}
		// must return the reference
		// in order to make it work with MicroStream
		return events;
	}

	public Event getEventAt(int index) {
		return getEvents().get(index);
	}

}

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

import io.quarkus.arc.Lock;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Lock
@ApplicationScoped
public class EventStorageRepository {

    @Inject
    SimplePersistenceManager spm;

    public void storeEvent(Event event) {
        event.setId(getId());
        spm.getRoot().getEvents().add(event);
        spm.store(spm.getRoot().getEvents());
    }

    public void storeEvents(List<Event> events) {
        for (Event p : events) {
            p.setId(getId());
        }
        spm.getRoot().getEvents().addAll(events);
        spm.store(spm.getRoot().getEvents());
    }

    public boolean removeEvent(Event event) {
        boolean isRemoved = spm.getRoot().getEvents().remove(event);
        spm.store(spm.getRoot().getEvents());
        return isRemoved;
    }

    public boolean removeEventById(UUID id) {
        boolean isRemoved = spm.getRoot().getEvents().removeIf(p -> p.getId().equals(id));
        spm.store(spm.getRoot().getEvents());
        return isRemoved;
    }

    public List<Event> getEvents(int from, int limit) {
        return getEvents()
            .stream()
            .skip(from)
            .limit(limit)
            .collect(Collectors.toList());
    }

    public List<Event> getEvents() {
        return spm.getRoot().getEvents();
    }

    public LocalDateTime getLastParsingDate(
        String type, String classPk, String className) {
        return getEvents()
            .stream()
            .filter(p -> p.getType().equals(type) && p.getClassPK().equals(classPk) && p.getClassName().equals(className))
            .map(Event::getParsingDate)
            .max(LocalDateTime::compareTo)
            .orElse(null);
    }

    public Optional<Event> updateEvent(Event event) {

        Optional<Event> foundEventOptional = spm
            .getRoot()
            .getEvents()
            .stream()
            .filter(p -> p.getId().equals(event.getId()))
            .findFirst();

        if (foundEventOptional.isEmpty()) {
            return foundEventOptional;
        }

        Event foundEvent = foundEventOptional.get();

        eventMapper.updateEvent(event, foundEvent);
        // store back ref
        spm.store(foundEvent);

        return foundEventOptional;
    }

    public Optional<Event> getEventById(UUID id) {
        return spm.getRoot().getEvents().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    private UUID getId() {
        return UUID.randomUUID();
    }

    @Inject
    EventMapper eventMapper;

}
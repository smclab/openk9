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
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.nio.file.Paths;

@Lock
@ApplicationScoped
public class SimplePersistenceManager {

    @ConfigProperty(
        name = "openk9.microstream.storage.dir",
        defaultValue = "./storage"
    )
    String storageDir;

    private EmbeddedStorageManager storageManager;

    private SimplePersistenceManager() {
    }

    @PostConstruct
    void init() {
        this.storageManager = EmbeddedStorage.start(
            new DataRoot(), Paths.get(storageDir));
    }

    @PreDestroy
    void shutdown() {
        storageManager.shutdown();
    }

    public void store(Object instance) {
        storageManager.store(instance);
    }

    public void storeRoot() {
        storageManager.storeRoot();
    }

    public DataRoot getRoot() {
        return (DataRoot) storageManager.root();
    }

    public void setRoot(DataRoot root) {
        storageManager.setRoot(root);
    }

}
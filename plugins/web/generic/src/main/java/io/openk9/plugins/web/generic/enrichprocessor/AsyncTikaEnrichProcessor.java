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

package io.openk9.plugins.web.generic.enrichprocessor;

import io.openk9.model.BinaryPayload;
import io.openk9.model.IngestionPayload;
import io.openk9.search.enrich.api.AsyncEnrichProcessor;
import io.openk9.search.enrich.api.EnrichProcessor;
import org.osgi.service.component.annotations.Component;

import java.util.List;

@Component(immediate = true, service = EnrichProcessor.class)
public class AsyncTikaEnrichProcessor implements AsyncEnrichProcessor {

    @Override
    public String destinationName() {
        return "io.openk9.tika";
    }

    @Override
    public String name() {
        return AsyncTikaEnrichProcessor.class.getName();
    }

    @Override
    public boolean validate(IngestionPayload ingestionPayload) {

        List<BinaryPayload> binaries = ingestionPayload.getResources().getBinaries();
        return binaries != null && !binaries.isEmpty();
    }
}

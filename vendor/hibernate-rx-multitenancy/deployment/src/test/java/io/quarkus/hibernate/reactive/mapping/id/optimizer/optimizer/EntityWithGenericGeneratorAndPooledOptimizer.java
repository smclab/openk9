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

package io.quarkus.hibernate.reactive.mapping.id.optimizer.optimizer;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.OptimizableGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

@Entity
public class EntityWithGenericGeneratorAndPooledOptimizer {

    @Id
    @GeneratedValue(generator = "gen_gen_pooled_lo")
	@GenericGenerator(
		name = "gen_gen_pooled_lo", type = SequenceStyleGenerator.class,
		parameters = @Parameter(name = OptimizableGenerator.OPT_PARAM, value = "pooled")
	)
    Long id;

    public EntityWithGenericGeneratorAndPooledOptimizer() {
    }

}

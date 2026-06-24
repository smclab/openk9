/*
 * Copyright (C) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
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

package io.openk9.datasource.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level constraint for the {@code Highlight} entity that enforces the minimum
 * {@code fragmentSize} required by the FVH highlighter.
 * <p>
 * When the highlight {@code type} is {@code FVH}, the underlying Lucene
 * {@code FastVectorHighlighter} requires a {@code fragmentSize} of at least
 * {@code MIN_FVH_FRAGMENT_SIZE}; smaller values fail at query time. The other
 * highlighter types are not affected, so the check applies only when {@code type} is
 * {@code FVH}. A {@code null} {@code fragmentSize} is considered valid.
 */
@Constraint(validatedBy = {ValidHighlightFragmentSizeValidator.class})
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidHighlightFragmentSize {

	String message() default "When highlight type is FVH, fragmentSize must be at least 18";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}

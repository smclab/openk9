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
 * Class-level constraint for the {@code Highlight} entity that rejects a {@code boundaryScanner}
 * not supported by the chosen highlighter.
 * <p>
 * When the highlight {@code type} is {@code UNIFIED}, a {@code boundaryScanner} of {@code CHARS}
 * is not supported and fails at query time; the check therefore rejects that combination. The
 * other highlighter types and boundary scanner values are not affected. A {@code null}
 * {@code boundaryScanner} is considered valid.
 */
@Constraint(validatedBy = {ValidHighlightBoundaryScannerValidator.class})
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidHighlightBoundaryScanner {

	String message() default "When highlight type is UNIFIED, boundaryScanner CHARS is not supported";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}

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

package io.openk9.datasource.model.form;

import java.util.Collection;

import lombok.Builder;
import lombok.Singular;

/**
 * Represents a form template containing a collection of form fields for UI rendering.
 *
 * <p>This record defines the structure and configuration of a form by encapsulating
 * multiple form fields. Each field contains information about input types, validation
 * rules, default values, and display properties needed for proper form rendering.
 *
 * <p>Currently used in:
 * <ul>
 *   <li>FormConfiguration records for entity configuration forms</li>
 *   <li>Plugin driver JSON form rendering</li>
 * </ul>
 *
 * @param fields collection of form fields that define the form structure and behavior
 * @see FormField
 * @see FormConfigurations.FormConfiguration
 */
@Builder
public record FormTemplate(@Singular Collection<FormField> fields) {
}

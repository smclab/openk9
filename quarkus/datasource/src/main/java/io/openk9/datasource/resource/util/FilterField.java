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

package io.openk9.datasource.resource.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.jboss.logging.Logger;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

@Data
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor
public class FilterField {
   protected Operator operator;
   protected String fieldName;
   protected String value;
   protected boolean not = false;

   public Predicate generateCriteria(CriteriaBuilder builder, Path field) {
      return generateCriteria(builder, fieldName -> field);
   }

   public Predicate generateCriteria(
       CriteriaBuilder builder, Function<String, Path> fieldFactory) {

      Path field = fieldFactory.apply(fieldName);

      try {
         long v = Long.parseLong(value);
         switch (operator) {
            case lessThan: return _orNot(builder.lt(field, v));
            case lessThenOrEqualTo: return _orNot(builder.le(field, v));
            case greaterThan: return _orNot(builder.gt(field, v));
            case greaterThanOrEqualTo: return _orNot(builder.ge(field, v));
            case equal: return _orNot(builder.equal(field, v));
         }
      } catch (NumberFormatException ignore) {
         try {
            OffsetDateTime dateTime = OffsetDateTime.parse(value);
            switch (operator) {
               case lessThan: return _orNot(builder.lessThan(field, dateTime));
               case lessThenOrEqualTo: return _orNot(builder.lessThanOrEqualTo(field, dateTime));
               case greaterThan: return _orNot(builder.greaterThan(field, dateTime));
               case greaterThanOrEqualTo: return _orNot(builder.greaterThanOrEqualTo(field, dateTime));
               case equal: return _orNot(builder.equal(field, dateTime));
            }
         }
         catch (DateTimeParseException dtpe) {
            switch (operator) {
               case endsWith:
                  return _orNot(builder.like(field, "%" + value));
               case startsWith:
                  return _orNot(builder.like(field, value + "%"));
               case contains:
                  return _orNot(builder.like(field, "%" + value + "%"));
               case equal:
                  return _orNot(builder.equal(field, value));
            }
         }
      }

      return null;
   }

   private Predicate _orNot(Predicate predicate) {
      return not ? predicate.not() : predicate;
   }

   public enum Operator {
      lessThan, lessThenOrEqualTo, greaterThan, greaterThanOrEqualTo, equal,
      endsWith, startsWith, contains;
   }

   private static final Logger LOGGER = Logger.getLogger(FilterField.class);

}
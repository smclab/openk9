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

package io.openk9.search.query.internal.query.parser.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Itertools {

    @SafeVarargs
    public static <T> Iterable<List<T>> product(List<T>... lists) {
        int total = 1;
        int[] max = new int[lists.length];
        for (int i = 0; i < lists.length; i++) {
            max[i] = lists[i].size();
        }
        int[] initProduct = new int[lists.length];
        Arrays.fill(initProduct, 1);
        for (List<T> list :
                lists) {
            total *= list.size();
        }
        int finalTotal = total;
        return () -> new Iterator<>() {
            int index = -1;
            int[] presentProduct;

            @Override
            public boolean hasNext() {
                index++;
                return index < finalTotal;
            }

            @Override
            public List<T> next() {
				if (index == 0) {
					presentProduct = initProduct;
				}
				else {
                    PermutationGenerator.generateNextProduct(
                        presentProduct,
                        max);
                }
                List<T> result = new ArrayList<>();
                for (int i = 0; i < presentProduct.length; i++) {
                    result.add(lists[i].get(presentProduct[i] - 1));
                }
                return result;
            }
        };
    }
}
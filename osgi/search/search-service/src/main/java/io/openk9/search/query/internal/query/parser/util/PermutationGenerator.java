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
import java.util.List;

public class PermutationGenerator {
    public static <T> List<List<T>> generatePermutations(List<T> list, int r) {
        List<List<T>> result = new ArrayList<>();
        List<T> tempList;
        List<List<Integer>> mappings = CombinationGenerator.combination(r, list.size());
        List<List<Integer>> rPermutations = permutations(r);
        for (List<Integer> combination :
                mappings) {
            for (List<Integer> permutation :
                    rPermutations) {
                tempList = new ArrayList<>();
                for (int a :
                        permutation) {
                    tempList.add(list.get(combination.get(a - 1) - 1));
                }
                result.add(tempList);
            }
        }

        return result;
    }

    public static List<List<Integer>> permutations(int n) {
        List<List<Integer>> result = new ArrayList<>();
        int nfact = (int) factorial(n);
        List<Integer> tempList;
        int[] temp = new int[n];
        for (int i = 0; i < n; i++) {
            temp[i] = i + 1;
        }
        tempList = new ArrayList<>();
        int x;
        for (int i = 0; i < n; i++) {
            x = temp[i];
            tempList.add(x);
        }
        result.add(tempList);
        int m;
        int k;
        for (int i = 1; i < nfact; i++) {
            m = n - 1;
			while (temp[m - 1] > temp[m]) {
				m--;
			}
            k = n;
			while (temp[m - 1] > temp[k - 1]) {
				k--;
			}
            int swapVar;
            //swap m and k
            swapVar = temp[m - 1];
            temp[m - 1] = temp[k - 1];
            temp[k - 1] = swapVar;

            int p = m + 1;
            int q = n;
            while (p < q) {
                swapVar = temp[p - 1];
                temp[p - 1] = temp[q - 1];
                temp[q - 1] = swapVar;
                p++;
                q--;
            }
            tempList = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                x = temp[j];
                tempList.add(x);
            }
            result.add(tempList);
        }
        return result;
    }

    static double factorial(int n) {
        int nfact = 1;
        for (int i = 1; i <= n; i++) {
            nfact *= i;
        }
        return nfact;
    }

    public static int[] generateNextProduct(int[] curr, int[] max) {
        int n = curr.length - 1;
        curr[n]++;
        for (int i = n; i > 0; i--) {
            if (curr[i] > max[i]) {
                curr[i] = 1;
                curr[i - 1]++;
            }
        }
        return curr;
    }

    public static int[] generateNextPermutation(int[] temp, int n) {
        int m = n - 1;
		while (temp[m - 1] > temp[m]) {
			m--;
		}
        int k = n;
		while (temp[m - 1] > temp[k - 1]) {
			k--;
		}
        int swapVar;
        //swap m and k
        swapVar = temp[m - 1];
        temp[m - 1] = temp[k - 1];
        temp[k - 1] = swapVar;

        int p = m + 1;
        int q = n;
        while (p < q) {
            swapVar = temp[p - 1];
            temp[p - 1] = temp[q - 1];
            temp[q - 1] = swapVar;
            p++;
            q--;
        }
        return temp;
    }

}
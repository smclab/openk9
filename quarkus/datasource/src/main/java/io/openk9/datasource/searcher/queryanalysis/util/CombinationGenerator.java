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

package io.openk9.datasource.searcher.queryanalysis.util;

import java.util.ArrayList;
import java.util.List;

public class CombinationGenerator {
    public static <T> List<List<T>> generateCombinations(List<T> list, int r) {
        List<List<T>> result = new ArrayList<>();
        List<List<Integer>> mappings = combination(r, list.size());
        List<T> tempList;
        for (List<Integer> singleCombination :
                mappings) {
            tempList = new ArrayList<>();
            for (int a :
                    singleCombination) {
                tempList.add(list.get(a - 1));
            }
            result.add(tempList);
        }
        return result;
    }

    public static List<List<Integer>> combination(int r, int n) {
        int numberOfCombinations = (int) nCr(n, r);
        List<Integer> tempList;
        List<List<Integer>> result = new ArrayList<>();
        int[] temp = new int[r];
        for (int i = 0; i < r; i++) {
            temp[i] = i + 1;
        }
        tempList = new ArrayList<>();
        int x;
        for (int i = 0; i < r; i++) {
            x = temp[i];
            tempList.add(x);
        }
        result.add(tempList);
        int m, maxVal;
        for (int i = 1; i < numberOfCombinations; i++) {
            m = r;
            maxVal = n;
            while (temp[m - 1] == maxVal) {
                m = m - 1;
                maxVal--;
            }
            temp[m - 1]++;
            for (int j = m; j < r; j++) {
                temp[j] = temp[j - 1] + 1;
            }
            tempList = new ArrayList<>();
            for (int k = 0; k < r; k++) {
                x = temp[k];
                tempList.add(x);
            }
            result.add(tempList);
        }

        return result;
    }

    public static double nCr(int n, int r) {
        int rfact = 1, nfact = 1, nrfact = 1, temp1 = n - r, temp2 = r;
        if (r > n - r) {
            temp1 = r;
            temp2 = n - r;
        }
        for (int i = 1; i <= n; i++) {
            if (i <= temp2) {
                rfact *= i;
                nrfact *= i;
            } else if (i <= temp1) {
                nrfact *= i;
            }
            nfact *= i;
        }
        return nfact / (double) (rfact * nrfact);
    }

    public static int[] generateNextCombination(int[] temp, int n, int r) {
        int m = r;
        int maxVal = n;
        while (temp[m - 1] == maxVal) {
            m = m - 1;
            maxVal--;
        }
        temp[m - 1]++;
        for (int j = m; j < r; j++) {
            temp[j] = temp[j - 1] + 1;
        }
        return temp;
    }
}
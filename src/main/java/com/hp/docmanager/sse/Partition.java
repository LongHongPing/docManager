package com.hp.docmanager.sse;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Description:
 *
 * @Author hp long
 * @Date 2022/3/7 16:56
 */
public class Partition {
    private Partition() {
    }

    public static Multimap<Integer, String> partitioning(Multimap<String, String> lookup) {

        // Partitions Creation
        Set<String> keys = lookup.keySet();

        int partitionId = 0;
        Multimap<Integer, String> partitions = ArrayListMultimap.create();
        int counter2 = 0;

        for (String key : keys) {
            Set<Integer> keys2 = partitions.keySet();
            List<String> inter = (List<String>) lookup.get(key);
            List<String> interTMP = new ArrayList<String>(inter);

            com.hp.docmanager.sse.Printer.debugln("Step number: " + counter2++ + "Number of keywords " + keys.size());

            Set<String> set = new HashSet<String>(interTMP);
            Multimap<Integer, String> partitionsTMP = ArrayListMultimap.create();

            for (Integer key2 : keys2) {

                if (!set.isEmpty()) {
                    Set<String> tmp = new HashSet<String>(partitions.get(key2));

                    Set<String> intersection = Sets.intersection(tmp, set);

                    Set<String> difference;

                    if (intersection.isEmpty()) {
                        difference = tmp;
                    } else {
                        difference = Sets.difference(tmp, intersection);
                        set = Sets.difference(set, intersection);

                    }

                    if (!difference.isEmpty()) {
                        partitionId = partitionId + 1;
                        partitionsTMP.putAll(partitionId, difference);
                    }

                    if (!intersection.isEmpty()) {
                        partitionId = partitionId + 1;
                        partitionsTMP.putAll(partitionId, intersection);
                    }

                } else {
                    partitionId = partitionId + 1;
                    partitionsTMP.putAll(partitionId, new HashSet<String>(partitions.get(key2)));
                }

            }

            interTMP = new ArrayList<String>(set);

            if (!interTMP.isEmpty()) {

                partitionId = partitionId + 1;
                partitionsTMP.putAll(partitionId, interTMP);

            }

            partitions = ArrayListMultimap.create(partitionsTMP);
            partitionsTMP.clear();
            interTMP.clear();

        }

        com.hp.docmanager.sse.Printer.debugln("Partitions size " + partitions.keySet().size());
        com.hp.docmanager.sse.Printer.debugln("\n");

        return partitions;
    }
}

package org.clematis.skos.parser

import org.apache.commons.collections4.CollectionUtils
import org.clematis.skos.parser.model.Concept
import org.clematis.skos.parser.model.Taxonomy

class TaxonomyUtils {

    static List<Stack<Concept>> getCircularDependencies(Taxonomy t) {

        List<Stack<Concept>> paths = new ArrayList<>()

        for (Concept c: t.getTopConcepts()) {
            visitChildren(c, new HashMap<>(), new Stack<Concept>(), paths)
        }

        return paths
    }

    static void visitChildren(Concept c,
                              Map<String, Boolean> visited,
                              Stack<Concept> path,
                              List<Stack<Concept>> paths) {

        visited.put(c.getId(), true)
        path.push(c)

        Collection<Concept> children = c.getChildren()
        for (Concept child : children) {
            if (visited.get(child.getId()) == null) {
                visitChildren(child,
                    (Map<String, Boolean>) visited.clone(),
                    (Stack<Concept>) path.clone(),
                    paths
                )
            } else {
                path.push(child)
                paths.add(path)
            }
        }
    }

    static <K, V> boolean equal(Map<K, V> left, Map<K, V> right) {
        mapDiff(left, right)
        return left == right
    }

    static void mapDiff(Map m1, Map m2, String path="") {
        m1.each { k, v ->

            if(m2[k] != m1[k]) {
                if(m1[k] in Map) {
                    mapDiff(m1[k] as Map, m2[k] as Map, "${path}${k}.")
                }
                else {

                    println("${path}${k} ->");
                    println("\texpected: ${m1[k]}")
                    println("\tactual: ${m2[k]}")

                    if (m1[k] in List) {
                        print("\texpected but missing elements: ")
                        m1[k].each {
                            if(!m2[k].any{it1->return it1==it}) {
                                print(it + ", ")
                            }
                        }
                        print("\tnot expected but found: ")
                        m2[k].each {
                            if(!m1[k].any{it1->return it1==it}) {
                                print(it + ", ")
                            }
                        }
                    }
                    println("")
                }
            }
        }
    }

    static <V> boolean equal(Set<V> left, Set<V> right) {
        return CollectionUtils.isEqualCollection(left, right)
    }
}

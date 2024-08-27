package org.clematis.skos.parser

import org.clematis.skos.parser.model.Concept
import org.clematis.skos.parser.model.Taxonomy

class TaxonomyUtils {

    /**
     * Returns true if the concepts a being a parent of b is in the same branch as the concept b
     *
     * @param t taxonomy containing both concepts
     * @return true if the concepts a being a parent of b is in the same branch as the concept b
     */
    static boolean isSameBranch(Taxonomy t, Concept a, Concept b) {

        Map<String, Integer> tin = new HashMap<>()
        Map<String, Integer> tout = new HashMap<>()
        int T = 0

        for (Concept c: t.getTopConcepts()) {
            markConcepts(c, tin, tout, T)
        }

        return tin.get(a.getId()) < tout.get(a.getId()) < tin.get(b.getId()) < tout.get(b.getId())
    }

    static void markConcepts(Concept c, Map<UUID, Integer> tin, Map<UUID, Integer> tout, int T) {
        tin.put(c.getId(), T)
        T = T + 1
        Collection<Concept> children = c.getChildren()
        for (Concept child : children) {
            if (tin.get(child.getId()) == null) {
                markConcepts(child, tin, tout, T)
            }
        }
        tout.put(c.getId(), T)
    }

    static List<Stack<Concept>> getCircularDependencies(Taxonomy t) {

        Map<String, Boolean> visited = new HashMap<>()
        List<Stack<Concept>> paths = new ArrayList<>()

        for (Concept c: t.getTopConcepts()) {
            dfs(c, visited, new Stack<Concept>(), paths)
        }

        return paths
    }

    static void dfs(Concept c,
                    Map<String, Boolean> visited,
                    Stack<Concept> path,
                    List<Stack<Concept>> paths) {

        visited.put(c.getId(), true)
        path.push(c)

        Collection<Concept> children = c.getChildren()
        for (Concept child : children) {
            if (visited.get(child.getId()) == null) {
                dfs(child, visited, (Stack<Concept>) path.clone(), paths)
            } else {
                // the concept is already visited
                path.push(child)
                paths.add(path)
            }
        }
    }
}

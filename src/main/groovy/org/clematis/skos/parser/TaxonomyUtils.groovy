package org.clematis.skos.parser

import org.clematis.skos.parser.model.Concept
import org.clematis.skos.parser.model.Taxonomy

class TaxonomyUtils {

    static List<Stack<Concept>> getCircularDependencies(Taxonomy t) {

        List<Stack<Concept>> paths = new ArrayList<>()

        for (Concept c: t.getTopConcepts()) {
            dfs(c, new HashMap<>(), new Stack<Concept>(), paths)
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
                dfs(child,
                    (Map<String, Boolean>) visited.clone(),
                    (Stack<Concept>) path.clone(),
                    paths
                )
            } else {
                // the concept is already visited
                path.push(child)
                paths.add(path)
            }
        }
    }
}

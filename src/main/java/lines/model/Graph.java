package lines.model;

import java.util.*;

public class Graph<T> {

    private final Set<T> elements = new HashSet<>();
    private final Map<T, Set<T>> relations = new HashMap<>();

    public Set<T> getElements() {
        return elements;
    }

    public Map<T, Set<T>> getRelations() {
        return relations;
    }

    public boolean hasPath(T from, T to) {
        Queue<T> queue = new ArrayDeque<>(relations.get(from));
        Set<T> checked = new HashSet<>();
        while (!queue.isEmpty()) {
            T el = queue.poll();
            if (!checked.contains(el)) {
                if (el.equals(to)) {
                    return true;
                } else {
                    queue.addAll(relations.get(el));
                    checked.add(el);
                }
            }
        }
        return false;
    }

}

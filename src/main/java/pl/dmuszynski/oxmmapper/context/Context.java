package pl.dmuszynski.oxmmapper.context;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface Context<T> {

    Class<?> getType();

    int getDepth();

    Collection<T> getData();

    Set<Context<T>> subContexts();

    Set<Context<T>> allContexts();

    static <T> Optional<Context<T>> find(Context<T> context, Class<?> target, int depth) {
        Set<Context<T>> contexts = context.allContexts();
        return getContext(contexts, target, depth);
    }

    private static <T> Optional<Context<T>> getContext(Set<Context<T>> contexts, Class<?> target, int depth) {
        return contexts.stream()
                .filter(context -> context.getDepth() == depth)
                .filter(context -> context.getType().equals(target))
                .findAny();
    }
}

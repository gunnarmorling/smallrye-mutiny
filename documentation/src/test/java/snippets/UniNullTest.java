package snippets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class UniNullTest {

    @Test
    public void uni() {
        Uni<String> uni = Uni.createFrom().item(() -> null);
        // tag::code[]
        uni.onItem().ifNull().continueWith("hello");
        uni.onItem().ifNull().switchTo(() -> Uni.createFrom().item("hello"));
        uni.onItem().ifNull().failWith(() -> new Exception("Boom!"));
        // end::code[]

        assertThat(uni.onItem().ifNull().continueWith("hello").await().indefinitely()).isEqualTo("hello");
    }

    @Test
    public void accumulate() {
        Multi<Integer> multi = Multi.createFrom().range(1, 3);
        // tag::acc[]
        Multi<Integer> added = multi.onItem().scan(() -> 0, (item, acc) -> acc + item);
        // end::acc[]
        assertThat(added.subscribe().asIterable()).containsExactly(0, 1, 3);

    }
}

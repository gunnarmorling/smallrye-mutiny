package io.smallrye.mutiny.operators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import io.smallrye.mutiny.GroupedMulti;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.test.MultiAssertSubscriber;

public class MultiGroupTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGroupIntoListsWithSize0() {
        Multi.createFrom().range(1, 5).groupItems().intoLists().of(0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGroupIntoListsWithSize0AndSkip() {
        Multi.createFrom().range(1, 5).groupItems().intoLists().of(0, 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGroupIntoListsWithSkip0() {
        Multi.createFrom().range(1, 5).groupItems().intoLists().of(1, 0);
    }

    @Test
    public void testGroupIntoListsOfTwoElements() {
        MultiAssertSubscriber<List<Integer>> subscriber = Multi.createFrom().range(1, 10).groupItems().intoLists().of(2)
                .subscribe().withSubscriber(MultiAssertSubscriber.create(100));
        subscriber.assertCompletedSuccessfully();
        assertThat(subscriber.items()).containsExactly(
                Arrays.asList(1, 2), Arrays.asList(3, 4), Arrays.asList(5, 6), Arrays.asList(7, 8),
                Collections.singletonList(9));
    }

    @Test
    public void testGroupIntoListsOfTwoElementsWithRequests() {
        MultiAssertSubscriber<List<Integer>> subscriber = Multi.createFrom().range(1, 10).groupItems().intoLists().of(2)
                .subscribe().withSubscriber(MultiAssertSubscriber.create());
        subscriber
                .assertSubscribed().assertHasNotReceivedAnyItem()
                .request(3);

        assertThat(subscriber.items()).containsExactly(
                Arrays.asList(1, 2), Arrays.asList(3, 4), Arrays.asList(5, 6));

        subscriber.assertNotTerminated().request(5);

        subscriber.assertCompletedSuccessfully();
        assertThat(subscriber.items()).containsExactly(
                Arrays.asList(1, 2), Arrays.asList(3, 4), Arrays.asList(5, 6), Arrays.asList(7, 8),
                Collections.singletonList(9));
    }

    @Test
    public void testGroupIntoListsOfTwoElementsWithSkip() {
        MultiAssertSubscriber<List<Integer>> subscriber = Multi.createFrom().range(1, 10).groupItems().intoLists().of(2, 3)
                .subscribe().withSubscriber(MultiAssertSubscriber.create(100));
        subscriber.assertCompletedSuccessfully();
        assertThat(subscriber.items()).containsExactly(
                Arrays.asList(1, 2), Arrays.asList(4, 5), Arrays.asList(7, 8));
    }

    @Test
    public void testGroupIntoListsOfTwoElementsWithSkipSmallerThanSize() {
        MultiAssertSubscriber<List<Integer>> subscriber = Multi.createFrom().range(1, 10).groupItems().intoLists().of(2, 1)
                .subscribe().withSubscriber(MultiAssertSubscriber.create(100));
        subscriber.assertCompletedSuccessfully();
        assertThat(subscriber.items()).containsExactly(
                Arrays.asList(1, 2), Arrays.asList(2, 3), Arrays.asList(3, 4),
                Arrays.asList(4, 5), Arrays.asList(5, 6), Arrays.asList(6, 7),
                Arrays.asList(7, 8), Arrays.asList(8, 9), Collections.singletonList(9));
    }

    @Test
    public void testGroupIntoListsOfTwoElementsWithRequestsAndSkip() {
        MultiAssertSubscriber<List<Integer>> subscriber = Multi.createFrom().range(1, 10).groupItems().intoLists().of(2, 3)
                .subscribe().withSubscriber(MultiAssertSubscriber.create());
        subscriber
                .assertSubscribed().assertHasNotReceivedAnyItem()
                .request(2);

        assertThat(subscriber.items()).containsExactly(
                Arrays.asList(1, 2), Arrays.asList(4, 5));

        subscriber.assertNotTerminated().request(5);

        subscriber.assertCompletedSuccessfully();
        assertThat(subscriber.items()).containsExactly(
                Arrays.asList(1, 2), Arrays.asList(4, 5), Arrays.asList(7, 8));
    }

    @Test
    public void testCombinationOfToListsAndAwait() {
        Multi<List<Integer>> multi = Multi.createFrom().range(1, 10).groupItems().intoLists().of(2);

        assertThat(multi.collectItems().first().await().indefinitely()).containsExactly(1, 2);
        assertThat(multi.collectItems().last().await().indefinitely()).containsExactly(9);

        assertThat(multi.collectItems().asList().await().indefinitely()).hasSize(5)
                .containsExactly(Arrays.asList(1, 2), Arrays.asList(3, 4), Arrays.asList(5, 6),
                        Arrays.asList(7, 8), Collections.singletonList(9));
    }

    @Test
    public void testAsListsOnEmptyStream() {
        assertThat(Multi.createFrom().empty().groupItems().intoLists().of(2).collectItems().last().await().indefinitely())
                .isNull();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAsListsWithNegativeDuration() {
        Multi.createFrom().range(1, 10).groupItems().intoLists().every(Duration.ofMillis(-2));
    }

    @Test
    public void testAsListsWithDuration() {
        Multi<Long> publisher = Multi.createFrom().publisher(Multi.createFrom().ticks().every(Duration.ofMillis(2)));
        MultiAssertSubscriber<List<Long>> subscriber = publisher.groupItems().intoLists().every(Duration.ofMillis(100))
                .subscribe()
                .withSubscriber(MultiAssertSubscriber.create(100));

        await().until(() -> subscriber.items().size() > 3);
        subscriber.cancel();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGroupIntoMultisWithSize0() {
        Multi.createFrom().range(1, 5).groupItems().intoMultis().of(0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGroupIntoMultisWithSize0AndSkip() {
        Multi.createFrom().range(1, 5).groupItems().intoMultis().of(0, 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGroupIntoMultisWithSkip0() {
        Multi.createFrom().range(1, 5).groupItems().intoMultis().of(1, 0);
    }

    @Test
    public void testGroupIntoMultisOfTwoElements() {
        MultiAssertSubscriber<Multi<Integer>> subscriber = Multi.createFrom().range(1, 10).groupItems().intoMultis().of(2)
                .subscribe().withSubscriber(MultiAssertSubscriber.create(100));
        subscriber.assertCompletedSuccessfully();
        List<List<Integer>> flatten = flatten(subscriber.items());
        assertThat(flatten).containsExactly(
                Arrays.asList(1, 2), Arrays.asList(3, 4), Arrays.asList(5, 6), Arrays.asList(7, 8),
                Collections.singletonList(9));
    }

    private <T> List<List<T>> flatten(List<Multi<T>> items) {
        List<List<T>> list = new ArrayList<>();
        for (Multi<T> multi : items) {
            list.add(multi.collectItems().asList().await().indefinitely());
        }
        return list;
    }

    @Test
    public void testGroupIntoMultisOfTwoElementsWithRequests() {
        MultiAssertSubscriber<Multi<Integer>> subscriber = Multi.createFrom().range(1, 10).groupItems().intoMultis().of(2)
                .subscribe().withSubscriber(MultiAssertSubscriber.create());
        subscriber
                .assertSubscribed().assertHasNotReceivedAnyItem()
                .request(3);

        assertThat(subscriber.items()).hasSize(3);

        subscriber.assertNotTerminated().request(5);

        subscriber.assertCompletedSuccessfully();
        assertThat(flatten(subscriber.items())).containsExactly(
                Arrays.asList(1, 2), Arrays.asList(3, 4), Arrays.asList(5, 6), Arrays.asList(7, 8),
                Collections.singletonList(9));
    }

    @Test
    public void testGroupIntoMultisOfTwoElementsWithSkip() {
        MultiAssertSubscriber<Multi<Integer>> subscriber = Multi.createFrom().range(1, 10).groupItems().intoMultis().of(2, 3)
                .subscribe().withSubscriber(MultiAssertSubscriber.create(100));
        subscriber.assertCompletedSuccessfully();
        assertThat(flatten(subscriber.items())).containsExactly(
                Arrays.asList(1, 2), Arrays.asList(4, 5), Arrays.asList(7, 8));
    }

    @Test
    public void testGroupIntoMultisOfTwoElementsWithSkipSmallerThanSize() {
        MultiAssertSubscriber<Multi<Integer>> subscriber = Multi.createFrom().range(1, 10).groupItems().intoMultis().of(2, 1)
                .subscribe().withSubscriber(MultiAssertSubscriber.create(100));
        subscriber.assertCompletedSuccessfully();
        assertThat(flatten(subscriber.items())).containsExactly(
                Arrays.asList(1, 2), Arrays.asList(2, 3), Arrays.asList(3, 4),
                Arrays.asList(4, 5), Arrays.asList(5, 6), Arrays.asList(6, 7),
                Arrays.asList(7, 8), Arrays.asList(8, 9), Collections.singletonList(9));
    }

    @Test
    public void testGroupIntoMultisOfTwoElementsWithRequestsAndSkip() {
        MultiAssertSubscriber<Multi<Integer>> subscriber = Multi.createFrom().range(1, 10).groupItems().intoMultis().of(2, 3)
                .subscribe().withSubscriber(MultiAssertSubscriber.create());
        subscriber
                .assertSubscribed().assertHasNotReceivedAnyItem()
                .request(2);

        assertThat(subscriber.items()).hasSize(2);

        subscriber.assertNotTerminated().request(5);

        subscriber.assertCompletedSuccessfully();
        assertThat(flatten(subscriber.items())).containsExactly(
                Arrays.asList(1, 2), Arrays.asList(4, 5), Arrays.asList(7, 8));
    }

    @Test
    public void testCombinationOfToMultisAndAwait() {
        Multi<Multi<Integer>> multi = Multi.createFrom().range(1, 10).groupItems().intoMultis().of(2);

        assertThat(multi.collectItems().first().await().indefinitely().collectItems().asList().await().indefinitely())
                .containsExactly(1, 2);
        assertThat(multi.collectItems().last().await().indefinitely().collectItems().asList().await().indefinitely())
                .containsExactly(9);

        assertThat(flatten(multi.collectItems().asList().await().indefinitely())).hasSize(5)
                .containsExactly(Arrays.asList(1, 2), Arrays.asList(3, 4), Arrays.asList(5, 6),
                        Arrays.asList(7, 8), Collections.singletonList(9));
    }

    @Test
    public void testAsMultisOnEmptyStream() {
        assertThat(Multi.createFrom().empty().groupItems().intoMultis().of(2).collectItems().last().await().indefinitely())
                .isNull();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAsMultisWithNegativeDuration() {
        Multi.createFrom().range(1, 10).groupItems().intoMultis().every(Duration.ofMillis(-2));
    }

    @Test
    public void testAsMultisWithDuration() {
        Multi<Long> publisher = Multi.createFrom().publisher(Multi.createFrom().ticks().every(Duration.ofMillis(2)));
        MultiAssertSubscriber<Multi<Long>> subscriber = publisher.groupItems().intoMultis().every(Duration.ofMillis(100))
                .subscribe()
                .withSubscriber(MultiAssertSubscriber.create(100));

        await().until(() -> subscriber.items().size() > 3);
        subscriber.cancel();
    }

    @Test
    public void testBasicTimeWindow() {
        Multi<Multi<Integer>> multi = Multi.createFrom().range(1, 7)
                .groupItems().intoMultis().every(Duration.ofMillis(1));
        Uni<List<Integer>> uni = multi
                .onItem().produceMulti(m -> m).concatenate()
                .collectItems().asList();

        List<Integer> list = uni.await().atMost(Duration.ofSeconds(4));
        assertThat(list).contains(1, 2, 3, 4, 5, 6);
    }

    @Test
    public void testThatWindowWithDurationEmitsEmptyLists() {
        MultiAssertSubscriber<List<Object>> subscriber = MultiAssertSubscriber.create(3);
        Multi.createFrom().nothing()
                .groupItems().intoMultis().every(Duration.ofMillis(10))
                .onItem().produceUni(m -> m.collectItems().asList()).merge()
                .subscribe().withSubscriber(subscriber);

        await().until(() -> subscriber.items().size() == 3);
        List<List<Object>> items = subscriber.items();
        assertThat(items).allSatisfy(list -> assertThat(list).isEmpty());
        subscriber.cancel();
    }

    @Test
    public void testGroupByWithKeyMapperOnly() {
        MultiAssertSubscriber<GroupedMulti<Integer, Integer>> subscriber = Multi.createFrom().range(1, 10)
                .groupItems().by(i -> i % 2)
                .subscribe().withSubscriber(MultiAssertSubscriber.create(100));

        subscriber.assertCompletedSuccessfully();
        assertThat(subscriber.items()).hasSize(2);

        List<Integer> odd = subscriber.items().get(0).collectItems().asList().await().indefinitely();
        List<Integer> even = subscriber.items().get(1).collectItems().asList().await().indefinitely();

        assertThat(subscriber.items().get(0).key()).isEqualTo(1);
        assertThat(subscriber.items().get(1).key()).isEqualTo(0);

        assertThat(odd).containsExactly(1, 3, 5, 7, 9);
        assertThat(even).containsExactly(2, 4, 6, 8);
    }

    @Test
    public void testGroupByWithKeyMapperAndValueMapper() {
        MultiAssertSubscriber<GroupedMulti<Integer, String>> subscriber = Multi.createFrom().range(1, 10)
                .groupItems().by(i -> i % 2, t -> Integer.toString(t))
                .subscribe().withSubscriber(MultiAssertSubscriber.create(100));

        subscriber.assertCompletedSuccessfully();
        assertThat(subscriber.items()).hasSize(2);

        List<String> odd = subscriber.items().get(0).collectItems().asList().await().indefinitely();
        List<String> even = subscriber.items().get(1).collectItems().asList().await().indefinitely();

        assertThat(subscriber.items().get(0).key()).isEqualTo(1);
        assertThat(subscriber.items().get(1).key()).isEqualTo(0);

        assertThat(odd).containsExactly("1", "3", "5", "7", "9");
        assertThat(even).containsExactly("2", "4", "6", "8");
    }

    @Test
    public void testGroupByProducingASingleGroup() {
        MultiAssertSubscriber<GroupedMulti<Integer, Integer>> subscriber = Multi.createFrom().range(1, 10)
                .groupItems().by(i -> 0)
                .subscribe().withSubscriber(MultiAssertSubscriber.create(100));

        subscriber.assertCompletedSuccessfully();
        assertThat(subscriber.items()).hasSize(1);
        List<Integer> numbers = subscriber.items().get(0).collectItems().asList().await().indefinitely();
        assertThat(subscriber.items().get(0).key()).isEqualTo(0);
        assertThat(numbers).hasSize(9);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGroupByWithNullKeyMapper() {
        Multi.createFrom().range(1, 10).groupItems().by(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGroupByWithNullValueMapper() {
        Multi.createFrom().range(1, 10).groupItems().by(i -> i % 2, null);
    }

    @Test
    public void testGroupByOnFailingMulti() {
        MultiAssertSubscriber<GroupedMulti<Integer, Integer>> subscriber = Multi.createFrom()
                .<Integer> failure(new IOException("boom"))
                .groupItems().by(i -> i % 2)
                .subscribe().withSubscriber(MultiAssertSubscriber.create(100));

        subscriber.assertHasFailedWith(IOException.class, "boom");
        assertThat(subscriber.items()).hasSize(0);
    }

    @Test
    public void testGroupByOnEmptyMulti() {
        MultiAssertSubscriber<GroupedMulti<Integer, Integer>> subscriber = Multi.createFrom().<Integer> empty()
                .groupItems().by(i -> i % 2)
                .subscribe().withSubscriber(MultiAssertSubscriber.create(100));

        subscriber.assertCompletedSuccessfully().assertHasNotReceivedAnyItem();
    }

    @Test
    public void testGroupByFollowedWithAFlatMap() {
        MultiAssertSubscriber<Integer> subscriber = Multi.createFrom().range(1, 10)
                .groupItems().by(i -> 1)
                .flatMap(gm -> gm)
                .subscribe().withSubscriber(MultiAssertSubscriber.create(100));

        subscriber.assertCompletedSuccessfully();
        assertThat(subscriber.items()).hasSize(9);
    }

}

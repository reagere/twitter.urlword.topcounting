package com.reagere.twitter.urlword.topcounting.actors;

import com.reagere.twitter.urlword.topcounting.model.PairKeyCount;
import com.reagere.twitter.urlword.topcounting.model.TupleListTime;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;

public class CountOccurrencesFlowStreamActor implements Subscriber<TupleListTime>, Publisher<PairKeyCount> {

    private final Map<String, Long> counts = new HashMap<>();
    private final Set<Subscription> subscriptionSet = new HashSet<>();
    private final Set<Subscriber<? super PairKeyCount>> subscriberSet = new HashSet<>();

    @Override
    public void onSubscribe(Subscription subscription) {
        subscriptionSet.add(subscription);
    }

    @Override
    public void onNext(TupleListTime tupleListTime) {
        tupleListTime.getWordsUrls().forEach(wordUrl -> push(new PairKeyCount(counts.compute(wordUrl, (w, c) -> c == null ? 1 : c + 1), wordUrl, tupleListTime.getTime())));
    }

    private void push(PairKeyCount p) {
        subscriberSet.forEach(s -> s.onNext(p));
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
        subscriptionSet.forEach(Subscription::cancel);
        subscriptionSet.clear();
        subscriberSet.forEach(Subscriber::onComplete);
        subscriberSet.clear();
    }

    @Override
    public void subscribe(Subscriber<? super PairKeyCount> subscriber) {
        subscriberSet.add(subscriber);
    }

    protected Map<String, Long> getCounts() {
        return counts;
    }
}

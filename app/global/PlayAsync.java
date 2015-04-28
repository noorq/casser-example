/*
 *      Copyright (C) 2015 Noorq, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package global;

import play.libs.F;
import scala.concurrent.Future;
import scala.concurrent.impl.Promise.DefaultPromise;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public final class PlayAsync {

	private PlayAsync() {
	}

    public static <T, A> F.Promise<F.Tuple<T, A>> asPromise(ListenableFuture<T> future, A a) {
        return F.Promise.wrap(asFuture(future, a));
    }
    
    public static <T, A> Future<F.Tuple<T, A>> asFuture(ListenableFuture<T> future, A a) {
        final scala.concurrent.Promise<F.Tuple<T, A>> promise = new DefaultPromise<F.Tuple<T, A>>();
        Futures.addCallback(future, new FutureCallback<T>() {
            @Override public void onSuccess(T result) {
                promise.success(new F.Tuple<T, A>(result, a));
            }
            @Override public void onFailure(Throwable t) {
                promise.failure(t);
            }
        });
        return promise.future();
    }
	
    public static <T> F.Promise<T> asPromise(ListenableFuture<T> future) {
        return F.Promise.wrap(asFuture(future));
    }
	
    public static <T> Future<T> asFuture(ListenableFuture<T> future) {
        final scala.concurrent.Promise<T> promise = new DefaultPromise<T>();
        Futures.addCallback(future, new FutureCallback<T>() {
            @Override public void onSuccess(T result) {
                promise.success(result);
            }
            @Override public void onFailure(Throwable t) {
                promise.failure(t);
            }
        });
        return promise.future();
    }
}

package com.msa.android.presentation.common;

import com.msa.android.data.source.network.ConnectivityObserver;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class NetworkStatusViewModel_Factory implements Factory<NetworkStatusViewModel> {
  private final Provider<ConnectivityObserver> observerProvider;

  public NetworkStatusViewModel_Factory(Provider<ConnectivityObserver> observerProvider) {
    this.observerProvider = observerProvider;
  }

  @Override
  public NetworkStatusViewModel get() {
    return newInstance(observerProvider.get());
  }

  public static NetworkStatusViewModel_Factory create(
      Provider<ConnectivityObserver> observerProvider) {
    return new NetworkStatusViewModel_Factory(observerProvider);
  }

  public static NetworkStatusViewModel newInstance(ConnectivityObserver observer) {
    return new NetworkStatusViewModel(observer);
  }
}

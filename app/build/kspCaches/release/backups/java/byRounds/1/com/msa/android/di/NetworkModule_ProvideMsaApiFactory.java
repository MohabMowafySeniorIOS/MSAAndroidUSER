package com.msa.android.di;

import com.msa.android.data.source.network.MsaApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
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
public final class NetworkModule_ProvideMsaApiFactory implements Factory<MsaApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideMsaApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public MsaApi get() {
    return provideMsaApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideMsaApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideMsaApiFactory(retrofitProvider);
  }

  public static MsaApi provideMsaApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideMsaApi(retrofit));
  }
}

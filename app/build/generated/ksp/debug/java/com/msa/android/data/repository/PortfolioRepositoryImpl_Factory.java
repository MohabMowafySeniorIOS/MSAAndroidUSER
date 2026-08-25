package com.msa.android.data.repository;

import com.msa.android.data.source.local.PortfolioPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class PortfolioRepositoryImpl_Factory implements Factory<PortfolioRepositoryImpl> {
  private final Provider<PortfolioPreferences> prefsProvider;

  public PortfolioRepositoryImpl_Factory(Provider<PortfolioPreferences> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public PortfolioRepositoryImpl get() {
    return newInstance(prefsProvider.get());
  }

  public static PortfolioRepositoryImpl_Factory create(
      Provider<PortfolioPreferences> prefsProvider) {
    return new PortfolioRepositoryImpl_Factory(prefsProvider);
  }

  public static PortfolioRepositoryImpl newInstance(PortfolioPreferences prefs) {
    return new PortfolioRepositoryImpl(prefs);
  }
}

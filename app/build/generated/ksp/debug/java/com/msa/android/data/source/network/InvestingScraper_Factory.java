package com.msa.android.data.source.network;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class InvestingScraper_Factory implements Factory<InvestingScraper> {
  private final Provider<Context> contextProvider;

  public InvestingScraper_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public InvestingScraper get() {
    return newInstance(contextProvider.get());
  }

  public static InvestingScraper_Factory create(Provider<Context> contextProvider) {
    return new InvestingScraper_Factory(contextProvider);
  }

  public static InvestingScraper newInstance(Context context) {
    return new InvestingScraper(context);
  }
}

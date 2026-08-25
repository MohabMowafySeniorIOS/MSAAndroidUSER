package com.msa.android.data.source.local;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class PriceDataGenerator_Factory implements Factory<PriceDataGenerator> {
  @Override
  public PriceDataGenerator get() {
    return newInstance();
  }

  public static PriceDataGenerator_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PriceDataGenerator newInstance() {
    return new PriceDataGenerator();
  }

  private static final class InstanceHolder {
    private static final PriceDataGenerator_Factory INSTANCE = new PriceDataGenerator_Factory();
  }
}

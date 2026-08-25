package com.msa.android.domain.usecase;

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
public final class PriceCalculator_Factory implements Factory<PriceCalculator> {
  @Override
  public PriceCalculator get() {
    return newInstance();
  }

  public static PriceCalculator_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PriceCalculator newInstance() {
    return new PriceCalculator();
  }

  private static final class InstanceHolder {
    private static final PriceCalculator_Factory INSTANCE = new PriceCalculator_Factory();
  }
}

package com.msa.android.domain.usecase;

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
public final class GoldValueCalculator_Factory implements Factory<GoldValueCalculator> {
  private final Provider<PriceCalculator> priceCalcProvider;

  public GoldValueCalculator_Factory(Provider<PriceCalculator> priceCalcProvider) {
    this.priceCalcProvider = priceCalcProvider;
  }

  @Override
  public GoldValueCalculator get() {
    return newInstance(priceCalcProvider.get());
  }

  public static GoldValueCalculator_Factory create(Provider<PriceCalculator> priceCalcProvider) {
    return new GoldValueCalculator_Factory(priceCalcProvider);
  }

  public static GoldValueCalculator newInstance(PriceCalculator priceCalc) {
    return new GoldValueCalculator(priceCalc);
  }
}

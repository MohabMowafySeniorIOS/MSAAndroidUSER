package com.msa.android.presentation.screens.indicators;

import com.msa.android.data.source.local.PriceDataGenerator;
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
public final class IndicatorsViewModel_Factory implements Factory<IndicatorsViewModel> {
  private final Provider<PriceDataGenerator> generatorProvider;

  public IndicatorsViewModel_Factory(Provider<PriceDataGenerator> generatorProvider) {
    this.generatorProvider = generatorProvider;
  }

  @Override
  public IndicatorsViewModel get() {
    return newInstance(generatorProvider.get());
  }

  public static IndicatorsViewModel_Factory create(Provider<PriceDataGenerator> generatorProvider) {
    return new IndicatorsViewModel_Factory(generatorProvider);
  }

  public static IndicatorsViewModel newInstance(PriceDataGenerator generator) {
    return new IndicatorsViewModel(generator);
  }
}

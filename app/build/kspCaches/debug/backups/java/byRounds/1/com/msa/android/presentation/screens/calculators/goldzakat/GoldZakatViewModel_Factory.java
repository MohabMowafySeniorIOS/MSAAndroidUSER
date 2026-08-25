package com.msa.android.presentation.screens.calculators.goldzakat;

import com.msa.android.domain.repository.MetalsRepository;
import com.msa.android.domain.usecase.ZakatCalculator;
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
public final class GoldZakatViewModel_Factory implements Factory<GoldZakatViewModel> {
  private final Provider<MetalsRepository> metalsRepoProvider;

  private final Provider<ZakatCalculator> calcProvider;

  public GoldZakatViewModel_Factory(Provider<MetalsRepository> metalsRepoProvider,
      Provider<ZakatCalculator> calcProvider) {
    this.metalsRepoProvider = metalsRepoProvider;
    this.calcProvider = calcProvider;
  }

  @Override
  public GoldZakatViewModel get() {
    return newInstance(metalsRepoProvider.get(), calcProvider.get());
  }

  public static GoldZakatViewModel_Factory create(Provider<MetalsRepository> metalsRepoProvider,
      Provider<ZakatCalculator> calcProvider) {
    return new GoldZakatViewModel_Factory(metalsRepoProvider, calcProvider);
  }

  public static GoldZakatViewModel newInstance(MetalsRepository metalsRepo, ZakatCalculator calc) {
    return new GoldZakatViewModel(metalsRepo, calc);
  }
}

package com.msa.android.presentation.screens.calculators.silverzakat;

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
public final class SilverZakatViewModel_Factory implements Factory<SilverZakatViewModel> {
  private final Provider<MetalsRepository> metalsRepoProvider;

  private final Provider<ZakatCalculator> calcProvider;

  public SilverZakatViewModel_Factory(Provider<MetalsRepository> metalsRepoProvider,
      Provider<ZakatCalculator> calcProvider) {
    this.metalsRepoProvider = metalsRepoProvider;
    this.calcProvider = calcProvider;
  }

  @Override
  public SilverZakatViewModel get() {
    return newInstance(metalsRepoProvider.get(), calcProvider.get());
  }

  public static SilverZakatViewModel_Factory create(Provider<MetalsRepository> metalsRepoProvider,
      Provider<ZakatCalculator> calcProvider) {
    return new SilverZakatViewModel_Factory(metalsRepoProvider, calcProvider);
  }

  public static SilverZakatViewModel newInstance(MetalsRepository metalsRepo,
      ZakatCalculator calc) {
    return new SilverZakatViewModel(metalsRepo, calc);
  }
}

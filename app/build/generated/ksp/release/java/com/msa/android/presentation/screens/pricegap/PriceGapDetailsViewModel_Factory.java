package com.msa.android.presentation.screens.pricegap;

import com.msa.android.domain.repository.BanksRepository;
import com.msa.android.domain.repository.MetalsRepository;
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
public final class PriceGapDetailsViewModel_Factory implements Factory<PriceGapDetailsViewModel> {
  private final Provider<MetalsRepository> metalsRepoProvider;

  private final Provider<BanksRepository> banksRepoProvider;

  public PriceGapDetailsViewModel_Factory(Provider<MetalsRepository> metalsRepoProvider,
      Provider<BanksRepository> banksRepoProvider) {
    this.metalsRepoProvider = metalsRepoProvider;
    this.banksRepoProvider = banksRepoProvider;
  }

  @Override
  public PriceGapDetailsViewModel get() {
    return newInstance(metalsRepoProvider.get(), banksRepoProvider.get());
  }

  public static PriceGapDetailsViewModel_Factory create(
      Provider<MetalsRepository> metalsRepoProvider, Provider<BanksRepository> banksRepoProvider) {
    return new PriceGapDetailsViewModel_Factory(metalsRepoProvider, banksRepoProvider);
  }

  public static PriceGapDetailsViewModel newInstance(MetalsRepository metalsRepo,
      BanksRepository banksRepo) {
    return new PriceGapDetailsViewModel(metalsRepo, banksRepo);
  }
}

package com.msa.android.presentation.screens.calculators.bullions;

import com.msa.android.domain.repository.BullionRepository;
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
public final class BullionViewModel_Factory implements Factory<BullionViewModel> {
  private final Provider<BullionRepository> bullionRepoProvider;

  private final Provider<MetalsRepository> metalsRepoProvider;

  public BullionViewModel_Factory(Provider<BullionRepository> bullionRepoProvider,
      Provider<MetalsRepository> metalsRepoProvider) {
    this.bullionRepoProvider = bullionRepoProvider;
    this.metalsRepoProvider = metalsRepoProvider;
  }

  @Override
  public BullionViewModel get() {
    return newInstance(bullionRepoProvider.get(), metalsRepoProvider.get());
  }

  public static BullionViewModel_Factory create(Provider<BullionRepository> bullionRepoProvider,
      Provider<MetalsRepository> metalsRepoProvider) {
    return new BullionViewModel_Factory(bullionRepoProvider, metalsRepoProvider);
  }

  public static BullionViewModel newInstance(BullionRepository bullionRepo,
      MetalsRepository metalsRepo) {
    return new BullionViewModel(bullionRepo, metalsRepo);
  }
}

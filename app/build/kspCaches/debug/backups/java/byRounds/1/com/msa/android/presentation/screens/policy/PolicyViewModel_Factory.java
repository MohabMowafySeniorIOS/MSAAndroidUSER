package com.msa.android.presentation.screens.policy;

import com.msa.android.data.source.local.LanguagePreferences;
import com.msa.android.domain.repository.PagesRepository;
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
public final class PolicyViewModel_Factory implements Factory<PolicyViewModel> {
  private final Provider<PagesRepository> pagesRepoProvider;

  private final Provider<LanguagePreferences> prefsProvider;

  public PolicyViewModel_Factory(Provider<PagesRepository> pagesRepoProvider,
      Provider<LanguagePreferences> prefsProvider) {
    this.pagesRepoProvider = pagesRepoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public PolicyViewModel get() {
    return newInstance(pagesRepoProvider.get(), prefsProvider.get());
  }

  public static PolicyViewModel_Factory create(Provider<PagesRepository> pagesRepoProvider,
      Provider<LanguagePreferences> prefsProvider) {
    return new PolicyViewModel_Factory(pagesRepoProvider, prefsProvider);
  }

  public static PolicyViewModel newInstance(PagesRepository pagesRepo, LanguagePreferences prefs) {
    return new PolicyViewModel(pagesRepo, prefs);
  }
}

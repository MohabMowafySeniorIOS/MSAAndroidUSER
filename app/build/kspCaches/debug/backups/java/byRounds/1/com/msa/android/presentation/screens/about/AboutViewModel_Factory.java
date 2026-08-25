package com.msa.android.presentation.screens.about;

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
public final class AboutViewModel_Factory implements Factory<AboutViewModel> {
  private final Provider<PagesRepository> pagesRepoProvider;

  private final Provider<LanguagePreferences> prefsProvider;

  public AboutViewModel_Factory(Provider<PagesRepository> pagesRepoProvider,
      Provider<LanguagePreferences> prefsProvider) {
    this.pagesRepoProvider = pagesRepoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public AboutViewModel get() {
    return newInstance(pagesRepoProvider.get(), prefsProvider.get());
  }

  public static AboutViewModel_Factory create(Provider<PagesRepository> pagesRepoProvider,
      Provider<LanguagePreferences> prefsProvider) {
    return new AboutViewModel_Factory(pagesRepoProvider, prefsProvider);
  }

  public static AboutViewModel newInstance(PagesRepository pagesRepo, LanguagePreferences prefs) {
    return new AboutViewModel(pagesRepo, prefs);
  }
}

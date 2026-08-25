package com.msa.android.presentation.screens.faq;

import com.msa.android.data.source.local.LanguagePreferences;
import com.msa.android.domain.repository.FaqRepository;
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
public final class FaqViewModel_Factory implements Factory<FaqViewModel> {
  private final Provider<FaqRepository> repoProvider;

  private final Provider<LanguagePreferences> prefsProvider;

  public FaqViewModel_Factory(Provider<FaqRepository> repoProvider,
      Provider<LanguagePreferences> prefsProvider) {
    this.repoProvider = repoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public FaqViewModel get() {
    return newInstance(repoProvider.get(), prefsProvider.get());
  }

  public static FaqViewModel_Factory create(Provider<FaqRepository> repoProvider,
      Provider<LanguagePreferences> prefsProvider) {
    return new FaqViewModel_Factory(repoProvider, prefsProvider);
  }

  public static FaqViewModel newInstance(FaqRepository repo, LanguagePreferences prefs) {
    return new FaqViewModel(repo, prefs);
  }
}

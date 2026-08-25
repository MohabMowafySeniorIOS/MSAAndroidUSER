package com.msa.android.presentation.screens.onboarding;

import com.msa.android.data.source.local.LanguagePreferences;
import com.msa.android.domain.repository.OnBoardingRepository;
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
public final class OnBoardingViewModel_Factory implements Factory<OnBoardingViewModel> {
  private final Provider<OnBoardingRepository> repoProvider;

  private final Provider<LanguagePreferences> prefsProvider;

  public OnBoardingViewModel_Factory(Provider<OnBoardingRepository> repoProvider,
      Provider<LanguagePreferences> prefsProvider) {
    this.repoProvider = repoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public OnBoardingViewModel get() {
    return newInstance(repoProvider.get(), prefsProvider.get());
  }

  public static OnBoardingViewModel_Factory create(Provider<OnBoardingRepository> repoProvider,
      Provider<LanguagePreferences> prefsProvider) {
    return new OnBoardingViewModel_Factory(repoProvider, prefsProvider);
  }

  public static OnBoardingViewModel newInstance(OnBoardingRepository repo,
      LanguagePreferences prefs) {
    return new OnBoardingViewModel(repo, prefs);
  }
}

package com.msa.android.presentation.screens.language;

import com.msa.android.data.source.local.LanguagePreferences;
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
public final class LanguageViewModel_Factory implements Factory<LanguageViewModel> {
  private final Provider<LanguagePreferences> prefsProvider;

  public LanguageViewModel_Factory(Provider<LanguagePreferences> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public LanguageViewModel get() {
    return newInstance(prefsProvider.get());
  }

  public static LanguageViewModel_Factory create(Provider<LanguagePreferences> prefsProvider) {
    return new LanguageViewModel_Factory(prefsProvider);
  }

  public static LanguageViewModel newInstance(LanguagePreferences prefs) {
    return new LanguageViewModel(prefs);
  }
}

package com.msa.android.presentation.screens.help;

import com.msa.android.domain.repository.ContactRepository;
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
public final class HelpViewModel_Factory implements Factory<HelpViewModel> {
  private final Provider<ContactRepository> repoProvider;

  public HelpViewModel_Factory(Provider<ContactRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public HelpViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static HelpViewModel_Factory create(Provider<ContactRepository> repoProvider) {
    return new HelpViewModel_Factory(repoProvider);
  }

  public static HelpViewModel newInstance(ContactRepository repo) {
    return new HelpViewModel(repo);
  }
}

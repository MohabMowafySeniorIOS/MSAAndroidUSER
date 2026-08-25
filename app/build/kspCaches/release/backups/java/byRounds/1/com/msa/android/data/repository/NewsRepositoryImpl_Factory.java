package com.msa.android.data.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.msa.android.data.source.local.LanguagePreferences;
import com.msa.android.data.source.network.MsaApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class NewsRepositoryImpl_Factory implements Factory<NewsRepositoryImpl> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<MsaApi> msaApiProvider;

  private final Provider<LanguagePreferences> languagePreferencesProvider;

  public NewsRepositoryImpl_Factory(Provider<FirebaseFirestore> firestoreProvider,
      Provider<MsaApi> msaApiProvider, Provider<LanguagePreferences> languagePreferencesProvider) {
    this.firestoreProvider = firestoreProvider;
    this.msaApiProvider = msaApiProvider;
    this.languagePreferencesProvider = languagePreferencesProvider;
  }

  @Override
  public NewsRepositoryImpl get() {
    return newInstance(firestoreProvider.get(), msaApiProvider.get(), languagePreferencesProvider.get());
  }

  public static NewsRepositoryImpl_Factory create(Provider<FirebaseFirestore> firestoreProvider,
      Provider<MsaApi> msaApiProvider, Provider<LanguagePreferences> languagePreferencesProvider) {
    return new NewsRepositoryImpl_Factory(firestoreProvider, msaApiProvider, languagePreferencesProvider);
  }

  public static NewsRepositoryImpl newInstance(FirebaseFirestore firestore, MsaApi msaApi,
      LanguagePreferences languagePreferences) {
    return new NewsRepositoryImpl(firestore, msaApi, languagePreferences);
  }
}

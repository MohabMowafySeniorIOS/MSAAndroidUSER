package com.msa.android;

import com.msa.android.data.source.local.LanguagePreferences;
import com.msa.android.data.source.network.ConnectivityObserver;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<LanguagePreferences> languagePreferencesProvider;

  private final Provider<ConnectivityObserver> connectivityObserverProvider;

  public MainActivity_MembersInjector(Provider<LanguagePreferences> languagePreferencesProvider,
      Provider<ConnectivityObserver> connectivityObserverProvider) {
    this.languagePreferencesProvider = languagePreferencesProvider;
    this.connectivityObserverProvider = connectivityObserverProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<LanguagePreferences> languagePreferencesProvider,
      Provider<ConnectivityObserver> connectivityObserverProvider) {
    return new MainActivity_MembersInjector(languagePreferencesProvider, connectivityObserverProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectLanguagePreferences(instance, languagePreferencesProvider.get());
    injectConnectivityObserver(instance, connectivityObserverProvider.get());
  }

  @InjectedFieldSignature("com.msa.android.MainActivity.languagePreferences")
  public static void injectLanguagePreferences(MainActivity instance,
      LanguagePreferences languagePreferences) {
    instance.languagePreferences = languagePreferences;
  }

  @InjectedFieldSignature("com.msa.android.MainActivity.connectivityObserver")
  public static void injectConnectivityObserver(MainActivity instance,
      ConnectivityObserver connectivityObserver) {
    instance.connectivityObserver = connectivityObserver;
  }
}

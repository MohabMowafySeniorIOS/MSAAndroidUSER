package com.msa.android.data.repository;

import com.google.firebase.firestore.FirebaseFirestore;
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
public final class OnBoardingRepositoryImpl_Factory implements Factory<OnBoardingRepositoryImpl> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  public OnBoardingRepositoryImpl_Factory(Provider<FirebaseFirestore> firestoreProvider) {
    this.firestoreProvider = firestoreProvider;
  }

  @Override
  public OnBoardingRepositoryImpl get() {
    return newInstance(firestoreProvider.get());
  }

  public static OnBoardingRepositoryImpl_Factory create(
      Provider<FirebaseFirestore> firestoreProvider) {
    return new OnBoardingRepositoryImpl_Factory(firestoreProvider);
  }

  public static OnBoardingRepositoryImpl newInstance(FirebaseFirestore firestore) {
    return new OnBoardingRepositoryImpl(firestore);
  }
}

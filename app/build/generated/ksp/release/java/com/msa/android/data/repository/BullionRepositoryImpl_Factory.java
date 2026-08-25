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
public final class BullionRepositoryImpl_Factory implements Factory<BullionRepositoryImpl> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  public BullionRepositoryImpl_Factory(Provider<FirebaseFirestore> firestoreProvider) {
    this.firestoreProvider = firestoreProvider;
  }

  @Override
  public BullionRepositoryImpl get() {
    return newInstance(firestoreProvider.get());
  }

  public static BullionRepositoryImpl_Factory create(
      Provider<FirebaseFirestore> firestoreProvider) {
    return new BullionRepositoryImpl_Factory(firestoreProvider);
  }

  public static BullionRepositoryImpl newInstance(FirebaseFirestore firestore) {
    return new BullionRepositoryImpl(firestore);
  }
}

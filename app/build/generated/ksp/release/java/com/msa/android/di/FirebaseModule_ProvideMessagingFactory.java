package com.msa.android.di;

import com.google.firebase.messaging.FirebaseMessaging;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class FirebaseModule_ProvideMessagingFactory implements Factory<FirebaseMessaging> {
  @Override
  public FirebaseMessaging get() {
    return provideMessaging();
  }

  public static FirebaseModule_ProvideMessagingFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FirebaseMessaging provideMessaging() {
    return Preconditions.checkNotNullFromProvides(FirebaseModule.INSTANCE.provideMessaging());
  }

  private static final class InstanceHolder {
    private static final FirebaseModule_ProvideMessagingFactory INSTANCE = new FirebaseModule_ProvideMessagingFactory();
  }
}
